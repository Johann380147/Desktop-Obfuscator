package com.sim.application.controllers;

import com.sim.application.classes.File;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


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
        java.io.File selectedDirectory = openDirectoryChooser();

        if (selectedDirectory != null) {
            defaultPath = selectedDirectory;

            TreeItem<File> rootItem = new TreeItem<>(new File(selectedDirectory.getAbsolutePath(), null, true));
            directory.setRootDirectory(rootItem);

            stopThread();
            // Run on non-FX thread
            startThread(() -> {
                java.io.File[] fileList = selectedDirectory.listFiles();
                for(java.io.File file : fileList){
                    createTree(rootItem, file);
                }
            });
        }
    }

    private static java.io.File openDirectoryChooser() {
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

    private static boolean createTree(TreeItem<File> parent, java.io.File file) {
        boolean hasJavaFiles = false;

        if (file == null) return false;

        if (file.isDirectory()) {
            File internalFile = new File(file.getAbsolutePath(), null, true);
            TreeItem<File> treeItem = new TreeItem<>(internalFile);

            for (java.io.File f : file.listFiles()) {
                if (createTree(treeItem, f) == true) {
                    hasJavaFiles = true;
                }
            }

            if (hasJavaFiles) {
                Platform.runLater(() -> parent.getChildren().add(treeItem));
            }
        } else if ("java".equals(FileUtil.getFileExt(file.toPath()))) {
            File internalFile = new File(file.getAbsolutePath(), FileUtil.getFileContent(file.toPath()), false);
            Platform.runLater(() -> parent.getChildren().add(new TreeItem<>(internalFile)));
            hasJavaFiles = true;
        }
        return hasJavaFiles;
    }
}
