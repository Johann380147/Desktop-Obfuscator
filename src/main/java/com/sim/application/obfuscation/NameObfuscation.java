package com.sim.application.obfuscation;

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
    public boolean execute(byte[] source) {
        return false;
    }
}
