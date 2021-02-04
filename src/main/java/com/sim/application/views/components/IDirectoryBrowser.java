package com.sim.application.views.components;

import com.sim.application.classes.File;
import javafx.scene.control.TreeItem;

public interface IDirectoryBrowser {

    public TreeItem<File> getCurrentSelection();
    public TreeItem<File> getRootDirectory();
    public void setRootDirectory(TreeItem<File> root);
    public void clearDirectory();
}
