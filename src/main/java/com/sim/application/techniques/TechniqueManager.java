package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.controllers.obfuscation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public final class TechniqueManager {
    private static final List<Technique> techniques = Collections.unmodifiableList(List.of (
            TrimCodeController.getInstance(),
            ObfuscateFlowController.getInstance(),
            ObfuscateMethodController.getInstance(),
            ObfuscateNameController.getInstance(),
            ObfuscateConstantController.getInstance()));

    private TechniqueManager() {}

    public static List<Technique> getTechniques() {
        return techniques;
    }

    public static void run(Technique technique, CompilationUnit source, Map<String, String> classMap) throws NullTechniqueException, FailedTechniqueException {
        if (techniques.contains(technique) == false) {
            throw new NullTechniqueException(technique.getName() + " technique was not found");
        }

        var result = technique.execute(source, classMap);
        if (result == false) {
            throw new FailedTechniqueException(technique.getName() + " failed to complete");
        }
    }
}