package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.Map;

public final class ObfuscateMethodController extends Technique {
    private static ObfuscateMethodController instance;
    private String name = "Method Obfuscation";
    private String description = "Adds excessive overloading and splits method implementation where possible";

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
    public void execute(Map<JavaFile, CompilationUnit> source, BiMap<String, String> classMap) throws FailedTechniqueException {

    }
}
