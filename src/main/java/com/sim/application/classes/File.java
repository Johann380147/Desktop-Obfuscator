package com.sim.application.classes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class File {
    private String fileName;
    private String fullPath;
    private byte[] content;
    private boolean isFolder;

    private StringProperty name = new SimpleStringProperty();

    public File() {}
    public File(String fullPath, byte[] content, boolean isFolder) {
        setFullPath(fullPath);
        this.content = content;
        this.isFolder = isFolder;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public byte[] getContent() {
        return content;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
        String[] tmp = fullPath.split("/|\\\\");
        if (tmp.length > 0) {
            this.fileName = tmp[tmp.length - 1];
        }
        else {
            this.fileName = "";
        }
        name.set(fileName);
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public StringProperty nameProperty() {
        return name;
    }
}
