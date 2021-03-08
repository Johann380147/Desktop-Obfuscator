package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.controllers.obfuscation.ObfuscateMethodController;

public class MethodObfuscation extends Technique {

    private String name = "Method Obfuscation";
    private String description = "Adds excessive overloading and splits method implementation where possible";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean execute(CompilationUnit source) { return ObfuscateMethodController.obfuscate(source);
    }
}
