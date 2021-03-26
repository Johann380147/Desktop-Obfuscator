package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.sim.application.classes.ChangeInformation;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.utils.StringUtil;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ObfuscateNameController extends Technique {
    private static ObfuscateNameController instance;
    private String name = "Name Obfuscation";
    private String description = "Replaces class and variable names with random strings";

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
    public void execute(Map<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {

        String currFile = "";
        try {
            // Gathering all class declarations
            for (var file : sourceFiles.keySet()) {
                var unit = sourceFiles.get(file);
                currFile = file.getFileName();
                var reconVisitor = new ReconnaissanceVisitor(unit, 10, problemList);
                reconVisitor.visit(unit, classMap);
            }

            for (var file : sourceFiles.keySet()) {
                // Gather all instances that need to be changed
                var changeList = new ArrayList<ChangeInformation>();
                var unit = sourceFiles.get(file);
                currFile = file.getFileName();
                var visitor = new ChangeVisitor(unit, currFile, changeList, problemList);
                visitor.visit(unit, classMap);

                // Make the changes
                processChanges(unit, changeList, classMap);
            }
        } catch (Exception e) {
            throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
        }
        System.out.println("_________________________________________________");
        problemList.forEach(System.out::println);
    }

    private void processChanges(CompilationUnit unit, List<ChangeInformation> changeList, ClassMap classMap) {
        boolean isRenamed = false;

        for (var change : changeList) {
            var node = change.getNode();
            var qualifiedName = change.getQualifiedName();
            var scope = change.getScope();
            var qualifiedScope = change.getQualifiedScope();

            if (node instanceof ImportDeclaration) {
                var type = (ImportDeclaration)node;
                var newName = classMap.get(qualifiedName);
                type.setName(newName);
            } else if (node instanceof ClassOrInterfaceDeclaration) {
                var type = (ClassOrInterfaceDeclaration)node;
                var newName = nameBuilder(type.getNameAsString(), classMap.get(qualifiedName), ".");
                type.setName(newName);

                if(!type.isNestedType() && !isRenamed) {
                    var fileName = nameBuilder("", newName, ".");
                    unit.setStorage(Paths.get(unit.getStorage().get().getDirectory().toString(), fileName + ".java"));
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
            } else if (node instanceof VariableDeclarator) {
                var type = (VariableDeclarator)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), " ");
                type.setName(newName);
            } else if (node instanceof FieldAccessExpr) {
                var type = (FieldAccessExpr)node;
                var newName = nameBuilder("", classMap.get(qualifiedName), " ");
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
            }
        }
    }

    private static String nameBuilder(String oldNameWithPackage, String newName, String separator) {
        var arr = separator == "." ? newName.split("\\.") : newName.split(separator);
        var changedName = arr[arr.length - 1];
        var index = oldNameWithPackage.lastIndexOf(separator);
        String packageName = "";
        if (index != -1) {
            packageName = oldNameWithPackage.substring(0, index + 1);
        }

        return packageName + changedName;
    }

    private static String joinNames(String... names) {
        String str = "";
        for (String name : names) {
            str += name + ".";
        }
        return str.substring(0, str.lastIndexOf("."));
    }

    private static <T extends Node> T getContainingNode(Node node, Class<T> containerClass) {
        if (containerClass.isInstance(node)) return ((T) node);
        if (node.getParentNode().isPresent()) return getContainingNode(node.getParentNode().get(), containerClass);
        return null;
    }

    private static <T extends Node> boolean isNodeType(Node node, Class<T> type) {
        if (type.isInstance(node)) return true;
        if (node.getParentNode().isPresent()) return isNodeType(node.getParentNode().get(), type);
        return false;
    }

    private static class ReconnaissanceVisitor extends ModifierVisitor<ClassMap> {

        private final CompilationUnit unit;
        private final int MAX_NAME_LENGTH;
        private final List<Problem> problemList;

        private ReconnaissanceVisitor(CompilationUnit unit,
                            int MAX_NAME_LENGTH,
                            List<Problem> problemList) {
            this.unit = unit;
            this.MAX_NAME_LENGTH = MAX_NAME_LENGTH;
            this.problemList = problemList;
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
            String newName = StringUtil.randomString(MAX_NAME_LENGTH);
            while (classMap.containsValue(packageName + newName)) {
                newName = StringUtil.randomString(MAX_NAME_LENGTH);
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

            try {
                var resolvedMethod = md.resolve();
                String qualifiedName = resolvedMethod.getQualifiedName();
                String qualifiedSignature = resolvedMethod.getQualifiedSignature();

                if (classMap.containsKey(qualifiedSignature)) return md;

                String newName = nameBuilder(qualifiedName, StringUtil.randomString(MAX_NAME_LENGTH), ".");
                while (classMap.containsValue(newName)) {
                    newName = nameBuilder(qualifiedName, StringUtil.randomString(MAX_NAME_LENGTH), ".");
                }

                classMap.put(qualifiedSignature, newName);
            } catch (UnsolvedSymbolException e) {

            }

            return md;
        }

        @Override
        public FieldDeclaration visit(FieldDeclaration fd, ClassMap classMap) {
            super.visit(fd, classMap);
            fd.getVariables().forEach(v -> {
                try {
                    var resolvedFieldDeclaration = fd.resolve();
                    var declaringType = resolvedFieldDeclaration.declaringType();
                    var qualifiedClassName = declaringType.getQualifiedName();

                    var resolvedVariable = v.resolve();
                    var vType = resolvedVariable.getType().describe();
                    var vName = resolvedVariable.getName();

                    var identifier = qualifiedClassName + " " + vType + " " + vName;
                    if (classMap.containsKey(identifier)) return;

                    String newName = nameBuilder(identifier, StringUtil.randomString(MAX_NAME_LENGTH), " ");
                    while (classMap.containsValue(newName)) {
                        newName = nameBuilder(identifier, StringUtil.randomString(MAX_NAME_LENGTH), " ");
                    }

                    classMap.put(identifier, newName);

                } catch (UnsolvedSymbolException e) {

                }
            });

            return fd;
        }

        @Override
        public VariableDeclarationExpr visit(VariableDeclarationExpr vd, ClassMap classMap) {
            super.visit(vd, classMap);

            vd.getVariables().forEach(v -> {
                try {
                    var container = getContainingNode(vd, MethodDeclaration.class);
                    if (container == null) return;

                    var resolvedMethod = container.resolve();
                    var qualifiedContainerName = resolvedMethod.getQualifiedSignature();

                    String vType;
                    String vName;
                    if (v.getType().toString().equals("var")) {
                        vType = "var";
                        vName = v.getNameAsString();
                    } else {
                        var resolvedVariable = v.resolve();
                        vType = resolvedVariable.getType().describe();
                        vName = resolvedVariable.getName();
                    }

                    var identifier = qualifiedContainerName + " " + vType + " " + vName;
                    if (classMap.containsKey(identifier)) return;

                    String newName = nameBuilder(identifier, StringUtil.randomString(MAX_NAME_LENGTH), " ");
                    while (classMap.containsValue(newName)) {
                        newName = nameBuilder(identifier, StringUtil.randomString(MAX_NAME_LENGTH), " ");
                    }

                    classMap.put(identifier, newName);
                } catch (UnsolvedSymbolException e) {

                }
            });

            return vd;
        }
    }

    private static class ChangeVisitor extends ModifierVisitor<ClassMap> {

        private final CompilationUnit unit;
        private final String fileName;
        private final List<ChangeInformation> changeList;
        private final List<Problem> problemList;

        private ChangeVisitor(CompilationUnit unit,
                              String fileName,
                              List<ChangeInformation> changeList,
                              List<Problem> problemList) {
            this.unit = unit;
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
        public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration id, ClassMap classMap) {
            super.visit(id, classMap);

            String qualifiedName = "";
            if (id.getFullyQualifiedName().isPresent()) {
                qualifiedName = id.getFullyQualifiedName().get();
            } else {
                qualifiedName = id.getNameAsString();
            }

            if (classMap.containsKey(id.getFullyQualifiedName().get())) {
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

            } catch(UnsupportedOperationException e) {
                problemList.add(new Problem<>(cit, e, fileName));
                //TODO: let user handle whether they want to change?
                //TODO: TypeParameter (generic)
                //var resolvedMethodDeclaration = methodDeclaration.resolve();
                //ResolvedType returnType = resolvedMethodDeclaration.getReturnType();
            } catch (UnsolvedSymbolException e) {
                problemList.add(new Problem<>(cit, e, fileName));
                //TODO: let user handle whether they want to change?
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
            } catch (UnsolvedSymbolException e) {
                problemList.add(new Problem<>(cd, e, fileName));
                //TODO: let user handle whether they want to change?
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

            String qualifiedSignature = md.getSignature().asString();
            try {
                var resolvedMethod = md.resolve();
                qualifiedSignature = resolvedMethod.getQualifiedSignature();
            } catch (UnsolvedSymbolException e) {
                problemList.add(new Problem<>(md, e, fileName));
                //TODO: let user handle whether they want to change?
            }

            if (classMap.containsKey(qualifiedSignature)) {
                changeList.add(new ChangeInformation(md, qualifiedSignature));
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
            } catch (UnsolvedSymbolException e) {
                problemList.add(new Problem<>(mc, e, fileName));
                //TODO: let user handle whether they want to change?
            } catch (RuntimeException e) {
                problemList.add(new Problem<>(mc, e, fileName));
            }

            return mc;
        }

        @Override
        public MethodReferenceExpr visit(MethodReferenceExpr mr, ClassMap classMap) {
            super.visit(mr, classMap);

            try {
                var resolve = mr.resolve();
                var qualifiedSignature = resolve.getQualifiedSignature();

                if (classMap.containsKey(qualifiedSignature)) {
                    changeList.add(new ChangeInformation(mr, qualifiedSignature));
                }
            } catch (UnsolvedSymbolException e) {
                problemList.add(new Problem<>(mr, e, fileName));
            } catch (RuntimeException e) {
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
                        try {
                            var resolvedVariable = v.resolve();
                            var vType = resolvedVariable.getType().describe();
                            var vName = resolvedVariable.getName();

                            var identifier = qualifiedClassName + " " + vType + " " + vName;
                            if (classMap.containsKey(identifier)) {
                                changeList.add(new ChangeInformation(v, identifier));
                            }
                        } catch (UnsolvedSymbolException e) {
                            problemList.add(new Problem<>(v, e, fileName));
                        }
                    });
            } catch (UnsolvedSymbolException e) {
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
                    var value = (JavaParserFieldDeclaration)resolvedFieldAccess.asField();
                    var field = value.getWrappedNode();
                    field.findCompilationUnit().ifPresent(cu -> {
                        if (!cu.containsData(Node.SYMBOL_RESOLVER_KEY)) {
                            cu.setData(Node.SYMBOL_RESOLVER_KEY, unit.getData(Node.SYMBOL_RESOLVER_KEY));
                        }
                    });

                    var resolvedField = field.resolve();
                    var scope = resolvedField.declaringType().getClassName();
                    var qualifiedClassName = resolvedField.declaringType().getQualifiedName();

                    var vType = resolvedFieldAccess.getType().describe();
                    var vName = fa.getName();

                    var identifier = qualifiedClassName + " " + vType + " " + vName;
                    if (field.isStatic())
                        changeList.add(new ChangeInformation(fa, identifier, scope, qualifiedClassName));
                    else
                        changeList.add(new ChangeInformation(fa, identifier));
                }
            } catch (UnsolvedSymbolException e) {
                problemList.add(new Problem<>(fa, e, fileName));
            }

            return fa;
        }

        @Override
        public VariableDeclarationExpr visit(VariableDeclarationExpr vd, ClassMap classMap) {
            super.visit(vd, classMap);

            vd.getVariables().forEach(v -> {
                try {
                    var container = getContainingNode(vd, MethodDeclaration.class);
                    if (container == null) return;

                    var resolvedMethod = container.resolve();
                    var qualifiedContainerName = resolvedMethod.getQualifiedSignature();

                    String vType;
                    String vName;
                    if (v.getType().toString().equals("var")) {
                        vType = "var";
                        vName = v.getNameAsString();
                    } else {
                        var resolvedVariable = v.resolve();
                        vType = resolvedVariable.getType().describe();
                        vName = resolvedVariable.getName();
                    }

                    var identifier = qualifiedContainerName + " " + vType + " " + vName;
                    if (classMap.containsKey(identifier)) {
                        changeList.add(new ChangeInformation(v, identifier));
                    }
                } catch (UnsolvedSymbolException e) {
                    problemList.add(new Problem<>(v, e, fileName));
                }
            });

            return vd;
        }

        @Override
        public NameExpr visit(NameExpr ne, ClassMap classMap) {
            super.visit(ne, classMap);

            try {
                var vType = ne.calculateResolvedType().describe();
                var vName = ne.getNameAsString();
                String qualifiedMethodSignature = "";
                String qualifiedClassName = "";

                var methodContainer = getContainingNode(ne, MethodDeclaration.class);
                if (methodContainer != null) {
                    var resolvedMethod = methodContainer.resolve();
                    qualifiedMethodSignature = resolvedMethod.getQualifiedSignature();
                }
                var classContainer = getContainingNode(ne, ClassOrInterfaceDeclaration.class);
                if (classContainer != null) {
                    var resolvedContainerClass = classContainer.resolve();
                    qualifiedClassName = resolvedContainerClass.getQualifiedName();
                }

                // Check for local variable declaration first
                if (!qualifiedMethodSignature.equals("")) {
                    var identifier = qualifiedMethodSignature + " " + vType + " " + vName;
                    if (classMap.containsKey(identifier)) {
                        changeList.add(new ChangeInformation(ne, identifier));
                        return ne;
                    }
                }
                // If none exists locally, check for field declarations
                if (!qualifiedClassName.equals("")) {
                    var identifier = qualifiedClassName + " " + vType + " " + vName;
                    if (classMap.containsKey(identifier)) {
                        changeList.add(new ChangeInformation(ne, identifier));
                    }
                }
            } catch (UnsolvedSymbolException e) {
                problemList.add(new Problem<>(ne, e, fileName));
            } catch (IllegalStateException e) {
                problemList.add(new Problem<>(ne, e, fileName));
            } catch (RuntimeException e) {
                problemList.add(new Problem<>(ne, e, fileName));
            }

            return ne;
        }

        @Override
        public Parameter visit(Parameter p, ClassMap classMap) {
            super.visit(p, classMap);

            var s= 0;

            return p;
        }

        @Override
        public EnumDeclaration visit(EnumDeclaration ed, ClassMap classMap) {
            super.visit(ed, classMap);

            var s= 0;

            return ed;
        }

        @Override
        public EnumConstantDeclaration visit(EnumConstantDeclaration ecd, ClassMap classMap) {
            super.visit(ecd, classMap);

            var s= 0;

            return ecd;
        }
    }
}
