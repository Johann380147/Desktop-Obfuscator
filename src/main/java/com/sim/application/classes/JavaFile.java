package com.sim.application.classes;

import java.io.File;

public class JavaFile {
    private boolean isDirectory;
    private boolean isAddedPostObfuscation;
    private String fileName;
    private String fullPath;
    private String rootPath;
    private String relativePath;
    private String newFullPath;
    private String content;
    private String obfuscatedContent;
    private int inputPos = 0;
    private int outputPos = 0;

    public JavaFile(String rootPath, String fullPath, String content, boolean isAddedPostObfuscation) {
        if (content == null) {
            this.isDirectory = true;
        }
        this.content = content;
        this.fullPath = fullPath;
        this.newFullPath = fullPath;
        this.rootPath = rootPath;
        setFileName(fullPath);
        setRelativePath(rootPath, fullPath);
        this.isAddedPostObfuscation = isAddedPostObfuscation;
    }

    public JavaFile(String rootPath, File file, String content) {
        this.isDirectory = file.isDirectory();
        this.content = content;
        this.fullPath = file.getAbsolutePath();
        this.rootPath = rootPath;
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

    public String getNewFullPath() {
        return newFullPath;
    }

    public String getRootPath() {
        return rootPath;
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

    public boolean isAddedPostObfuscation() {
        return isAddedPostObfuscation;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
        setFileName(fullPath);
    }

    public void setNewFullPath(String newFullPath) {
        this.newFullPath = newFullPath;
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

    public void setContent(String content) {
        this.content = content;
    }

    public void setObfuscatedContent(String obfuscatedContent) {
        this.obfuscatedContent = obfuscatedContent;
    }
}
