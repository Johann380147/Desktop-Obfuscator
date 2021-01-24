package com.sim.application.controllers;

import com.sim.application.classes.File;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.NullTechniqueException;
import com.sim.application.techniques.ObfuscationManager;
import com.sim.application.views.MainView;
import com.sim.application.views.components.Console;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

public final class ObfuscateCodeController {

    private static List<File> files;

    private ObfuscateCodeController() {}

    public static void obfuscate(List<String> techniques) {
        var directory = MainView.getView().getDirectory().getRoot();
        if (directory == null) return;

        files = new ArrayList<>();
        for (TreeItem<File> node : directory.getChildren()) {
            getFiles(techniques, node);
        }

        // Process all files then apply 1 technique at a time to all files
        // Obfuscate (button) -> ObfuscateCodeController -> ObfuscationManager (translate) -> NameObfuscation -> ObfuscateNameController
        processFiles(files);
        for (String technique : techniques) {
            for (File file : files) {
                try {
                    file.setObfuscatedContent(ObfuscationManager.run(technique, file.getContent()));
                }
                catch (NullTechniqueException e) {
                    LogStateController.log(e.getMessage(), Console.Status.ERROR);
                }
                catch (FailedTechniqueException e) {
                    LogStateController.log(file.getFileName() + ": " + e.getMessage(), Console.Status.ERROR);
                }
            }
        }
    }

    private static void processFiles(List<File> files) {
        // TODO: Analyze & understand structure of class, methods, variables, etc
    }

    private static void getFiles(List<String> techniques, TreeItem<File> node) {
        if (node != null && node.getValue() != null) {
            if (node.getValue().isFolder()) {
                for (TreeItem<File> child : node.getChildren()) {
                    getFiles(techniques, child);
                }
            }
            else {
             files.add(node.getValue());
            }
        }
    }
}
