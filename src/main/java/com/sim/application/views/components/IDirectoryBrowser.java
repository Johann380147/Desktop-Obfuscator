package com.sim.application.views.components;

import com.sim.application.classes.JavaFile;
import javafx.scene.control.TreeItem;

import java.util.List;

public interface IDirectoryBrowser {

    public List<JavaFile> getJavaFiles();
    public List<JavaFile> getSrcDirs();
    public TreeItem<JavaFile> getCurrentSelection();
    public TreeItem<JavaFile> getRootDirectory();
    public void setRootDirectory(TreeItem<JavaFile> root);
    public void clearDirectory();
}
