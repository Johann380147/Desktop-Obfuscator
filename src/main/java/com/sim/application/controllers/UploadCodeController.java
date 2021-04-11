package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Parser;
import com.sim.application.classes.ProjectFiles;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.components.Console;
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

    private static ProjectFiles projectFiles;

    private UploadCodeController() {}

    public static void initialize(Stage stage, IDirectoryBrowser directory) {
        UploadCodeController.stage = stage;
        UploadCodeController.directory = directory;
    }

    public static void uploadCode(ProjectFiles projectFiles) {
        File selectedDirectory = openDirectoryChooser();
        if (selectedDirectory == null) return;

        var upload = new Upload(selectedDirectory);
        upload.setOnSucceeded(workerStateEvent ->
            projectFiles.setProjectFiles(upload.getValue())
        );

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

    public static class Upload extends Task<ProjectFiles> {

        private File selectedDirectory;

        Upload(File selectedDirectory) {
            this.selectedDirectory = selectedDirectory;
        }

        @Override
        public ProjectFiles call() {
            try {
                defaultPath = selectedDirectory;
                TreeItem<JavaFile> rootItem = new TreeItem<>(new JavaFile(selectedDirectory.getAbsolutePath(), selectedDirectory, null));

                Platform.runLater(() -> LogStateController.log("Uploading files...", Console.Status.INFO));

                projectFiles = new ProjectFiles();
                File[] fileList = selectedDirectory.listFiles();
                for (File file : fileList) {
                    createTree(rootItem, file, selectedDirectory.getPath());
                }
                Parser.init(selectedDirectory.getAbsolutePath());

                Platform.runLater(() -> directory.setRootDirectory(rootItem));
                Platform.runLater(() -> LogStateController.log("Files upload done", Console.Status.INFO));
                return projectFiles;
            } catch (Exception e) {
                Platform.runLater(() -> LogStateController.log("Files upload failed. " + e.getMessage(), Console.Status.INFO));
                return null;
            } finally {
                Platform.runLater(() -> directory.enableButtons());
            }
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
                projectFiles.addJavaFiles(javaFile);
                hasJavaFiles = true;
            }
            return hasJavaFiles;
        }
    }
}
