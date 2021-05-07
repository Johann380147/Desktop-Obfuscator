package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Parser;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.IMainView;
import com.sim.application.views.components.IConsole;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;


public final class UploadCodeController {

    private static Stage stage;
    private static IDirectoryBrowser directory;
    private static java.io.File defaultPath;
    private static IMainView mainView;

    private UploadCodeController() {}

    public static void initialize(Stage stage, IMainView mainView, IDirectoryBrowser directory) {
        UploadCodeController.stage = stage;
        UploadCodeController.mainView = mainView;
        UploadCodeController.directory = directory;
    }

    public static void uploadCode() {
        File selectedDirectory = openDirectoryChooser();
        if (selectedDirectory == null) return;

        var upload = new Upload(selectedDirectory);

        mainView.disableObfuscateButton();
        mainView.disableDownloadButton();
        directory.disableButtons();
        var thread = new Thread(upload);
        thread.setDaemon(true);
        thread.start();
    }

    private static File openDirectoryChooser() {
        if (stage == null) return null;
        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (defaultPath != null) {
            if (defaultPath.exists()) {
                directoryChooser.setInitialDirectory(defaultPath);
            } else {
                defaultPath = null;
            }
        }
        return directoryChooser.showDialog(stage);
    }

    public static class Upload extends Task<Void> {

        private File selectedDirectory;

        Upload(File selectedDirectory) {
            this.selectedDirectory = selectedDirectory;
        }

        @Override
        public Void call() {
            try {
                defaultPath = selectedDirectory;
                TreeItem<JavaFile> rootItem = new TreeItem<>(new JavaFile(selectedDirectory.getAbsolutePath(), selectedDirectory, null));

                log("Uploading files...", IConsole.Status.INFO);
                JavaFile.setProjectObfuscated(false);
                directory.clearProjectFiles();
                File[] fileList = selectedDirectory.listFiles();
                if (fileList != null) {
                    for (File file : fileList) {
                        createTree(rootItem, file, selectedDirectory.getPath());
                    }
                }
                Parser.init(selectedDirectory.getAbsolutePath());

                Platform.runLater(() -> directory.setRootDirectory(rootItem));
                log("Files upload done", IConsole.Status.INFO);
            } catch (Exception e) {
                log("Files upload failed. " + e.getMessage(), IConsole.Status.INFO);
            } finally {
                Platform.runLater(() -> {
                    mainView.enableObfuscateButton();
                    mainView.enableDownloadButton();
                    directory.enableButtons();
                });
            }
            return null;
        }

        private boolean createTree(TreeItem<JavaFile> parent, File file, String rootPath) {
            boolean hasJavaFiles = false;

            if (file == null) return false;

            if (file.isDirectory()) {
                JavaFile javaFile = new JavaFile(rootPath, file, null);
                TreeItem<JavaFile> treeItem = new TreeItem<>(javaFile);

                File[] fileList = file.listFiles();
                if (fileList != null) {
                    for (File f : fileList) {
                        if (createTree(treeItem, f, rootPath)) {
                            hasJavaFiles = true;
                        }
                    }
                }

                if (hasJavaFiles) {
                    parent.getChildren().add(treeItem);
                }
            } else if ("java".equals(FileUtil.getFileExt(file.toPath()))) {
                JavaFile javaFile = new JavaFile(rootPath, file, FileUtil.getFileContent(file.toPath()));
                parent.getChildren().add(new TreeItem<>(javaFile));
                directory.addProjectFile(javaFile);
                hasJavaFiles = true;
            }
            return hasJavaFiles;
        }

        private static void log(String msg, IConsole.Status status) {
            Platform.runLater(() -> LogStateController.log(msg, status));
        }
    }
}
