package com.sim.application.techniques;

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
    public byte[] execute(byte[] source) {
        return ObfuscateNameController.obfuscate(source);
    }
}
