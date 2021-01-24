package com.sim.application.techniques;

public class NullTechniqueException extends Exception {

    private static final long serialVersionUID = 943992610278480716L;

    public NullTechniqueException(String errorMsg) { super(errorMsg); }

    public NullTechniqueException(String errorMsg, Throwable err) {
        super(errorMsg, err);
    }
}
