package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


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
        directory.disableButtons();

        File selectedDirectory = openDirectoryChooser();
        if (selectedDirectory == null) return;

        var upload = new Upload(selectedDirectory);
        upload.setOnSucceeded(workerStateEvent -> {
            projectFiles.setProjectFiles(upload.getValue());
        });
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

                if (projectFiles.getSourceDirectories().size() == 0) {
                    projectFiles.addSourceDirectory(new JavaFile(selectedDirectory.getPath(), selectedDirectory, null));
                }

                Platform.runLater(() -> directory.setRootDirectory(rootItem));
                Platform.runLater(() -> LogStateController.log("Files upload done", Console.Status.INFO));
                return projectFiles;
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

                if (isSourceDirectory(javaFile.getFullPath())) {
                    projectFiles.addSourceDirectory(javaFile);
                }
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
            } else if ("jar".equals(FileUtil.getFileExt(file.toPath())) ||
                    "zip".equals(FileUtil.getFileExt(file.toPath()))) {
                JavaFile javaFile = new JavaFile(rootPath, file, null);
                parent.getChildren().add(new TreeItem<>(javaFile));
                projectFiles.addLibraryFile(javaFile);
                hasJavaFiles = true;
            }
            return hasJavaFiles;
        }

        private boolean isSourceDirectory(String path) {
            if (path.endsWith("src") ||
                    path.endsWith("src" + File.separator + "main" + File.separator + "java")) {
                return true;
            } else {
                return false;
            }
        }
    }
}
