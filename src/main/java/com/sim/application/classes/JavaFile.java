package com.sim.application.classes;

import com.github.javaparser.ast.CompilationUnit;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class JavaFile {
    private boolean isDirectory;
    private String fileName;
    private String fullPath;
    private String relativePath;
    private String content;
    private String obfuscatedContent;
    private int inputPos = 0;
    private int outputPos = 0;

    public JavaFile(String rootPath, File file, String content) {
        this.isDirectory = file.isDirectory();
        this.content = content;
        this.fullPath = file.getAbsolutePath();
        setFileName(file.getAbsolutePath());
        setRelativePath(rootPath, file.getAbsolutePath());
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }

    public String getObfuscatedContent() {
        return obfuscatedContent;
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
        return isDirectory;
    }

    private void setFileName(String fullPath) {
        String[] tmp = fullPath.split("/|\\\\");
        if (tmp.length > 0) {
            this.fileName = tmp[tmp.length - 1];
        }
        else {
            this.fileName = "";
        }
    }

    private void setRelativePath(String rootPath, String filePath) {
        String[] tmp = rootPath.split("/|\\\\");
        if (tmp.length > 0) {
            relativePath = tmp[tmp.length - 1];
            relativePath += filePath.replace(rootPath, "");
        }
    }

    public void setObfuscatedContent(String obfuscatedContent) {
        this.obfuscatedContent = obfuscatedContent;
    }
}
