package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.controllers.obfuscation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


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

    private static void run(Technique technique, Map<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList, Consumer<Technique> successCallback) throws FailedTechniqueException {
        technique.execute(sourceFiles, classMap, problemList);
        successCallback.accept(technique);
    }

    public static void run(List<Technique> techniques, Map<JavaFile, CompilationUnit> sourceFiles, Consumer<Technique> successCallback) throws FailedTechniqueException {
        ClassMap classMap = new ClassMap();
        List<Problem> problemList = new ArrayList<>();

        // Defining the order of the obfuscation methods
        if (techniques.contains(TrimCodeController.getInstance())) {
            run(TrimCodeController.getInstance(), sourceFiles, classMap, problemList, successCallback);
        }
        if (techniques.contains(ObfuscateNameController.getInstance())) {
            run(ObfuscateNameController.getInstance(), sourceFiles, classMap, problemList, successCallback);
        }
        if (techniques.contains(ObfuscateConstantController.getInstance())) {
            run(ObfuscateConstantController.getInstance(), sourceFiles, classMap, problemList, successCallback);
        }
        if (techniques.contains(ObfuscateMethodController.getInstance())) {
            run(ObfuscateMethodController.getInstance(), sourceFiles, classMap, problemList, successCallback);
        }
        if (techniques.contains(ObfuscateFlowController.getInstance())) {
            run(ObfuscateFlowController.getInstance(), sourceFiles, classMap, problemList, successCallback);
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