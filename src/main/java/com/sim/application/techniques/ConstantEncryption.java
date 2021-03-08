package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.controllers.obfuscation.ObfuscateConstantController;

public class ConstantEncryption extends Technique {

    private String name = "Encrypt Constants";
    private String description = "";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean execute(CompilationUnit source) { return ObfuscateConstantController.obfuscate(source);
    }
}
