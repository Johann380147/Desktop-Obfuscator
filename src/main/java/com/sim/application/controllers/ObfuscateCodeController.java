package com.sim.application.controllers;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.classes.JavaFile;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.NullTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.techniques.TechniqueManager;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.scene.control.TreeItem;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ObfuscateCodeController {

    private static IDirectoryBrowser directory;
    private static List<JavaFile> files;

    private ObfuscateCodeController() {}

    public static void initialize(IDirectoryBrowser directory) { ObfuscateCodeController.directory = directory; }

    public static void obfuscate(List<Technique> techniques) {
        if (directory == null) return;

        var root = directory.getRootDirectory();
        if (root == null) return;

        files = new ArrayList<>();
        for (TreeItem<JavaFile> node : root.getChildren()) {
            getFiles(node);
        }

        // Process 1 technique at a time to all files
        // Obfuscate (button) -> ObfuscateCodeController -> TechniqueManager (translate) -> NameObfuscation -> ObfuscateNameController

        var currFile = "";
        var compilationMap = new HashMap<JavaFile, CompilationUnit>();
        try {
            for (Technique technique : techniques) {
                for (JavaFile file : files) {
                    currFile = file.getFileName();
                    CompilationUnit compilationUnit;
                    if (compilationMap.containsKey(file)) {
                        compilationUnit = compilationMap.get(file);
                    } else {
                        compilationUnit = StaticJavaParser.parse(file.getFile());
                        compilationMap.put(file, compilationUnit);
                    }

                    TechniqueManager.run(technique, compilationUnit);
                    file.setObfuscatedContent(compilationUnit.toString().getBytes());
                }
            }
        } catch (FileNotFoundException e) {
            LogStateController.log(e.getMessage(), Console.Status.ERROR);
        }
        catch (NullTechniqueException e) {
            LogStateController.log(e.getMessage(), Console.Status.ERROR);
        } catch (FailedTechniqueException e) {
            LogStateController.log(currFile + ": " + e.getMessage(), Console.Status.ERROR);
        }
        DisplayObfuscatedCodeController.DisplayCode(directory.getCurrentSelection());
    }

    private static void getFiles(TreeItem<JavaFile> node) {
        if (node != null && node.getValue() != null) {
            if (node.getValue().isDirectory()) {
                for (TreeItem<JavaFile> child : node.getChildren()) {
                    getFiles(child);
                }
            }
            else {
             files.add(node.getValue());
            }
        }
    }
}
