package com.sim.application.controllers;

import com.sim.application.classes.Parser;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;


public final class DownloadObfuscatedCodeController {
    private static Stage stage;
    private static IDirectoryBrowser directory;

    private DownloadObfuscatedCodeController() {}

    public static void initialize(Stage stage, IDirectoryBrowser directory) {
        DownloadObfuscatedCodeController.stage = stage;
        DownloadObfuscatedCodeController.directory = directory;
    }

    public static void download() {
        if (directory == null) return;

        var compilationUnits = Parser.getCompilationUnits();
        if (compilationUnits == null || compilationUnits.size() == 0) return;

        var chosenFolder = openDirectoryChooser(Parser.getProjectFileName());
        if (chosenFolder == null) return;

        String downloadLocation = chosenFolder.getAbsolutePath();
        LogStateController.log("Downloading obfuscated files...", Console.Status.INFO);

        for (var cu : compilationUnits) {
            var filePath = cu.getStorage().get().getPath().toAbsolutePath().toString();
            var rootPath = Parser.getProjectDir();
            filePath = filePath.replace(rootPath, "");
            cu.setStorage(Paths.get(downloadLocation, filePath));
            cu.getStorage().get().save();
        }

        LogStateController.log("Downloaded to " + downloadLocation, Console.Status.INFO);
    }

    private static File openDirectoryChooser(String root) {
        if (stage == null) return null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(root);
        return fileChooser.showSaveDialog(stage);
    }
}
