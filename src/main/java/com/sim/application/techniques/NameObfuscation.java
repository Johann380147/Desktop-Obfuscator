package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.controllers.obfuscation.ObfuscateNameController;

public class NameObfuscation extends Technique {
    private String name = "Name Obfuscation";
    private String description = "Replaces class and variable names with random strings";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean execute(CompilationUnit source) { return ObfuscateNameController.obfuscate(source);
    }
}
