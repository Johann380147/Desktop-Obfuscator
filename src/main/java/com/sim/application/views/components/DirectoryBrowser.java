package com.sim.application.views.components;

import com.sim.application.classes.JavaFile;
import com.sim.application.controllers.*;
import com.sim.application.views.StageObserver;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DirectoryBrowser extends VBox implements Initializable, StageObserver, IDirectoryBrowser {

    @FXML
    private TreeView<JavaFile> directory;
    @FXML
    private Button browse;
    @FXML
    private Button clear;

    private TreeItem<JavaFile> currentSelection;

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

    @Override
    public TreeItem<JavaFile> getCurrentSelection() {
        return currentSelection;
    }

    @Override
    public TreeItem<JavaFile> getRootDirectory() {
        return directory.getRoot();
    }

    @Override
    public void setRootDirectory(TreeItem<JavaFile> root) {
        directory.setRoot(root);
        if (root != null) {
            root.setExpanded(true);
        }
    }

    @Override
    public void clearDirectory() {
        directory.setRoot(null);
    }

    private void InitControllersNeedingStage(Stage stage) {
        UploadCodeController.initialize(stage, this);
        DownloadObfuscatedCodeController.initialize(stage, this);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Map File's fileName to each node's display text
        directory.setCellFactory(treeView -> {
            TreeCell<JavaFile> cell = new TreeCell<>() {
                @Override
                protected void updateItem(JavaFile file, boolean b) {
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

        ClearDirectoryController.initialize(this);
        ObfuscateCodeController.initialize(this);
        StageObserver.runOnStageSet(this, stage -> InitControllersNeedingStage(stage));
        browse.setOnMouseClicked(event -> UploadCodeController.uploadCode());
        clear.setOnMouseClicked(event -> ClearDirectoryController.clearDirectory());
        directory.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            currentSelection = newValue;
            DisplayUploadedCodeController.DisplayCode(newValue);
            DisplayObfuscatedCodeController.DisplayCode(newValue);
        });
    }
}
