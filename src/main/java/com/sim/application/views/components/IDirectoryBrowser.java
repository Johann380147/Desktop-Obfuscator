package com.sim.application.views.components;

import com.sim.application.classes.JavaFile;
import javafx.scene.control.TreeItem;

import java.util.List;

public interface IDirectoryBrowser {

    List<JavaFile> getProjectFiles();
    void addProjectFile(JavaFile file);
    void removeFilesAddedPostObfuscation();
    TreeItem<JavaFile> getCurrentSelection();
    TreeItem<JavaFile> getRootDirectory();
    void setRootDirectory(TreeItem<JavaFile> root);
    void clearDirectory();
    void disableButtons();
    void enableButtons();
}
