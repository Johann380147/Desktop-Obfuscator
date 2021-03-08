package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.controllers.obfuscation.TrimCodeController;

public class Trimming extends Technique {

    private String name = "Trimming";
    private String description = "Removes unused class, fields, methods and whitespace";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean execute(CompilationUnit source) {
        return TrimCodeController.obfuscate(source);
    }
}
