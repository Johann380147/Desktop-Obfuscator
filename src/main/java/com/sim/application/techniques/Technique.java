package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;

import java.util.List;

public abstract class Technique {

    public abstract String getName();
    public abstract String getDescription();
    public abstract void execute(BiMap<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException;
}
