package com.sim.application.classes;

import com.github.javaparser.ast.CompilationUnit;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class JavaFile {
    private File file;
    private String fileName;
    private String fullPath;
    private String relativePath;
    private byte[] content;
    private byte[] obfuscatedContent;
    private int inputPos = 0;
    private int outputPos = 0;

    private StringProperty name = new SimpleStringProperty();

    public JavaFile(String rootPath, File file, byte[] content) {
        this.file = file;
        this.content = content;
        this.fullPath = file.getAbsolutePath();
        setFileName(file.getAbsolutePath());
        setRelativePath(rootPath, file.getAbsolutePath());
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public byte[] getObfuscatedContent() {
        return obfuscatedContent;
    }

    public File getFile() {
        return file;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public int getInputPos() {
        return inputPos;
    }

    public int getOutputPos() {
        return outputPos;
    }

    public void setInputPos(int pos) {
        inputPos = pos;
    }

    public void setOutputPos(int pos) {
        outputPos = pos;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    private void setFileName(String fullPath) {
        String[] tmp = fullPath.split("/|\\\\");
        if (tmp.length > 0) {
            this.fileName = tmp[tmp.length - 1];
        }
        else {
            this.fileName = "";
        }
        name.set(fileName);
    }

    private void setRelativePath(String rootPath, String filePath) {
        String[] tmp = rootPath.split("/|\\\\");
        if (tmp.length > 0) {
            String rootName = tmp[tmp.length - 1];
            relativePath = rootName;
            relativePath += filePath.replace(rootPath, "");
        }
    }

    public void setObfuscatedContent(byte[] obfuscatedContent) {
        this.obfuscatedContent = obfuscatedContent;
    }

    public StringProperty nameProperty() {
        return name;
    }
}
