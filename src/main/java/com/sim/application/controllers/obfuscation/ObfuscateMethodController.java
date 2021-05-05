package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.google.common.collect.BiMap;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.utils.StringUtil;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class ObfuscateMethodController extends Technique {
    private static ObfuscateMethodController instance;
    private final String name = "Method Obfuscation";
    private final String description = "Splits method implementation where possible";

    public static ObfuscateMethodController getInstance() {
        if (instance == null) {
            instance = new ObfuscateMethodController();
        }
        return instance;
    }

    private ObfuscateMethodController() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(BiMap<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {
        for (var file : sourceFiles.keySet()) {
            var currFile = file.getFileName();

            var unit = sourceFiles.get(file);
            var usedNames = getAllUsedNames(unit);
            var changeList = new ArrayList<Pair<Node, MethodDeclaration>>();

            try {
                var methodVisitor = new MethodVisitor(usedNames, changeList);
                methodVisitor.visit(unit, null);

                for (var change : changeList) {
                    var parentNode = change.getKey();
                    var method = change.getValue();

                    if (parentNode instanceof ClassOrInterfaceDeclaration) {
                        var clazz = (ClassOrInterfaceDeclaration)parentNode;
                        clazz.getMembers().add(method);
                    } else if (parentNode instanceof ObjectCreationExpr) {
                        var clazz = (ObjectCreationExpr)parentNode;
                        clazz.addAnonymousClassBody(method);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
            }
        }
    }

    private HashSet<String> getAllUsedNames(CompilationUnit unit) {
        var methodNames = new HashSet<String>();
        unit.getTypes().forEach(typeDeclaration -> {
            if (!(typeDeclaration instanceof AnnotationDeclaration)) {
                var resolvedType = typeDeclaration.resolve();
                try {
                    var methods = resolvedType.getAllMethods();
                    methodNames.addAll(methods.stream().map(MethodUsage::getName).collect(Collectors.toSet()));
                } catch (UnsolvedSymbolException ignored) { }
            }
        });
        return methodNames;
    }

    private class MethodVisitor extends ModifierVisitor<Void> {

        private final HashSet<String> usedNames;
        private final ArrayList<Pair<Node, MethodDeclaration>> changeList;

        private MethodVisitor(HashSet<String> usedNames,
                              ArrayList<Pair<Node, MethodDeclaration>> changeList) {
            this.usedNames = usedNames;
            this.changeList = changeList;
        }

        public MethodDeclaration visit(MethodDeclaration md, Void args) {
            super.visit(md, args);

            md.getBody().ifPresent(body -> {
                var clazz = getContainingClass(md);
                if (clazz.isEmpty()) return;
                var statements = body.getStatements();
                if (statements.size() < 2) return;
                int indexToCut = statements.size() / 2;

                // Collect all variable declarations from statements below the cutoff index
                var newMethodParameters = new NodeList<Parameter>();
                md.getParameters().forEach(parameter -> newMethodParameters.add(new Parameter(parameter.getType(), parameter.getNameAsString())));
                AtomicBoolean hasErrors = new AtomicBoolean(false);
                for (int i = 0; i < indexToCut; i++) {
                    var statement = statements.get(i);
                    statement.ifExpressionStmt(expressionStmt -> {
                        var expression = expressionStmt.getExpression();
                        expression.ifVariableDeclarationExpr(variableDeclarationExpr ->
                                variableDeclarationExpr.getVariables().forEach(variableDeclarator -> {
                                    String type = getType(variableDeclarator);
                                    if (type == null) {
                                        hasErrors.set(true);
                                        return;
                                    }
                                    var parameter = new Parameter();
                                    parameter.setType(type);
                                    parameter.setName(variableDeclarator.getNameAsString());
                                    newMethodParameters.add(parameter);
                                })
                        );
                    });
                }

                if (hasErrors.get()) return;

                // Store all statements beyond the cutoff index
                var newMethodStatements = new NodeList<Statement>();
                for (int i = indexToCut; i < statements.size(); i++) {
                    newMethodStatements.add(statements.get(i));
                }
                // Remove statements from old method
                int prevSize = statements.size();
                for (int i = indexToCut; i < prevSize; i++) {
                    statements.removeLast();
                }

                // Generate deep clone of old method with new name
                var name = generateMethodName();
                var hasReturnStmt = hasReturnStatement(newMethodStatements);
                var newMethod = generateNewMethod(md, name, newMethodParameters, newMethodStatements, hasReturnStmt);

                // Add method call to new method in old method
                var methodCall = new MethodCallExpr();
                methodCall.setName(name);
                newMethodParameters.forEach(parameter -> methodCall.addArgument(parameter.getNameAsString()));
                if (!md.getType().isVoidType() && hasReturnStmt) {
                    body.addStatement(new ReturnStmt(methodCall));
                } else {
                    body.addStatement(new ExpressionStmt(methodCall));
                }

                // Add new method later to class
                changeList.add(new Pair<>(clazz.get(), newMethod));
            });

            return md;
        }

        private MethodDeclaration generateNewMethod(MethodDeclaration oldMethod, String name, NodeList<Parameter> newMethodParameters, NodeList<Statement> newMethodStatements, boolean hasReturnStmt) {
            var newMethod = new MethodDeclaration();

            newMethod.setName(name);
            newMethod.setType(hasReturnStmt ? oldMethod.getTypeAsString() : "void");
            newMethod.setParameters(newMethodParameters);
            newMethod.setBody(new BlockStmt(newMethodStatements));

            for (var modifier : oldMethod.getModifiers()) {
                newMethod.addModifier(modifier.getKeyword());
            }
            for (var typeParam : oldMethod.getTypeParameters()) {
                newMethod.addTypeParameter(typeParam.asString());
            }
            for (var exception : oldMethod.getThrownExceptions()) {
                newMethod.addThrownException(exception.clone());
            }

            var receiverParameter = oldMethod.getReceiverParameter();
            if (receiverParameter.isPresent()) {
                var newReceiver = new ReceiverParameter();
                for (var annotation : receiverParameter.get().getAnnotations()) {
                    if (annotation instanceof NormalAnnotationExpr) {
                        var memberValuePairs = ((NormalAnnotationExpr)annotation).getPairs();
                        var normalAnnotation = new NormalAnnotationExpr();
                        normalAnnotation.setName(annotation.getNameAsString());
                        for (var pair : memberValuePairs) {
                            normalAnnotation.addPair(pair.getNameAsString(), pair.getValue());
                        }
                        newMethod.addAnnotation(normalAnnotation);
                    } else if (annotation instanceof MarkerAnnotationExpr) {
                        newMethod.addMarkerAnnotation(annotation.getNameAsString());
                    } else if (annotation instanceof SingleMemberAnnotationExpr) {
                        newMethod.addSingleMemberAnnotation(annotation.getNameAsString(), ((SingleMemberAnnotationExpr) annotation).getMemberValue());
                    }
                }
                newMethod.setReceiverParameter(newReceiver);
            }
            return newMethod;
        }

        private boolean hasReturnStatement(NodeList<Statement> statements) {
            for (var statement : statements) {
                if (statement.findFirst(ReturnStmt.class).isPresent()) {
                    return true;
                }
            }
            return false;
        }

        private Optional<? extends Node> getContainingClass(MethodDeclaration md) {
            Node clazz = null;
            var parentNode = md.getParentNode();
            if (parentNode.isPresent() && parentNode.get() instanceof ObjectCreationExpr) {
                clazz = parentNode.get();
            } else {
                var containingClass = md.findAncestor(ClassOrInterfaceDeclaration.class);
                if (containingClass.isPresent()) {
                    clazz = containingClass.get();
                }
            }
            if (clazz == null) {
                return Optional.empty();
            } else {
                return Optional.of(clazz);
            }
        }

        private String getType(VariableDeclarator variableDeclarator) {
            AtomicReference<String> type = new AtomicReference<>(variableDeclarator.getTypeAsString());
            if (variableDeclarator.getInitializer().isPresent()) {
                if (variableDeclarator.getTypeAsString().equals("var")) {
                    var initializer = variableDeclarator.getInitializer().get();
                    try {
                        var resolvedType = initializer.calculateResolvedType();
                        type.set(resolvedType.describe());
                    } catch (Exception e) {
                        return null;
                    }
                }
            } else {
                if (variableDeclarator.getTypeAsString().equals("var")) {
                    return null;
                } else if (variableDeclarator.getType().isPrimitiveType()) {
                    String value = "";
                    if (variableDeclarator.getTypeAsString().equals("char")) value = "'\\0'";
                    else if (variableDeclarator.getTypeAsString().equals("int")) value = String.valueOf(getPrimitiveDefaultValue(int.class));
                    else if (variableDeclarator.getTypeAsString().equals("double")) value = String.valueOf(getPrimitiveDefaultValue(double.class));
                    else if (variableDeclarator.getTypeAsString().equals("long")) value = String.valueOf(getPrimitiveDefaultValue(long.class)) + "L";
                    else if (variableDeclarator.getTypeAsString().equals("boolean")) value = String.valueOf(getPrimitiveDefaultValue(boolean.class));

                    if (value.equals("")) {
                        return null;
                    }
                    variableDeclarator.setInitializer(value);
                } else {
                    variableDeclarator.setInitializer("null");
                }
            }
            return type.get();
        }

        private String generateMethodName() {
            var randomWord = StringUtil.randomWord();
            if (randomWord != null) {
                while (usedNames.contains(randomWord)) {
                    randomWord = StringUtil.randomWord();
                }
            } else {
                randomWord = StringUtil.randomString(20, true);
                while (usedNames.contains(randomWord)) {
                    randomWord = StringUtil.randomString(20, true);
                }
            }
            usedNames.add(randomWord);
            return randomWord;
        }

        private <T> T getPrimitiveDefaultValue(Class<T> type) {
            if (type.isPrimitive()) return (T)Array.get(Array.newInstance(type, 1), 0);
            else return null;
        }
    }
}
