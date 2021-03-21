package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.classes.JavaFile;

import java.util.Map;

public abstract class Technique {

    public abstract String getName();
    public abstract String getDescription();
    public abstract void execute(Map<JavaFile, CompilationUnit> source, BiMap<String, String> classMap) throws FailedTechniqueException;
}
