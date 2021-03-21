package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.Map;

public final class ObfuscateFlowController extends Technique {
    private static ObfuscateFlowController instance;
    private String name = "Flow Obfuscation";
    private String description = "Changes flow of loops and conditional operators";

    public static ObfuscateFlowController getInstance() {
        if (instance == null) {
            instance = new ObfuscateFlowController();
        }
        return instance;
    }

    private ObfuscateFlowController() {}

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
