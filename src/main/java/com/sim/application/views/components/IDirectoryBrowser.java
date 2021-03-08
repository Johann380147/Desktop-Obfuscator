package com.sim.application.views.components;

import com.sim.application.classes.JavaFile;
import javafx.scene.control.TreeItem;

public interface IDirectoryBrowser {

    public TreeItem<JavaFile> getCurrentSelection();
    public TreeItem<JavaFile> getRootDirectory();
    public void setRootDirectory(TreeItem<JavaFile> root);
    public void clearDirectory();
}
