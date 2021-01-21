package com.sim.application.obfuscation;

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
    public boolean execute(byte[] source) {
        return false;
    }
}
