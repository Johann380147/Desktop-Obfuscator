package com.sim.application.views.components;

import com.sim.application.classes.JavaFile;
import com.sim.application.classes.ProjectFiles;
import javafx.scene.control.TreeItem;

import java.util.List;

public interface IDirectoryBrowser {

    public ProjectFiles getProjectFiles();
    public TreeItem<JavaFile> getCurrentSelection();
    public TreeItem<JavaFile> getRootDirectory();
    public void setRootDirectory(TreeItem<JavaFile> root);
    public void clearDirectory();
    public void disableButtons();
    public void enableButtons();
}
