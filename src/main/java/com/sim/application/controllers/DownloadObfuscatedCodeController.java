package com.sim.application.controllers;

import com.sim.application.entities.JavaFile;
import com.sim.application.parsers.JParser;
import com.sim.application.parsers.TextParser;
import com.sim.application.parsers.XmlParser;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.IMainView;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.DirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;


public final class DownloadObfuscatedCodeController {
    private static Stage stage;
    private static DirectoryBrowser directory;
    private static IMainView mainView;

    private DownloadObfuscatedCodeController() {}

    public static void initialize(Stage stage, IMainView mainView, DirectoryBrowser directory) {
        DownloadObfuscatedCodeController.stage = stage;
        DownloadObfuscatedCodeController.mainView = mainView;
        DownloadObfuscatedCodeController.directory = directory;
    }

    public static void download() {
        if (directory == null) return;

        var javaFiles = directory.getProjectFiles();
        if (javaFiles == null || javaFiles.size() == 0 || !JavaFile.isProjectObfuscated()) return;

        var chosenFolder = openDirectoryChooser(JParser.getProjectFileName());
        if (chosenFolder == null) return;

        LogStateController.log("Downloading obfuscated files...", Console.Status.INFO);
        mainView.disableDownloadButton();
        var download = new Download(javaFiles, chosenFolder);
        var thread = new Thread(download);
        thread.setDaemon(true);
        thread.start();
    }

    private static File openDirectoryChooser(String root) {
        if (stage == null) return null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(root);
        return fileChooser.showSaveDialog(stage);
    }

    public static class Download extends Task<Void> {

        private List<JavaFile> javaFiles;
        private String downloadLocation;

        Download(List<JavaFile> javaFiles, File chosenFolder) {
            this.javaFiles = javaFiles;
            this.downloadLocation = chosenFolder.getAbsolutePath();
        }

        @Override
        public Void call() {
            try {
                int errorCount = 0;
                StringBuilder filesWithError = new StringBuilder();
                var rootPath = JParser.getProjectDir();
                if (rootPath == null) return null;

                // Save obfuscated files
                for (var file : javaFiles) {
                    var filePath = file.getNewFullPath();
                    filePath = filePath.replace(rootPath, "");
                    String obfuscatedContent = file.getObfuscatedContent() == null ? file.getContent() : file.getObfuscatedContent();
                    var result = FileUtil.saveToDisk(Paths.get(downloadLocation, filePath).toString(), obfuscatedContent);
                    if (!result) {
                        filesWithError.append("\n").append(file.getFullPath());
                        errorCount++;
                    }
                }
                downloadConfigurationFiles(rootPath);
                downloadNonJavaFiles(rootPath);

                log("Downloaded to " + downloadLocation, Console.Status.INFO);
                if (errorCount > 0) {
                    log(errorCount + " file(s) failed to download:" + filesWithError.toString(), Console.Status.WARNING);
                }
            } catch(Throwable e) {
                e.printStackTrace();
                log("Download failed: " + e.getMessage(), Console.Status.INFO);
            } finally {
                Platform.runLater(() -> mainView.enableDownloadButton());
            }
            return null;
        }

        private void downloadConfigurationFiles(String rootPath) {
            var xmlDocuments = XmlParser.getStashedDocuments();
            for (var document : xmlDocuments) {
                try {
                    var filePath = URLDecoder.decode(document.getDocumentURI(), "UTF-8");
                    filePath = FileUtil.normalizeFilePath(filePath);
                    filePath = cleanFilePath(filePath);
                    filePath = filePath.replace(rootPath, "");
                    var newFilePath = Paths.get(downloadLocation, filePath);
                    XmlParser.saveFile(document, newFilePath.toString());
                } catch (UnsupportedEncodingException ignored) { }
            }
            var textDocuments = TextParser.getStashedDocuments();
            for (var document : textDocuments) {
                var filePath = document.getNewFullPath();
                filePath = filePath.replace(rootPath, "");
                FileUtil.saveToDisk(Paths.get(downloadLocation, filePath).toString(), document.getContent());
            }
        }

        private void downloadNonJavaFiles(String rootPath) {
            // Copy non-java files from previous folder
            File rootDir = new File(rootPath);
            if (!rootDir.exists()) {
                log("Could not download non-java files from original directory, the directory may have been moved or deleted", Console.Status.WARNING);
                return;
            }
            File[] fileList = rootDir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    copyFiles(file, rootPath, downloadLocation);
                }
            }
        }

        private void copyFiles(File file, String rootPath, String downloadLocation) {
            if (file == null) return;
            var filePath = file.getAbsolutePath();
            filePath = filePath.replace(rootPath, "");
            var newFilePath = Paths.get(downloadLocation, filePath);

            if (file.isDirectory()) {
                File[] fileList = file.listFiles();
                if (fileList != null) {
                    for (File child : fileList) {
                        copyFiles(child, rootPath, downloadLocation);
                    }
                }
            } else if (!"java".equals(FileUtil.getFileExt(file.toPath()))) {
                FileUtil.saveToDisk(newFilePath, file);
            }
        }

        private String cleanFilePath(String filePath) {
            if (filePath.startsWith("file:" + File.separator)) {
                return filePath.replaceFirst(Pattern.quote("file:" + File.separator), "");
            } else {
                return filePath;
            }
        }

        private void log(String msg, Console.Status status) {
            Platform.runLater(() -> LogStateController.log(msg, status));
        }
    }
}

