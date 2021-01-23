package com.sim.application.classes;

public class FailedTechniqueException extends Exception {

    private static final long serialVersionUID = -1701134571432144647L;

    public FailedTechniqueException(String errorMsg) { super(errorMsg); }

    public FailedTechniqueException(String errorMsg, Throwable err) {
        super(errorMsg, err);
    }
}
