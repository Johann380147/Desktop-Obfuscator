package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.sim.application.techniques.Technique;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

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
    public boolean execute(CompilationUnit source, Map<String, String> classMap) {
        source.findAll(ClassOrInterfaceDeclaration.class).stream()
                .forEach(c -> {
                    String oldName;
                    if (c.getFullyQualifiedName().isPresent())
                        oldName = c.getFullyQualifiedName().get();
                    else if (source.getPackageDeclaration().isPresent())
                        oldName = source.getPackageDeclaration().get() + c.getNameAsString();
                    else
                        oldName = c.getNameAsString();

                    String packageName = oldName.replace(c.getNameAsString(), "");
                    String newName;
                    if (classMap.containsKey(oldName))
                        newName = classMap.get(oldName);
                    else {
                        newName = randomChar();
                        while (classMap.values().contains(packageName + newName)) {
                            newName = randomChar();
                        }
                    }

                    c.setName(newName);
                    classMap.put(oldName, packageName + newName);
                    if(!c.isInnerClass()) {
                        source.setStorage(Paths.get(source.getStorage().get().getDirectory().toString(), newName + ".java"));
                    }
                });
        source.findAll(MethodDeclaration.class).stream()
                .forEach(m -> m.getParameters()
                        .forEach(p -> p.getType().ifClassOrInterfaceType(c -> {
                            var resolvedClass = c.resolve();
                            if (classMap.containsKey(resolvedClass.getQualifiedName())) {
                                var str = classMap.get(resolvedClass.getQualifiedName()).split("\\.");
                                String newName = str.length > 0 ? str[str.length - 1] : resolvedClass.getQualifiedName();
                                c.setName(newName);
                            }
                        })));
        return true;
    }

    private String randomChar() {
        String str = "";

        int numOfChars = new Random().nextInt(10 - 1) + 1;
        while (str.length() < numOfChars) {
            str += (char)(new Random().nextInt(26) + 'a');
        }

        return str;
    }
}
