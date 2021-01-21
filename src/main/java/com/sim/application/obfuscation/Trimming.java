package com.sim.application.obfuscation;

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
    public boolean execute(byte[] source) {
        return false;
    }
}
