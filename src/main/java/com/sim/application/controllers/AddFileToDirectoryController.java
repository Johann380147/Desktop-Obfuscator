package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.regex.Pattern;

public class AddFileToDirectoryController {
    private static IDirectoryBrowser directory;

    private AddFileToDirectoryController() {}

    public static void initialize(IDirectoryBrowser directory) {
        AddFileToDirectoryController.directory = directory;
    }

    public static void addFile(JavaFile file) {
        var relativePath = getParentDirectory(file);
        var root = directory.getRootDirectory();

        for (var child : root.getChildren()) {
            TreeItem<JavaFile> parentDirectory = findRelativePath(child, relativePath);
            if (parentDirectory != null) {
                directory.addProjectFile(file);
                Platform.runLater(() -> parentDirectory.getChildren().add(new TreeItem<>(file)));
                break;
            }
        }
    }

    private static TreeItem<JavaFile> findRelativePath(TreeItem<JavaFile> file, String relativePath) {
        if (file.getValue().getRelativePath().equals(relativePath)) {
            return file;
        }
        for (var child : file.getChildren()) {
            return findRelativePath(child, relativePath);
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
