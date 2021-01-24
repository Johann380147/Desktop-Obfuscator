package com.sim.application.techniques;

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
    public byte[] execute(byte[] source) {
        return TrimCodeController.obfuscate(source);
    }
}
