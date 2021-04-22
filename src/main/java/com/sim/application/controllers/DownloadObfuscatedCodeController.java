package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Parser;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.IMainView;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;


public final class DownloadObfuscatedCodeController {
    private static Stage stage;
    private static IDirectoryBrowser directory;
    private static IMainView mainView;

    private DownloadObfuscatedCodeController() {}

    public static void initialize(Stage stage, IMainView mainView, IDirectoryBrowser directory) {
        DownloadObfuscatedCodeController.stage = stage;
        DownloadObfuscatedCodeController.mainView = mainView;
        DownloadObfuscatedCodeController.directory = directory;
    }

    public static void download() {
        if (directory == null) return;

        var compilationUnits = Parser.getCompilationUnits();
        if (compilationUnits == null || compilationUnits.size() == 0 || !JavaFile.isProjectObfuscated()) return;

        var chosenFolder = openDirectoryChooser(Parser.getProjectFileName());
        if (chosenFolder == null) return;

        String downloadLocation = chosenFolder.getAbsolutePath();
        LogStateController.log("Downloading obfuscated files...", Console.Status.INFO);
        mainView.disableDownloadButton();
        var download = new Download(directory.getProjectFiles(), downloadLocation);
        var thread = new Thread(download);
        thread.setDaemon(true);
        thread.start();
    }
    public static class Download extends Task<Void> {

        private List<JavaFile> javaFiles;
        private String downloadLocation;

        Download(List<JavaFile> javaFiles, String downloadLocation) {
            this.javaFiles = javaFiles;
            this.downloadLocation = downloadLocation;
        }

        @Override
        public Void call() {
            try {
                for (var file : javaFiles) {
                    var filePath = file.getNewFullPath();
                    var rootPath = Parser.getProjectDir();
                    filePath = filePath.replace(rootPath, "");
                    FileUtil.saveToDisk(Paths.get(downloadLocation, filePath).toString(), file.getObfuscatedContent());
                }
                Platform.runLater(() -> LogStateController.log("Downloaded to " + downloadLocation, Console.Status.INFO));
            } finally {
                Platform.runLater(() -> mainView.enableDownloadButton());
            }
            return null;
        }
    }

    private static File openDirectoryChooser(String root) {
        if (stage == null) return null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(root);
        return fileChooser.showSaveDialog(stage);
    }
}
