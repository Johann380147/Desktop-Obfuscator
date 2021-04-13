package com.sim.application.controllers;

import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.classes.Parser;
import com.sim.application.views.IMainView;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;


public final class DownloadObfuscatedCodeController {
    private static Stage stage;
    private static IDirectoryBrowser directory;
    private static IMainView mainView;

    private DownloadObfuscatedCodeController() {}

    public static void initialize(Stage stage, IDirectoryBrowser directory, IMainView mainView) {
        DownloadObfuscatedCodeController.stage = stage;
        DownloadObfuscatedCodeController.directory = directory;
        DownloadObfuscatedCodeController.mainView = mainView;
    }

    public static void download() {
        if (directory == null) return;

        var compilationUnits = Parser.getCompilationUnits();
        if (compilationUnits == null || compilationUnits.size() == 0) return;

        var chosenFolder = openDirectoryChooser(Parser.getProjectFileName());
        if (chosenFolder == null) return;

        String downloadLocation = chosenFolder.getAbsolutePath();
        LogStateController.log("Downloading obfuscated files...", Console.Status.INFO);
        mainView.disableDownloadButton();
        var download = new Download(compilationUnits, downloadLocation);
        var thread = new Thread(download);
        thread.setDaemon(true);
        thread.start();
    }
    public static class Download extends Task<Void> {

        private Collection<CompilationUnit> compilationUnits;
        private String downloadLocation;

        Download(Collection<CompilationUnit> compilationUnits, String downloadLocation) {
            this.compilationUnits = compilationUnits;
            this.downloadLocation = downloadLocation;
        }

        @Override
        public Void call() {
            try {
                for (var cu : compilationUnits) {
                    var filePath = cu.getStorage().get().getPath().toAbsolutePath().toString();
                    var tempFilePath = filePath;
                    var rootPath = Parser.getProjectDir();
                    filePath = filePath.replace(rootPath, "");
                    cu.setStorage(Paths.get(downloadLocation, filePath));
                    cu.getStorage().get().save();
                    cu.setStorage(Paths.get(tempFilePath));
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
