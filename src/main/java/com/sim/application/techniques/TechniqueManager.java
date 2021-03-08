package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;

import java.util.Collections;
import java.util.List;


public final class TechniqueManager {
    private static final List<Technique> techniques = Collections.unmodifiableList(List.of (
            new Trimming(),
            new NameObfuscation(),
            new ConstantEncryption(),
            new FlowObfuscation(),
            new MethodObfuscation()));

    private TechniqueManager() {}

    public static List<Technique> getTechniques() {
        return techniques;
    }

    public static void run(Technique technique, CompilationUnit source) throws NullTechniqueException, FailedTechniqueException {
        if (techniques.contains(technique) == false) {
            throw new NullTechniqueException(technique.getName() + " technique was not found");
        }

        var result = technique.execute(source);
        if (result == false) {
            throw new FailedTechniqueException(technique.getName() + " failed to complete");
        }
    }
}