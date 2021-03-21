package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.Map;

public final class TrimCodeController extends Technique {
    private static TrimCodeController instance;
    private String name = "Trimming";
    private String description = "Removes unused class, fields, methods and whitespace";

    public static TrimCodeController getInstance() {
        if (instance == null) {
            instance = new TrimCodeController();
        }
        return instance;
    }

    private TrimCodeController() {}

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
