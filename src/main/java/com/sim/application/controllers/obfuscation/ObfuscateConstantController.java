package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.techniques.Technique;

import java.util.Map;

public final class ObfuscateConstantController extends Technique {
    private static ObfuscateConstantController instance;
    private String name = "Encrypt Constants";
    private String description = "";

    public static ObfuscateConstantController getInstance() {
        if (instance == null) {
            instance = new ObfuscateConstantController();
        }
        return instance;
    }

    private ObfuscateConstantController() {}

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
