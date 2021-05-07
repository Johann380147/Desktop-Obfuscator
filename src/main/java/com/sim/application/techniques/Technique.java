package com.sim.application.techniques;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.entities.JavaFile;

import java.util.List;

public abstract class Technique {

    public abstract String getName();
    public abstract String getDescription();
    public abstract void execute(BiMap<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException;
    protected void saveRawContent(BiMap<JavaFile, String> rawOutput) {
        for (JavaFile file : rawOutput.keySet()) {
            var obfuscatedContent = rawOutput.get(file);
            file.setObfuscatedContent(obfuscatedContent);
        }
    }
    protected void saveContent(BiMap<JavaFile, CompilationUnit> sourceFiles) {
        for (JavaFile file : sourceFiles.keySet()) {
            var obfuscatedContent = sourceFiles.get(file);
            file.setObfuscatedContent(obfuscatedContent.toString());
        }
    }
}
