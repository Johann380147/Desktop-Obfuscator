package com.sim.application.classes;

import java.util.ArrayList;
import java.util.List;

public class ProjectFiles {
    private List<JavaFile> javaFiles;
    private List<JavaFile> sourceDirectories;
    private List<JavaFile> libraryFiles;

    public ProjectFiles() {
        this.javaFiles = new ArrayList<>();
        this.sourceDirectories = new ArrayList<>();
        this.libraryFiles = new ArrayList<>();
    }

    public ProjectFiles(List<JavaFile> javaFiles,
                        List<JavaFile> sourceDirectories,
                        List<JavaFile> libraryFiles) {
        this.javaFiles = javaFiles;
        this.sourceDirectories = sourceDirectories;
        this.libraryFiles = libraryFiles;
    }

    public void setProjectFiles(ProjectFiles projectFiles) {
        this.javaFiles = projectFiles.javaFiles;
        this.sourceDirectories = projectFiles.sourceDirectories;
        this.libraryFiles = projectFiles.libraryFiles;
    }

    public List<JavaFile> getJavaFiles() {
        return javaFiles;
    }

    public List<JavaFile> getSourceDirectories() {
        return sourceDirectories;
    }

    public List<JavaFile> getLibraryFiles() {
        return libraryFiles;
    }

    public void addJavaFiles(JavaFile file) {
        this.javaFiles.add(file);
    }

    public void addSourceDirectory(JavaFile dir) {
        this.sourceDirectories.add(dir);
    }

    public void addLibraryFile(JavaFile file) {
        this.libraryFiles.add(file);
    }
}
