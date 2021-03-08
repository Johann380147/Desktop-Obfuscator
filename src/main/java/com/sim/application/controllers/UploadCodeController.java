package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;


public final class UploadCodeController {

    private static Stage stage;
    private static IDirectoryBrowser directory;
    private static java.io.File defaultPath;
    private static Thread thread;

    private UploadCodeController() {}

    public static void initialize(Stage stage, IDirectoryBrowser directory) {
        UploadCodeController.stage = stage;
        UploadCodeController.directory = directory;
    }

    public static void uploadCode() {
        File selectedDirectory = openDirectoryChooser();

        if (selectedDirectory != null) {
            defaultPath = selectedDirectory;

            TreeItem<JavaFile> rootItem = new TreeItem<>(new JavaFile(selectedDirectory.getAbsolutePath(), selectedDirectory, null));
            directory.setRootDirectory(rootItem);

            stopThread();
            // Run on non-FX thread
            startThread(() -> {
                File[] fileList = selectedDirectory.listFiles();
                for(File file : fileList){
                    createTree(rootItem, file, selectedDirectory.getPath());
                }
            });
        }
    }

    private static File openDirectoryChooser() {
        if (stage == null) return null;
        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (defaultPath != null) {
            directoryChooser.setInitialDirectory(defaultPath);
        }
        return directoryChooser.showDialog(stage);
    }

    private static void startThread(Runnable target) {
        thread = new Thread(target);
        thread.start();
    }

    private static void stopThread() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    private static boolean createTree(TreeItem<JavaFile> parent, File file, String rootPath) {
        boolean hasJavaFiles = false;

        if (file == null) return false;

        if (file.isDirectory()) {
            JavaFile javaFile = new JavaFile(rootPath, file, null);
            TreeItem<JavaFile> treeItem = new TreeItem<>(javaFile);

            for (File f : file.listFiles()) {
                if (createTree(treeItem, f, rootPath) == true) {
                    hasJavaFiles = true;
                }
            }

            if (hasJavaFiles) {
                Platform.runLater(() -> parent.getChildren().add(treeItem));
            }
        } else if ("java".equals(FileUtil.getFileExt(file.toPath()))) {
            JavaFile javaFile = new JavaFile(rootPath, file, FileUtil.getFileContent(file.toPath()));
            Platform.runLater(() -> parent.getChildren().add(new TreeItem<>(javaFile)));
            hasJavaFiles = true;
        }
        return hasJavaFiles;
    }
}
