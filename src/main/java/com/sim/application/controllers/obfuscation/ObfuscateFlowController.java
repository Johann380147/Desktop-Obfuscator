package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
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
    public boolean execute(CompilationUnit source, Map<String, String> classMap) {
        return false;
    }
}
