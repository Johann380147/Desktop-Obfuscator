package com.sim.application.obfuscation;

public abstract class Technique {

    public abstract String getName();
    public abstract String getDescription();
    public abstract boolean execute(byte[] source);
}
