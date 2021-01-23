package com.sim.application.views.components;

import com.sim.application.classes.File;
import com.sim.application.utils.FileUtil;
import com.sim.application.views.BaseView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class DirectoryBrowser extends VBox implements Initializable {

    @FXML
    private TreeView<File> directory;
    @FXML
    private Button browse;
    @FXML
    private Button clear;

    private java.io.File defaultPath;
    private HashMap<File, TreeItem<File>> fileTreeItems = new HashMap<>();
    private Thread thread;

    public DirectoryBrowser() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/DirectoryBrowser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void addFileSelectionChangedListener(ChangeListener<TreeItem<File>> listener) {
        directory.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    public boolean createTree(TreeItem<File> parent, java.io.File file) {
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
            parent.getChildren().add(new TreeItem<>(internalFile));
            hasJavaFiles = true;
        }
        return hasJavaFiles;
    }


    private void InitBrowseListener(Stage stage) {
        browse.setOnMouseClicked(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            if (defaultPath != null) {
                directoryChooser.setInitialDirectory(defaultPath);
            }
            java.io.File selectedDirectory = directoryChooser.showDialog(stage);

            if (selectedDirectory != null) {
                defaultPath = selectedDirectory;

                TreeItem<File> rootItem = new TreeItem<>(new File(selectedDirectory.getAbsolutePath(), null, true));
                directory.setRoot(rootItem);
                rootItem.setExpanded(true);

                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                }
                // Run on non-FX thread
                thread = new Thread(() -> {
                    java.io.File[] fileList = selectedDirectory.listFiles();
                    for(java.io.File file : fileList){
                        createTree(rootItem, file);
                    }
                });
                thread.start();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Map File's fileName to each node's display text
        directory.setCellFactory(treeView -> {
            TreeCell<File> cell = new TreeCell<>() {
                @Override
                protected void updateItem(File file, boolean b) {
                    super.updateItem(file, b);
                    textProperty().unbind();
                    if (isEmpty())
                        setText(null);
                    else
                        textProperty().bind(file.nameProperty());
                }
            };
            return cell;
        });
        clear.setOnMouseClicked(event -> directory.setRoot(null));

        BaseView.runOnStageSet(this, stage -> InitBrowseListener(stage));
    }
}
