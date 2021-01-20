package com.sim.application.obfuscation;

public class FlowObfuscation extends Technique {

    private String name = "Flow Obfuscation";
    private String description = "Description";

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
