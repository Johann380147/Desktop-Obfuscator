package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.List;
import java.util.Map;

public final class ObfuscateConstantController extends Technique {
    private static ObfuscateConstantController instance;
    private String name = "Encrypt Constants";
    private String description = "Encrypts constants using md5";

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
    public void execute(Map<JavaFile, CompilationUnit> source, BiMap<String, String> classMap, List<Problem> problems) throws FailedTechniqueException {

    }
}
