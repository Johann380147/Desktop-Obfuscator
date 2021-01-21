package com.sim.application.obfuscation;

public class FlowObfuscation extends Technique {

    private String name = "Flow Obfuscation";
    private String description = "Changes flow of loops and conditional operators";

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
