package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.List;

public class ChangeConfigurationFilesController extends Technique {
    private static ChangeConfigurationFilesController instance;
    private final String name = "Include configuration files (e.g. pom.xml)";
    private final String description = "";

    public static ChangeConfigurationFilesController getInstance() {
        if (instance == null) {
            instance = new ChangeConfigurationFilesController();
        }
        return instance;
    }

    private ChangeConfigurationFilesController() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(BiMap<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {
        String currFile = "";
        try {
            // Gathering all class declarations
            for (var file : sourceFiles.keySet()) {
                var unit = sourceFiles.get(file);
                currFile = file.getFileName();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
        }
    }
}
