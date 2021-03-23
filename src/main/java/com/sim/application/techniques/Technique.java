package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;

import java.util.List;
import java.util.Map;

public abstract class Technique {

    public abstract String getName();
    public abstract String getDescription();
    public abstract void execute(Map<JavaFile, CompilationUnit> source, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException;
}
