package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public final class DownloadObfuscatedCodeController {
    private static Stage stage;
    private static IDirectoryBrowser directory;
    private static ArrayList<JavaFile> files;

    private DownloadObfuscatedCodeController() {}

    public static void initialize(Stage stage, IDirectoryBrowser directory) {
        DownloadObfuscatedCodeController.stage = stage;
        DownloadObfuscatedCodeController.directory = directory;
    }

    public static void download() {
        try {
            if (directory == null) return;

            var root = directory.getRootDirectory();
            if (root == null) return;

            String downloadLocation = openDirectoryChooser().getAbsolutePath();
            FileUtil.saveToDisk(downloadLocation, FileUtil.createZipByteArray(root));
        } catch (IOException e) {

        }
    }

    private static File openDirectoryChooser() {
        if (stage == null) return null;
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Zip files (*.zip)", "*.zip");
        fileChooser.setInitialFileName("obfuscated");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showSaveDialog(stage);
    }
}
