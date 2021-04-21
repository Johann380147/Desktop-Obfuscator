package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Parser;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public final class UploadCodeController {

    private static Stage stage;
    private static IDirectoryBrowser directory;
    private static java.io.File defaultPath;

    private UploadCodeController() {}

    public static void initialize(Stage stage, IDirectoryBrowser directory) {
        UploadCodeController.stage = stage;
        UploadCodeController.directory = directory;
    }

    public static void uploadCode() {
        File selectedDirectory = openDirectoryChooser();
        if (selectedDirectory == null) return;

        var upload = new Upload(selectedDirectory);

        directory.disableButtons();
        var thread = new Thread(upload);
        thread.setDaemon(true);
        thread.start();
    }

    private static File openDirectoryChooser() {
        if (stage == null) return null;
        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (defaultPath != null) {
            directoryChooser.setInitialDirectory(defaultPath);
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

                Platform.runLater(() -> LogStateController.log("Uploading files...", Console.Status.INFO));
                directory.clearProjectFiles();
                File[] fileList = selectedDirectory.listFiles();
                for (File file : fileList) {
                    createTree(rootItem, file, selectedDirectory.getPath());
                }
                Parser.init(selectedDirectory.getAbsolutePath());

                Platform.runLater(() -> directory.setRootDirectory(rootItem));
                Platform.runLater(() -> LogStateController.log("Files upload done", Console.Status.INFO));
            } catch (Exception e) {
                Platform.runLater(() -> LogStateController.log("Files upload failed. " + e.getMessage(), Console.Status.INFO));
            } finally {
                Platform.runLater(() -> directory.enableButtons());
            }
            return null;
        }

        private boolean createTree(TreeItem<JavaFile> parent, File file, String rootPath) {
            boolean hasJavaFiles = false;

            if (file == null) return false;

            if (file.isDirectory()) {
                JavaFile javaFile = new JavaFile(rootPath, file, null);
                TreeItem<JavaFile> treeItem = new TreeItem<>(javaFile);

                for (File f : file.listFiles()) {
                    if (createTree(treeItem, f, rootPath)) {
                        hasJavaFiles = true;
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
    }
}
