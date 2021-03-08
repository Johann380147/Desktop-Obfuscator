package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;

public abstract class Technique {

    public abstract String getName();
    public abstract String getDescription();
    public abstract boolean execute(CompilationUnit source);
}
