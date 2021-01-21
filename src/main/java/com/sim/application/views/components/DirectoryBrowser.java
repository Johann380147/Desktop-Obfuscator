package com.sim.application.views.components;

import com.sim.application.classes.File;
import com.sim.application.utils.FileUtil;
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

    public void createTree(TreeItem<File> parent, java.io.File file) {
        if (file.isDirectory()) {
            File internalFile = new File(file.getAbsolutePath(), null, true);
            TreeItem<File> treeItem = new TreeItem<>(internalFile);
            parent.getChildren().add(treeItem);
            for (java.io.File f : file.listFiles()) {
                createTree(treeItem, f);
            }
        } else if ("java".equals(FileUtil.getFileExt(file.toPath()))) {
            File internalFile = new File(file.getAbsolutePath(), FileUtil.getFileContent(file.toPath()), false);
            parent.getChildren().add(new TreeItem<>(internalFile));
        }
    }


    private void InitBrowseListener(Stage stage) {
        browse.setOnMouseClicked(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            if (defaultPath != null)
                directoryChooser.setInitialDirectory(defaultPath);

            java.io.File selectedDirectory = directoryChooser.showDialog(stage);

            if (selectedDirectory != null) {
                defaultPath = selectedDirectory;

                TreeItem<File> rootItem = new TreeItem<>(new File(selectedDirectory.getAbsolutePath(), null, true));
                directory.setRoot(rootItem);
                rootItem.setExpanded(true);

                java.io.File[] fileList = selectedDirectory.listFiles();
                for(java.io.File file : fileList){
                    createTree(rootItem, file);
                }
            }
        });
    }

    private void determinePrimaryStage() {
        this.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                // scene is set for the first time. Now its the time to listen stage changes.
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        InitBrowseListener((Stage)newWindow);
                    }
                });
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
        determinePrimaryStage();
    }
}
