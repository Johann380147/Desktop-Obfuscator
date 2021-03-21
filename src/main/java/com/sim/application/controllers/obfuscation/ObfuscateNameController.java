package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.google.common.collect.BiMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.utils.StringUtil;
import javafx.util.Pair;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public final class ObfuscateNameController extends Technique {
    private static ObfuscateNameController instance;
    private String name = "Name Obfuscation";
    private String description = "Replaces class and variable names with random strings";

    private final int MAX_NAME_LENGTH = 10;

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
    public void execute(Map<JavaFile, CompilationUnit> source, BiMap<String, String> classMap) throws FailedTechniqueException {

        String currFile = "";
        try {
            // Gathering all class declarations
            for (var file : source.keySet()) {
                var unit = source.get(file);
                currFile = file.getFileName();
                findClassOrInterfaceDeclarations(unit, classMap);
            }

            for (var file : source.keySet()) {
                // Gather all instances that need to be changed
                var changeList = new ArrayList<Pair<Node, String>>();
                var unit = source.get(file);
                currFile = file.getFileName();
                var visitor = new NameVisitor(unit, changeList);
                visitor.visit(unit, classMap);

                // Make the changes on all instances
                for (var pair : changeList) {
                    var node = pair.getKey();
                    var resolvedName = pair.getValue();
                    if (node instanceof ClassOrInterfaceDeclaration) {
                        var type = (ClassOrInterfaceDeclaration)node;
                        var newName = classNameBuilder(type.getNameAsString(), classMap.get(resolvedName));
                        type.setName(newName);
                    } else if (node instanceof ClassOrInterfaceType) {
                        var type = (ClassOrInterfaceType)node;
                        var newName = classNameBuilder(type.getNameWithScope(), classMap.get(resolvedName));
                        type.setName(newName);
                    }
                }
            }
        } catch (Exception e) {
            throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
        }
    }


    private void findClassOrInterfaceDeclarations(CompilationUnit source, BiMap<String, String> classMap) {
        var file = new Object() { boolean isRenamed = false; };

        source.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(c -> {
                    String oldName;
                    if (c.getFullyQualifiedName().isPresent())
                        oldName = c.getFullyQualifiedName().get();
                    else if (source.getPackageDeclaration().isPresent())
                        oldName = source.getPackageDeclaration().get() + c.getNameAsString();
                    else
                        oldName = c.getNameAsString();

                    String packageName = oldName.replace(c.getNameAsString(), "");
                    String newName="";
                    if (classMap.containsKey(oldName))
                        newName = classMap.get(oldName);
                    else {
                        newName = StringUtil.randomString(MAX_NAME_LENGTH);
                        while (classMap.containsValue(packageName + newName)) {
                            newName = StringUtil.randomString(MAX_NAME_LENGTH);
                        }
                    }

                    classMap.put(oldName, packageName + newName);
                    if(!c.isNestedType() && !file.isRenamed) {
                        source.setStorage(Paths.get(source.getStorage().get().getDirectory().toString(), newName + ".java"));
                        file.isRenamed = true;
                    }
                });
    }

    private String classNameBuilder(String oldName, String newName) {
        var arr = newName.split("\\.");
        var changedName = arr[arr.length - 1];
        var index = oldName.lastIndexOf(".");
        String packageName = "";
        if (index != -1) {
            packageName = oldName.substring(0, index + 1);
        }

        return packageName + changedName;
    }

    private boolean isNestedClass(ClassOrInterfaceDeclaration nestedClass) {
        return isNestedClass(nestedClass, null);
    }

    private boolean isNestedClass(ClassOrInterfaceDeclaration nestedClass, Node parent) {
        Node parentNode;
        if (parent == null) {
            parentNode = nestedClass;
        } else {
            parentNode = parent;
        }

        if (parentNode.getParentNode().isPresent()) {
            if (parentNode.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {
                return true;
            } else {
                return isNestedClass(nestedClass, parentNode.getParentNode().get());
            }
        } else {
            return false;
        }
    }

    private static class NameVisitor extends ModifierVisitor<BiMap<String, String>> {

        private CompilationUnit unit;
        private ArrayList<Pair<Node, String>> changeList;

        private NameVisitor(CompilationUnit unit, ArrayList<Pair<Node, String>> changeList) {
            this.unit = unit;
            this.changeList= changeList;
        }

        @Override
        public ImportDeclaration visit(ImportDeclaration id, BiMap<String, String> classMap) {
            super.visit(id, classMap);

            if (classMap.containsKey(id.getNameAsString())) {
                id.setName(classMap.get(id.getNameAsString()));
            }

            return id;
        }

        @Override
        public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration id, BiMap<String, String> classMap) {
            super.visit(id, classMap);

            String name = "";
            if (id.getFullyQualifiedName().isPresent()) {
                name = id.getFullyQualifiedName().get();
            } else {
                name = id.getNameAsString();
            }

            if (classMap.containsKey(id.getFullyQualifiedName().get())) {
                changeList.add(new Pair<>(id, name));
            }

            return id;
        }

        @Override
        public ClassOrInterfaceType visit(ClassOrInterfaceType cit, BiMap<String, String> classMap) {
            super.visit(cit, classMap);

            if (cit.getNameAsString().equals("var") ||
                cit.getNameWithScope().equals("HashMap.Entry")) {
                    return cit;
            }

            String qualifiedName = cit.getNameWithScope();
            try {
                qualifiedName = cit.resolve().getQualifiedName();
            } catch(UnsupportedOperationException e) {
                //TODO: let user handle whether they want to change?
            } catch (UnsolvedSymbolException e) {
                //TODO: let user handle whether they want to change?
            }

            if (classMap.containsKey(qualifiedName)) {
                changeList.add(new Pair<>(cit, qualifiedName));
            }

            return cit;
        }

        @Override
        public MethodDeclaration visit(MethodDeclaration md, BiMap<String, String> classMap) {
            super.visit(md, classMap);
            return md;
        }
    }
}
