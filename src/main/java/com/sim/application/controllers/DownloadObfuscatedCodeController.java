package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Parser;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.IMainView;
import com.sim.application.views.components.IConsole;
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

        LogStateController.log("Downloading obfuscated files...", IConsole.Status.INFO);
        mainView.disableDownloadButton();
        var download = new Download(directory.getProjectFiles(), chosenFolder);
        var thread = new Thread(download);
        thread.setDaemon(true);
        thread.start();
    }
    public static class Download extends Task<Void> {

        private List<JavaFile> javaFiles;
        private File chosenFolder;
        private String downloadLocation;

        Download(List<JavaFile> javaFiles, File chosenFolder) {
            this.javaFiles = javaFiles;
            this.chosenFolder = chosenFolder;
            this.downloadLocation = chosenFolder.getAbsolutePath();
        }

        @Override
        public Void call() {
            try {
                int errorCount = 0;
                StringBuilder filesWithError = new StringBuilder();
                var rootPath = Parser.getProjectDir();

                // Save obfuscated files
                for (var file : javaFiles) {
                    var filePath = file.getNewFullPath();
                    filePath = filePath.replace(rootPath, "");
                    var result = FileUtil.saveToDisk(Paths.get(downloadLocation, filePath).toString(), file.getObfuscatedContent());
                    if (!result) {
                        filesWithError.append("\n").append(file.getFullPath());
                        errorCount++;
                    }
                }

                // Copy non-java files from previous folder
                File rootDir = new File(rootPath);
                File[] fileList = rootDir.listFiles();
                for (File file : fileList) {
                    copyFiles(file, rootPath, downloadLocation);
                }
                Platform.runLater(() -> LogStateController.log("Downloaded to " + downloadLocation, IConsole.Status.INFO));
                if (errorCount > 0) {
                    final int count = errorCount;
                    Platform.runLater(() -> LogStateController.log(count + " file(s) failed to download:" + filesWithError.toString(), IConsole.Status.WARNING));
                }
            } catch(Throwable e) {
                e.printStackTrace();
                Platform.runLater(() -> LogStateController.log("Download failed: " + e.getMessage(), IConsole.Status.INFO));
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

    private static void copyFiles(File file, String rootPath, String downloadLocation) {
        if (file == null) return;
        var filePath = file.getAbsolutePath();
        filePath = filePath.replace(rootPath, "");
        var newFilePath = Paths.get(downloadLocation, filePath);

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                copyFiles(child, rootPath, downloadLocation);
            }
        } else if (!"java".equals(FileUtil.getFileExt(file.toPath()))) {
            FileUtil.saveToDisk(newFilePath, file);
        }
    }
}

