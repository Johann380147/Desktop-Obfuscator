package com.sim.application.controllers;

import com.sim.application.classes.File;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.NullTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.techniques.TechniqueManager;
import com.sim.application.views.MainView;
import com.sim.application.views.components.Console;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

public final class ObfuscateCodeController {

    private static List<File> files;

    private ObfuscateCodeController() {}

    public static void obfuscate(List<Technique> techniques) {
        var directory = MainView.getView().getDirectory().getRoot();
        if (directory == null) return;

        files = new ArrayList<>();
        for (TreeItem<File> node : directory.getChildren()) {
            getFiles(node);
        }

        // Process all files then apply 1 technique at a time to all files
        // Obfuscate (button) -> ObfuscateCodeController -> TechniqueManager (translate) -> NameObfuscation -> ObfuscateNameController
        processFiles(files);

        try {
            TechniqueManager.run(techniques, files);
        }
        catch (NullTechniqueException e) {
            LogStateController.log(e.getMessage(), Console.Status.ERROR);
        }
        catch (FailedTechniqueException e) {
            LogStateController.log(e.getFileName() + ": " + e.getMessage(), Console.Status.ERROR);
        }

    }

    private static void processFiles(List<File> files) {
        // TODO: Analyze & understand structure of class, methods, variables, etc
    }

    private static void getFiles(TreeItem<File> node) {
        if (node != null && node.getValue() != null) {
            if (node.getValue().isFolder()) {
                for (TreeItem<File> child : node.getChildren()) {
                    getFiles(child);
                }
            }
            else {
             files.add(node.getValue());
            }
        }
    }
}
