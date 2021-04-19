package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserEnumConstantDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserEnumDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.google.common.collect.BiMap;
import com.sim.application.classes.ChangeInformation;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.utils.StringEncryption;
import javafx.util.Pair;

import java.nio.file.Paths;
import java.util.*;

public final class ObfuscateNameController extends Technique {
    private static ObfuscateNameController instance;
    private final String name = "Name Obfuscation";
    private final String description = "Replaces all declared names (e.g. classes, variables) with random strings";

    public static ObfuscateNameController getInstance() {
        if (instance == null) {
            instance = new ObfuscateNameController();
        }
        return instance;
    }

    private ObfuscateNameController() {}
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

        String currFile = "";
        try {
            // Gathering all class declarations
            for (var file : sourceFiles.keySet()) {
                var unit = sourceFiles.get(file);
                currFile = file.getFileName();
                var reconVisitor = new ReconnaissanceVisitor(unit);
                reconVisitor.visit(unit, classMap);
            }

            for (var file : sourceFiles.keySet()) {
                // Gather all instances that need to be changed
                var changeList = new ArrayList<ChangeInformation>();
                var unit = sourceFiles.get(file);
                currFile = file.getFileName();
                var visitor = new ChangeVisitor(currFile, changeList, problemList);
                visitor.visit(unit, classMap);

                // Make the changes
                processChanges(file, unit, changeList, classMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
        }
        System.out.println("_________________________________________________");
        problemList.forEach(System.out::println);
    }

    private void processChanges(JavaFile file, CompilationUnit unit, List<ChangeInformation> changeList, ClassMap classMap) {
        boolean isRenamed = false;

        for (var change : changeList) {
            var node = change.getNode();
            var qualifiedName = change.getQualifiedName();
            var scope = change.getScope();
            var qualifiedScope = change.getQualifiedScope();
            var isEnum = change.isEnum();

            if (node instanceof ImportDeclaration) {
                var type = (ImportDeclaration)node;
                var packageName = getPackage(classMap.get(qualifiedName));
                var shortName = nameBuilder("", classMap.get(qualifiedName), ".");

                StringBuilder newName = new StringBuilder(shortName);
                while (!packageName.equals("")) {
                    var scopeName = classMap.get(packageName);
                    if (scopeName == null) {
                        newName.insert(0, ".");
                        newName.insert(0, packageName);
                        break;
                    } else {
                        shortName = nameBuilder("", scopeName, ".");
                        newName.insert(0, ".");
                        newName.insert(0, shortName);
                        packageName = getPackage(scopeName);
                    }
                }
                type.setName(newName.toString());
            } else if (node instanceof AnnotationDeclaration) {
                var type = (AnnotationDeclaration)node;
                var newName = nameBuilder(type.getNameAsString(), classMap.get(qualifiedName), ".");
                type.setName(newName);
            } else if (node instanceof AnnotationMemberDeclaration) {
                var type = (AnnotationMemberDeclaration)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), " ");
                type.setName(newName);
            }  else if (node instanceof MarkerAnnotationExpr) {
                var type = (MarkerAnnotationExpr)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), ".");
                type.setName(newName);
            } else if (node instanceof NormalAnnotationExpr) {
                var type = (NormalAnnotationExpr)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), ".");
                type.setName(newName);
            } else if (node instanceof MemberValuePair) {
                var type = (MemberValuePair)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), " ");
                type.setName(newName);
            } else if (node instanceof ClassOrInterfaceDeclaration) {
                var type = (ClassOrInterfaceDeclaration)node;
                var newName = nameBuilder(type.getNameAsString(), classMap.get(qualifiedName), ".");
                type.setName(newName);

                if(!type.isNestedType() && !isRenamed) {
                    var fileName = nameBuilder("", newName, ".");
                    var newPath = Paths.get(unit.getStorage().get().getDirectory().toString(), fileName + ".java");
                    unit.setStorage(newPath);
                    file.setNewFullPath(newPath.toString());
                    isRenamed = true;
                }
            } else if (node instanceof ClassOrInterfaceType) {
                var type = (ClassOrInterfaceType)node;
                var newName = nameBuilder(type.getNameAsString(), classMap.get(qualifiedName), ".");
                type.setName(newName);
            } else if (node instanceof MethodDeclaration) {
                var type = (MethodDeclaration)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), ".");
                type.setName(newName);
            } else if (node instanceof ConstructorDeclaration) {
                var type = (ConstructorDeclaration)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), ".");
                type.setName(newName);
            } else if (node instanceof MethodCallExpr) {
                var type = (MethodCallExpr)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), ".");
                type.setName(newName);

                if (scope != null && qualifiedScope != null) {
                    var newScope = nameBuilder(scope, classMap.get(qualifiedScope), ".");
                    type.getScope()
                            .ifPresent(oldScope -> oldScope
                                .ifNameExpr(nameExpr -> nameExpr
                                    .setName(newScope)));
                }
            } else if (node instanceof MethodReferenceExpr) {
                var type = (MethodReferenceExpr)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), ".");
                type.setIdentifier(newName);
            } else if (node instanceof Parameter) {
                var type = (Parameter)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), " ");
                type.setName(newName);
            } else if (node instanceof VariableDeclarator) {
                var type = (VariableDeclarator)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), " ");
                type.setName(newName);
            } else if (node instanceof FieldAccessExpr) {
                var type = (FieldAccessExpr)node;
                String newName;
                if (isEnum) {
                    newName = nameBuilder("", classMap.get(qualifiedName), ".");
                    newName = nameBuilder("", newName, " ");
                } else {
                    newName = nameBuilder("", classMap.get(qualifiedName), " ");
                }
                type.setName(newName);

                if (scope != null && qualifiedScope != null) {
                    var newScope = nameBuilder(scope, classMap.get(qualifiedScope), ".");
                    type.getScope()
                            .ifNameExpr(nameExpr -> nameExpr
                                .setName(newScope));
                }
            } else if (node instanceof NameExpr) {
                var type = (NameExpr)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), " ");
                type.setName(newName);
            } else if (node instanceof EnumDeclaration) {
                var type = (EnumDeclaration)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), ".");
                type.setName(newName);
            } else if (node instanceof EnumConstantDeclaration) {
                var type = (EnumConstantDeclaration)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), " ");
                type.setName(newName);
            }
        }
    }

    private static String nameBuilder(String oldNameWithPackage, String newName, String separator) {
        if (oldNameWithPackage == null) return newName;
        if (newName == null) return oldNameWithPackage;

        var arr = separator.equals(".") ? newName.split("\\.") : newName.split(separator);
        var changedName = arr[arr.length - 1];
        var index = oldNameWithPackage.lastIndexOf(separator);
        String packageName = "";
        if (index != -1) {
            packageName = oldNameWithPackage.substring(0, index + 1);
        }

        return packageName + changedName;
    }

    private static String getPackage(String qualifiedName) {
        var index = qualifiedName.lastIndexOf(".");

        if (index != -1) {
            return qualifiedName.substring(0, index);
        } else {
            return "";
        }
    }

    private static String getName(String qualifiedName) {
        var index = qualifiedName.lastIndexOf(".");

        if (index != -1) {
            return qualifiedName.substring(index + 1);
        } else {
            return qualifiedName;
        }
    }

    private static String joinNames(String... names) {
        StringBuilder str = new StringBuilder();
        for (String name : names) {
            str.append(name).append(".");
        }
        return str.substring(0, str.lastIndexOf("."));
    }

    private static <T extends Node> Pair<T, Integer> getParentNode(Node node, Class<T> containerClass) {
        return getParentNode(node, containerClass, 0);
    }

    private static <T extends Node> Pair<T, Integer> getParentNode(Node node, Class<T> containerClass, int depth) {
        if (node.getParentNode().isPresent()) {
            if (containerClass.isInstance(node.getParentNode().get())) {
                return new Pair<>((T)node.getParentNode().get(), depth + 1);
            } else {
                return getParentNode(node.getParentNode().get(), containerClass, depth + 1);
            }
        } else {
            return null;
        }
    }

    private static <T extends Node> List<Pair<T, Integer>> getParentNodes(Node node, Class<T> containerClass) {
        var parentSet = new HashSet<Pair<T, Integer>>();

        var containerNode = getParentNode(node, containerClass);
        int depth = 0;
        while (containerNode != null && !parentSet.contains(containerNode)) {
            depth += containerNode.getValue();
            parentSet.add(new Pair<>(containerNode.getKey(), depth));
            containerNode = getParentNode(containerNode.getKey(), containerClass);
        }
        return new ArrayList<>(parentSet);
    }

    private static class ReconnaissanceVisitor extends ModifierVisitor<ClassMap> {

        private final CompilationUnit unit;
        private final StringEncryption stringEncryption;

        private ReconnaissanceVisitor(CompilationUnit unit) {
            this.unit = unit;
            this.stringEncryption = new StringEncryption();
        }

        @Override
        public AnnotationDeclaration visit(AnnotationDeclaration ad, ClassMap classMap) {
            super.visit(ad, classMap);

            ad.getFullyQualifiedName().ifPresent(qualifiedName -> {
                String newName = nameBuilder(qualifiedName, stringEncryption.getEncryptedVariableName(), ".");
                while (classMap.containsValue(newName)) {
                    newName = nameBuilder(qualifiedName, stringEncryption.getEncryptedVariableName(), ".");
                }

                classMap.put(qualifiedName, newName);
            });

            return ad;
        }

        @Override
        public AnnotationMemberDeclaration visit(AnnotationMemberDeclaration ad, ClassMap classMap) {
            super.visit(ad, classMap);

            if (ad.getNameAsString().equals("value")) return ad;


            ad.getParentNode().ifPresent(parent -> {
                var declaration = (AnnotationDeclaration)parent;
                declaration.getFullyQualifiedName().ifPresent(qualifiedName -> {
                    var vName = ad.getName();
                    var identifier = qualifiedName + " " + vName;

                    String newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                    while (classMap.containsValue(newName)) {
                        newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                    }

                    classMap.put(identifier, newName);
                });
            });

            return ad;
        }

        @Override
        public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration cid, ClassMap classMap) {
            super.visit(cid, classMap);

            String oldName;
            if (cid.getFullyQualifiedName().isPresent())
                oldName = cid.getFullyQualifiedName().get();
            else if (unit.getPackageDeclaration().isPresent())
                oldName = unit.getPackageDeclaration().get() + cid.getNameAsString();
            else
                oldName = cid.getNameAsString();

            if (classMap.containsKey(oldName)) return cid;

            String packageName = oldName.replace(cid.getNameAsString(), "");
            String newName = stringEncryption.getEncryptedVariableName();
            while (classMap.containsValue(packageName + newName)) {
                newName = stringEncryption.getEncryptedVariableName();
            }

            classMap.put(oldName, packageName + newName);

            return cid;
        }

        @Override
        public MethodDeclaration visit(MethodDeclaration md, ClassMap classMap) {
            super.visit(md, classMap);

            // Ignore main (entry) method
            var signature = md.getSignature();
            if (md.isPublic() &&
                md.isStatic() &&
                md.getType().isVoidType() &&
                signature.asString().equals("main(String[])")) {
                    return md;
            }
            var isOverrideMethod = md.getAnnotations().stream().anyMatch(annotationExpr -> annotationExpr.getNameAsString().equals("Override"));
            if (isOverrideMethod) return md;

            try {
                var resolvedMethod = md.resolve();
                String qualifiedName = resolvedMethod.getQualifiedName();
                String qualifiedSignature = resolvedMethod.getQualifiedSignature();

                if (classMap.containsKey(qualifiedSignature)) return md;

                String newName = nameBuilder(qualifiedName, stringEncryption.getEncryptedVariableName(), ".");
                while (classMap.containsValue(newName)) {
                    newName = nameBuilder(qualifiedName, stringEncryption.getEncryptedVariableName(), ".");
                }

                classMap.put(qualifiedSignature, newName);
            } catch (Exception ignored) {

            }

            return md;
        }

        @Override
        public FieldDeclaration visit(FieldDeclaration fd, ClassMap classMap) {
            super.visit(fd, classMap);
            try {
                var resolvedFieldDeclaration = fd.resolve();
                var declaringType = resolvedFieldDeclaration.declaringType();
                var qualifiedClassName = declaringType.getQualifiedName();

                fd.getVariables().forEach(v -> {
                    var vName = v.getNameAsString();
                    var identifier = qualifiedClassName + " " + vName;
                    if (classMap.containsKey(identifier)) return;

                    String newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                    while (classMap.containsValue(newName)) {
                        newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                    }

                    classMap.put(identifier, newName);


                });
            } catch (Exception ignored) {

            }
            return fd;
        }

        @Override
        public VariableDeclarationExpr visit(VariableDeclarationExpr vd, ClassMap classMap) {
            super.visit(vd, classMap);

            vd.getVariables().forEach(v -> {
                try {
                    if (vd.getParentNode().isEmpty()) return;

                    Node container;
                    if (vd.getParentNode().get() instanceof ForStmt ||
                        vd.getParentNode().get() instanceof ForEachStmt) {
                            container = vd.getParentNode().get();
                    } else {
                        container = vd.findAncestor(BlockStmt.class).get();
                    }

                    var containerHash = System.identityHashCode(container);
                    String vName = v.getNameAsString();
                    var identifier = containerHash + " " + vName;

                    if (classMap.containsKey(identifier)) return;

                    String newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                    while (classMap.containsValue(newName)) {
                        newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                    }

                    classMap.put(identifier, newName);
                } catch (Exception ignored) {

                }
            });

            return vd;
        }

        @Override
        public Parameter visit(Parameter p, ClassMap classMap) {
            super.visit(p, classMap);

            try {
                if (p.getParentNode().isEmpty()) return p;

                var container = p.getParentNode().get();
                var containerHash = System.identityHashCode(container);
                String vName = p.getNameAsString();
                var identifier = containerHash + " " + vName;

                if (classMap.containsKey(identifier)) return p;

                String newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                while (classMap.containsValue(newName)) {
                    newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                }
                classMap.put(identifier, newName);
            } catch (Exception ignored) {

            }

            return p;
        }

        @Override
        public EnumDeclaration visit(EnumDeclaration ed, ClassMap classMap) {
            super.visit(ed, classMap);

            try {
                var resolvedEnum = ed.resolve();
                var qualifiedName = resolvedEnum.getQualifiedName();

                if (classMap.containsKey(qualifiedName)) return ed;

                String newName = nameBuilder(qualifiedName, stringEncryption.getEncryptedVariableName(), ".");
                while (classMap.containsValue(newName)) {
                    newName = nameBuilder(qualifiedName, stringEncryption.getEncryptedVariableName(), ".");
                }
                classMap.put(qualifiedName, newName);
            } catch (Exception ignored) {

            }

            return ed;
        }

        @Override
        public EnumConstantDeclaration visit(EnumConstantDeclaration ecd, ClassMap classMap) {
            super.visit(ecd, classMap);

            try {
                var resolvedEnum = ecd.resolve();
                var cType = resolvedEnum.getType().describe();
                var cName = resolvedEnum.getName();
                var identifier = cType + " " + cName;

                if (classMap.containsKey(identifier)) return ecd;

                String newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                while (classMap.containsValue(newName)) {
                    newName = nameBuilder(identifier, stringEncryption.getEncryptedVariableName(), " ");
                }
                classMap.put(identifier, newName);
            } catch (Exception ignored) {

            }

            return ecd;
        }
    }

    private static class ChangeVisitor extends ModifierVisitor<ClassMap> {

        private final String fileName;
        private final List<ChangeInformation> changeList;
        private final List<Problem> problemList;

        private ChangeVisitor(String fileName,
                              List<ChangeInformation> changeList,
                              List<Problem> problemList) {
            this.fileName = fileName;
            this.changeList= changeList;
            this.problemList = problemList;
        }

        @Override
        public ImportDeclaration visit(ImportDeclaration id, ClassMap classMap) {
            super.visit(id, classMap);

            if (classMap.containsKey(id.getNameAsString())) {
                changeList.add(new ChangeInformation(id, id.getNameAsString()));
            }

            return id;
        }

        @Override
        public AnnotationDeclaration visit(AnnotationDeclaration ad, ClassMap classMap) {
            super.visit(ad, classMap);

            ad.getFullyQualifiedName().ifPresent(qualifiedName -> {
                if (classMap.containsKey(qualifiedName)) {
                    changeList.add(new ChangeInformation(ad, qualifiedName));
                }
            });

            return ad;
        }

        @Override
        public AnnotationMemberDeclaration visit(AnnotationMemberDeclaration ad, ClassMap classMap) {
            super.visit(ad, classMap);

            if (ad.getNameAsString().equals("value")) return ad;

            ad.getParentNode().ifPresent(parent -> {
                var declaration = (AnnotationDeclaration)parent;
                declaration.getFullyQualifiedName().ifPresent(qualifiedName -> {
                    var vName = ad.getName();
                    var identifier = qualifiedName + " " + vName;

                    if (classMap.containsKey(identifier)) {
                        changeList.add(new ChangeInformation(ad, identifier));
                    }
                });
            });

            return ad;
        }

        @Override
        public MarkerAnnotationExpr visit(MarkerAnnotationExpr ma, ClassMap classMap) {
            super.visit(ma, classMap);

            try {
                var resolvedAnnotation = ma.resolve();
                var qualifiedName = resolvedAnnotation.getQualifiedName();

                if (classMap.containsKey(qualifiedName)) {
                    changeList.add(new ChangeInformation(ma, qualifiedName));
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(ma, e, fileName));
            }

            return ma;
        }
        @Override
        public NormalAnnotationExpr visit(NormalAnnotationExpr na, ClassMap classMap) {
            super.visit(na, classMap);

            try {
                var resolvedAnnotation = na.resolve();
                var qualifiedName = resolvedAnnotation.getQualifiedName();

                if (classMap.containsKey(qualifiedName)) {
                    changeList.add(new ChangeInformation(na, qualifiedName));
                }

                na.getPairs().forEach(memberValuePair -> {
                    var name = memberValuePair.getName();
                    var identifier = qualifiedName + " " + name;
                    if (classMap.containsKey(identifier)) {
                        changeList.add(new ChangeInformation(memberValuePair, identifier));
                    }
                });
            } catch (Exception e) {
                problemList.add(new Problem<>(na, e, fileName));
            }

            return na;
        }

        @Override
        public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration id, ClassMap classMap) {
            super.visit(id, classMap);

            String qualifiedName;
            if (id.getFullyQualifiedName().isPresent()) {
                qualifiedName = id.getFullyQualifiedName().get();
            } else {
                qualifiedName = id.getNameAsString();
            }

            if (classMap.containsKey(qualifiedName)) {
                changeList.add(new ChangeInformation(id, qualifiedName));
            }

            return id;
        }

        @Override
        public ClassOrInterfaceType visit(ClassOrInterfaceType cit, ClassMap classMap) {
            super.visit(cit, classMap);

            if (cit.getNameAsString().equals("var") ||
                cit.isWildcardType()) {
                    return cit;
            }

            ResolvedType resolvedType = null;
            String qualifiedName = "";
            try {

                // Handling instanced inner classes
                if (cit.getParentNode().isPresent()) {
                    if (cit.getParentNode().get() instanceof  ObjectCreationExpr) {
                        var expr = (ObjectCreationExpr)cit.getParentNode().get();
                        if (expr.getScope().isPresent()) {
                            if (expr.getScope().get().isNameExpr()) {
                                resolvedType = expr.getScope().get().asNameExpr().calculateResolvedType();
                                qualifiedName = joinNames(resolvedType.describe(), cit.getNameAsString());
                            }
                        }
                    }
                }
                if (resolvedType == null) {
                    resolvedType = cit.resolve();
                    ResolvedReferenceType resolvedReferenceType = resolvedType.asReferenceType();
                    qualifiedName = resolvedReferenceType.getQualifiedName();
                }

                if (classMap.containsKey(qualifiedName)) {
                    changeList.add(new ChangeInformation(cit, qualifiedName));
                }

            } catch(Exception e) {
                problemList.add(new Problem<>(cit, e, fileName));
            }

            return cit;
        }

        @Override
        public ConstructorDeclaration visit(ConstructorDeclaration cd, ClassMap classMap) {
            super.visit(cd, classMap);

            try {
                var resolvedConstructor = cd.resolve();
                var declaringType = resolvedConstructor.declaringType().getQualifiedName();

                if (classMap.containsKey(declaringType)) {
                    changeList.add(new ChangeInformation(cd, declaringType));
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(cd, e, fileName));
            }

            return cd;
        }

        @Override
        public MethodDeclaration visit(MethodDeclaration md, ClassMap classMap) {
            super.visit(md, classMap);

            // Do not change main method
            var signature = md.getSignature();
            if (md.isPublic() &&
                md.isStatic() &&
                md.getType().isVoidType() &&
                signature.asString().equals("main(String[])")) {
                    return md;
            }

            var isOverrideMethod = md.getAnnotations().stream().anyMatch(annotationExpr -> annotationExpr.getNameAsString().equals("Override"));
            try {
                var resolvedMethod = md.resolve();

                if (isOverrideMethod) {
                    var declaringClass = resolvedMethod.declaringType().asClass();
                    var interfaceList = declaringClass.getInterfaces();
                    var superClass = declaringClass.getSuperClass().orElse(null);

                    if (superClass != null) {
                        String qualifiedSignature = superClass.getQualifiedName() + "." + resolvedMethod.getSignature();
                        if (classMap.containsKey(qualifiedSignature)) {
                            changeList.add(new ChangeInformation(md, qualifiedSignature));
                            return md;
                        }
                    }
                    for (var interfaze : interfaceList) {
                        String qualifiedSignature = interfaze.getQualifiedName() + "." + resolvedMethod.getSignature();
                        if (classMap.containsKey(qualifiedSignature)) {
                            changeList.add(new ChangeInformation(md, qualifiedSignature));
                            break;
                        }
                    }
                } else {
                    String qualifiedSignature = resolvedMethod.getQualifiedSignature();
                    if (classMap.containsKey(qualifiedSignature)) {
                        changeList.add(new ChangeInformation(md, qualifiedSignature));
                    }
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(md, e, fileName));
            }

            return md;
        }

        @Override
        public MethodCallExpr visit(MethodCallExpr mc, ClassMap classMap) {
            super.visit(mc, classMap);

            try {
                var resolvedMethod = mc.resolve();
                var qualifiedSignature = resolvedMethod.getQualifiedSignature();

                if (classMap.containsKey(qualifiedSignature)) {
                    if (resolvedMethod.isStatic() && mc.getScope().isPresent()) {
                        var scope = mc.getScope().get().asNameExpr().getNameAsString();
                        var qualifiedScope = resolvedMethod.declaringType().getQualifiedName();

                        changeList.add(new ChangeInformation(mc, qualifiedSignature, scope, qualifiedScope));
                    }
                    else {
                        changeList.add(new ChangeInformation(mc, qualifiedSignature));
                    }
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(mc, e, fileName));
            }

            return mc;
        }

        @Override
        public MethodReferenceExpr visit(MethodReferenceExpr mr, ClassMap classMap) {
            super.visit(mr, classMap);

            try {
                ResolvedMethodDeclaration resolvedMethod = null;
                var scope = mr.getScope();
                var typeArgs = mr.getTypeArguments();
                // Solve scope using default
                try {
                    resolvedMethod = mr.resolve();
                } catch (UnsolvedSymbolException e) {
                    var packageName = getPackage(scope.toString());
                    var newScope = packageName.length() == 0 ? new TypeExpr() : new TypeExpr(new ClassOrInterfaceType().setName(packageName));
                    var newTypeArgs = typeArgs.orElse(null);
                    var newIdentifier = getName(scope.toString());

                    FieldAccessExpr fieldAccessExpr;
                    if (newTypeArgs != null) {
                        fieldAccessExpr = new FieldAccessExpr(newScope, newTypeArgs, new SimpleName(newIdentifier));
                    } else {
                        fieldAccessExpr = new FieldAccessExpr(newScope, newIdentifier);
                    }
                    mr.setScope(fieldAccessExpr);
                }
                // Solve scope as fieldAccessExpr
                try {
                    if (resolvedMethod == null) {
                        resolvedMethod = mr.resolve();
                    }
                } catch (UnsolvedSymbolException e) {
                    var nameExpr = new NameExpr(scope.toString());
                    mr.setScope(nameExpr);
                }
                // Solve scope as NameExpr
                try {
                    if (resolvedMethod == null) {
                        resolvedMethod = mr.resolve();
                    }
                } catch (UnsolvedSymbolException e) {
                    problemList.add(new Problem<>(mr, e, fileName));
                }

                if (resolvedMethod != null) {
                    var qualifiedSignature = resolvedMethod.getQualifiedSignature();

                    if (classMap.containsKey(qualifiedSignature)) {
                        if (mr.getScope() instanceof NameExpr) {
                            solveNameExpr((NameExpr) mr.getScope(), classMap);
                        }
                        changeList.add(new ChangeInformation(mr, qualifiedSignature));
                    }
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(mr, e, fileName));
            }

            return mr;
        }

        @Override
        public FieldDeclaration visit(FieldDeclaration fd, ClassMap classMap) {
            super.visit(fd, classMap);

            try {
                var resolvedFieldDeclaration = fd.resolve();
                var declaringType = resolvedFieldDeclaration.declaringType();
                var qualifiedClassName = declaringType.getQualifiedName();

                fd.getVariables()
                    .forEach(v -> {
                        var vName = v.getNameAsString();
                        var identifier = qualifiedClassName + " " + vName;

                        if (classMap.containsKey(identifier)) {
                            changeList.add(new ChangeInformation(v, identifier));
                        }
                    });
            } catch (Exception e) {
                problemList.add(new Problem<>(fd, e, fileName));
            }

            return fd;
        }

        @Override
        public FieldAccessExpr visit(FieldAccessExpr fa, ClassMap classMap) {
            super.visit(fa, classMap);

            try {
                var resolvedFieldAccess = fa.resolve();
                if (resolvedFieldAccess instanceof JavaParserFieldDeclaration) {
                    var value = (JavaParserFieldDeclaration)resolvedFieldAccess;
                    var field = value.getWrappedNode();

                    var resolvedField = field.resolve();
                    var scope = resolvedField.declaringType().getClassName();
                    var qualifiedClassName = resolvedField.declaringType().getQualifiedName();
                    var vName = fa.getName();

                    var identifier = qualifiedClassName + " " + vName;
                    if (classMap.containsKey(identifier)) {
                        if (field.isStatic())
                            changeList.add(new ChangeInformation(fa, identifier, scope, qualifiedClassName));
                        else
                            changeList.add(new ChangeInformation(fa, identifier));
                    }
                } else if (resolvedFieldAccess instanceof  JavaParserEnumConstantDeclaration) {
                    var value = (JavaParserEnumConstantDeclaration)resolvedFieldAccess.asEnumConstant();
                    var cType = value.getType().describe();
                    var cName = value.getName();

                    var identifier = cType + " " + cName;
                    if (classMap.containsKey(identifier)) {
                        changeList.add(new ChangeInformation(fa, identifier, fa.getScope().toString(), cType, true));
                    }
                }
            } catch (UnsolvedSymbolException e) {
                ResolvedType resolvedType = null;
                try {
                    resolvedType = fa.calculateResolvedType();
                } catch (RuntimeException re) {
                    problemList.add(new Problem<>(fa, re, fileName));
                    return fa;
                }
                if (resolvedType != null && resolvedType.isReferenceType()) {
                    var referenceType = resolvedType.asReferenceType();
                    var declaration = referenceType.getTypeDeclaration();

                    if (declaration.isPresent() &&
                        declaration.get().isEnum() &&
                        declaration.get() instanceof JavaParserEnumDeclaration) {

                        String scope = "";
                        String qualifiedScope = "";
                        var enumDeclaration = (JavaParserEnumDeclaration)declaration.get();
                        var declaringType = enumDeclaration.getWrappedNode().getParentNode();
                        if (declaringType.isPresent() &&
                            declaringType.get() instanceof ClassOrInterfaceDeclaration) {

                            scope = ((ClassOrInterfaceDeclaration) declaringType.get()).getNameAsString();
                            qualifiedScope = ((ClassOrInterfaceDeclaration) declaringType.get()).getFullyQualifiedName().get();
                        }
                        if (classMap.containsKey(referenceType.getQualifiedName())) {
                            if (!scope.equals("") && !qualifiedScope.equals("")) {
                                changeList.add(new ChangeInformation(fa, referenceType.getQualifiedName(), scope, qualifiedScope, true));
                            } else {
                                changeList.add(new ChangeInformation(fa, referenceType.getQualifiedName()));
                            }

                            return fa;
                        }
                    }
                }
                problemList.add(new Problem<>(fa, e, fileName));
            } catch (Exception e) {
                problemList.add(new Problem<>(fa, e, fileName));
            }

            return fa;
        }

        @Override
        public VariableDeclarationExpr visit(VariableDeclarationExpr vd, ClassMap classMap) {
            super.visit(vd, classMap);

            vd.getVariables().forEach(v -> {
                try {
                    if (vd.getParentNode().isEmpty()) return;

                    Node container;
                    if (vd.getParentNode().get() instanceof ForStmt ||
                            vd.getParentNode().get() instanceof ForEachStmt) {
                        container = vd.getParentNode().get();
                    } else {
                        container = vd.findAncestor(BlockStmt.class).get();
                    }

                    var containerHash = System.identityHashCode(container);
                    String vName = v.getNameAsString();
                    var identifier = containerHash + " " + vName;

                    if (classMap.containsKey(identifier)) {
                        changeList.add(new ChangeInformation(v, identifier));
                    }
                } catch (Exception e) {
                    problemList.add(new Problem<>(v, e, fileName));
                }
            });

            return vd;
        }

        @Override
        public NameExpr visit(NameExpr ne, ClassMap classMap) {
            super.visit(ne, classMap);

            return solveNameExpr(ne, classMap);
        }

        private NameExpr solveNameExpr(NameExpr ne, ClassMap classMap) {
            try {
                var nodeIdentifiers = new ArrayList<Pair<String, Integer>>();

                var enumContainers = getParentNodes(ne, EnumDeclaration.class);
                if (enumContainers.size() > 0) {
                    enumContainers.forEach(c -> {
                        var qualifiedName = c.getKey().getFullyQualifiedName().get();
                        nodeIdentifiers.add(new Pair<>(qualifiedName, c.getValue()));
                    });
                }
                var classContainers = getParentNodes(ne, ClassOrInterfaceDeclaration.class);
                if (classContainers.size() > 0) {
                    classContainers.forEach(c -> {
                        var qualifiedName = c.getKey().getFullyQualifiedName().get();
                        nodeIdentifiers.add(new Pair<>(qualifiedName, c.getValue()));
                    });
                }
                var forContainers = getParentNodes(ne, ForStmt.class);
                if (forContainers.size() > 0) {
                    forContainers.forEach(c -> {
                        var hash = Integer.toString(System.identityHashCode(c.getKey()));
                        nodeIdentifiers.add(new Pair<>(hash, c.getValue()));
                    });
                }
                var foreachContainers = getParentNodes(ne, ForEachStmt.class);
                if (foreachContainers.size() > 0) {
                    foreachContainers.forEach(c -> {
                        var hash = Integer.toString(System.identityHashCode(c.getKey()));
                        nodeIdentifiers.add(new Pair<>(hash, c.getValue()));
                    });
                }
                var blockContainers = getParentNodes(ne, BlockStmt.class);
                if (blockContainers.size() > 0) {
                    blockContainers.forEach(c -> {
                        var hash = Integer.toString(System.identityHashCode(c.getKey()));
                        nodeIdentifiers.add(new Pair<>(hash, c.getValue()));
                    });
                }
                var constructorContainers = getParentNodes(ne, ConstructorDeclaration.class);
                if (constructorContainers.size() > 0) {
                    constructorContainers.forEach(c -> {
                        var hash = Integer.toString(System.identityHashCode(c.getKey()));
                        nodeIdentifiers.add(new Pair<>(hash, c.getValue()));
                    });
                }
                var methodContainers = getParentNodes(ne, MethodDeclaration.class);
                if (methodContainers.size() > 0) {
                    methodContainers.forEach(c -> {
                        var hash = Integer.toString(System.identityHashCode(c.getKey()));
                        nodeIdentifiers.add(new Pair<>(hash, c.getValue()));
                    });
                }
                var lambdaContainers = getParentNodes(ne, LambdaExpr.class);
                if (lambdaContainers.size() > 0) {
                    lambdaContainers.forEach(c -> {
                        var hash = Integer.toString(System.identityHashCode(c.getKey()));
                        nodeIdentifiers.add(new Pair<>(hash, c.getValue()));
                    });
                }

                nodeIdentifiers.sort(Comparator.comparingInt(Pair::getValue));

                for (var nodeIdentifier : nodeIdentifiers) {
                    var identifier = nodeIdentifier.getKey() + " " + ne.getNameAsString();
                    if (classMap.containsKey(identifier)) {
                        changeList.add(new ChangeInformation(ne, identifier));
                        return ne;
                    }
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(ne, e, fileName));
            }
            return ne;
        }

        @Override
        public Parameter visit(Parameter p, ClassMap classMap) {
            super.visit(p, classMap);

            try {
                if (p.getParentNode().isEmpty()) return p;

                var container = p.getParentNode().get();
                var containerHash = System.identityHashCode(container);
                String vName = p.getNameAsString();
                var identifier = containerHash + " " + vName;

                if (classMap.containsKey(identifier)) {
                    changeList.add(new ChangeInformation(p, identifier));
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(p, e, fileName));
            }

            return p;
        }

        @Override
        public EnumDeclaration visit(EnumDeclaration ed, ClassMap classMap) {
            super.visit(ed, classMap);

            try {
                var resolvedEnum = ed.resolve();
                var qualifiedName = resolvedEnum.getQualifiedName();

                if (classMap.containsKey(qualifiedName)) {
                    changeList.add(new ChangeInformation(ed, qualifiedName));
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(ed, e, fileName));
            }

            return ed;
        }

        @Override
        public EnumConstantDeclaration visit(EnumConstantDeclaration ecd, ClassMap classMap) {
            super.visit(ecd, classMap);

            try {
                var resolvedEnum = ecd.resolve();
                var cType = resolvedEnum.getType().describe();
                var cName = resolvedEnum.getName();
                var identifier = cType + " " + cName;

                if (classMap.containsKey(identifier)) {
                    changeList.add(new ChangeInformation(ecd, identifier));
                }
            } catch (Exception e) {
                problemList.add(new Problem<>(ecd, e, fileName));
            }

            return ecd;
        }
    }
}
