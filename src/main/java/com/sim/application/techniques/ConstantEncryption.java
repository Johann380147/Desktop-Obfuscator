package com.sim.application.techniques;

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
    public byte[] execute(byte[] source) {
        return ObfuscateConstantController.obfuscate(source);
    }
}
