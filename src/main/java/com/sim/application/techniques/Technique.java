package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;

import java.util.Map;

public abstract class Technique {

    public abstract String getName();
    public abstract String getDescription();
    public abstract boolean execute(CompilationUnit source, Map<String, String> classMap);
}
