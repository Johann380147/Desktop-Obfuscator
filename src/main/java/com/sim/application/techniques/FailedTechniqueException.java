package com.sim.application.techniques;

public class FailedTechniqueException extends Exception {

    private static final long serialVersionUID = -1701134571432144647L;
    private String fileName;

    public FailedTechniqueException(String errorMsg) { super(errorMsg); }

    public FailedTechniqueException(String errorMsg, Throwable err) {
        super(errorMsg, err);
    }

    public FailedTechniqueException setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }
}
