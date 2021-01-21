package com.sim.application.obfuscation;

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
    public boolean execute(byte[] source) {
        return false;
    }
}
