package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.List;

public final class ObfuscateFlowController extends Technique {
    private static ObfuscateFlowController instance;
    private final String name = "Flow Obfuscation";
    private final String description = "Changes flow of loops and conditional operators";

    public static ObfuscateFlowController getInstance() {
        if (instance == null) {
            instance = new ObfuscateFlowController();
        }
        return instance;
    }

    private ObfuscateFlowController() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(BiMap<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {
        InsertMethodVariableController IFV = new InsertMethodVariableController();
        IFV.updateContents(sourceFiles);
    }

}
