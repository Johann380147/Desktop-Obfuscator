package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.controllers.obfuscation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public final class TechniqueManager {
    private static final List<Technique> techniques = Collections.unmodifiableList(List.of (
            TrimCodeController.getInstance(),
            ObfuscateNameController.getInstance(),
            ObfuscateConstantController.getInstance(),
            ObfuscateMethodController.getInstance(),
            ObfuscateFlowController.getInstance()));

    private TechniqueManager() {}

    public static List<Technique> getTechniques() {
        return techniques;
    }

    private static void run(Technique technique, Map<JavaFile, CompilationUnit> source, BiMap<String, String> classMap, List<Problem> problems) throws FailedTechniqueException {
        technique.execute(source, classMap, problems);
    }

    public static void run(List<Technique> techniques, Map<JavaFile, CompilationUnit> sourceFiles) throws FailedTechniqueException {
        HashBiMap<String, String> classMap = HashBiMap.create();
        List<Problem> problems = new ArrayList<>();

        // Defining the order of the obfuscation methods
        if (techniques.contains(TrimCodeController.getInstance())) {
            run(TrimCodeController.getInstance(), sourceFiles, classMap, problems);
        }
        if (techniques.contains(ObfuscateNameController.getInstance())) {
            run(ObfuscateNameController.getInstance(), sourceFiles, classMap, problems);
        }
        if (techniques.contains(ObfuscateConstantController.getInstance())) {
            run(ObfuscateConstantController.getInstance(), sourceFiles, classMap, problems);
        }
        if (techniques.contains(ObfuscateMethodController.getInstance())) {
            run(ObfuscateMethodController.getInstance(), sourceFiles, classMap, problems);
        }
        if (techniques.contains(ObfuscateFlowController.getInstance())) {
            run(ObfuscateFlowController.getInstance(), sourceFiles, classMap, problems);
        }

        saveObfuscatedContent(sourceFiles);
    }

    private static void saveObfuscatedContent(Map<JavaFile, CompilationUnit> sourceFiles) {
        for (JavaFile file : sourceFiles.keySet()) {
            var obfuscatedContent = sourceFiles.get(file);
            file.setObfuscatedContent(obfuscatedContent.toString().getBytes());
        }
    }
}