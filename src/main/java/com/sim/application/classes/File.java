package com.sim.application.classes;

public class File {
    private String fileName;
    private byte[] content;

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
