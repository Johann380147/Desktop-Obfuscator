package com.sim.application.controllers;

import com.sim.application.entities.JavaFile;
import com.sim.application.views.components.DirectoryBrowser;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.regex.Pattern;

public class AddFileToDirectoryController {
    private static DirectoryBrowser directory;

    private AddFileToDirectoryController() {}

    public static void initialize(DirectoryBrowser directory) {
        AddFileToDirectoryController.directory = directory;
    }

    public static void addFile(JavaFile file) {
        var relativePath = getParentDirectory(file);
        var root = directory.getRootDirectory();

        TreeItem<JavaFile> parentDirectory = findRelativePath(root, relativePath);
        if (parentDirectory != null) {
            directory.addProjectFile(file);
            Platform.runLater(() -> parentDirectory.getChildren().add(new TreeItem<>(file)));
        }
    }

    private static TreeItem<JavaFile> findRelativePath(TreeItem<JavaFile> file, String relativePath) {
        if (file.getValue().getRelativePath().equals(relativePath)) {
            return file;
        }
        for (var child : file.getChildren()) {
            var jFile = findRelativePath(child, relativePath);
            if (jFile != null) {
                return jFile;
            }
        }
        return null;
    }

    private static String getParentDirectory(JavaFile file) {
        StringBuilder stringBuilder = new StringBuilder();
        var path = file.getRelativePath().split(Pattern.quote(File.separator));
        for (int i = 0; i < path.length - 1; i++) {
            stringBuilder.append(path[i]);
            if (i != path.length - 2) {
                stringBuilder.append(File.separator);
            }
        }
        return stringBuilder.toString();
    }
}
