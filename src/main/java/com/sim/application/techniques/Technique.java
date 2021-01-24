package com.sim.application.techniques;

public abstract class Technique {

    public abstract String getName();
    public abstract String getDescription();
    public abstract byte[] execute(byte[] source);
}
