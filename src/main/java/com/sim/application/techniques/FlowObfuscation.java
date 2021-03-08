package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.controllers.obfuscation.ObfuscateFlowController;

public class FlowObfuscation extends Technique {

    private String name = "Flow Obfuscation";
    private String description = "Changes flow of loops and conditional operators";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean execute(CompilationUnit source) { return ObfuscateFlowController.obfuscate(source);
    }
}
