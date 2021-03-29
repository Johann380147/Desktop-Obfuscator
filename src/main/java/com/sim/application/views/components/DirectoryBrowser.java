package com.sim.application.views.components;

import com.sim.application.classes.JavaFile;
import com.sim.application.classes.ProjectFiles;
import com.sim.application.controllers.*;
import com.sim.application.views.StageObserver;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.glyphfont.Glyph;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DirectoryBrowser extends VBox implements Initializable, StageObserver, IDirectoryBrowser {

    @FXML
    private TreeView<JavaFile> directory;
    @FXML
    private Button browse;
    @FXML
    private Button clear;

    private TreeItem<JavaFile> currentSelection;
    private ProjectFiles projectFiles = new ProjectFiles();

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
    public ProjectFiles getProjectFiles() {
        return projectFiles;
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
    public void disableButtons() {
        browse.setDisable(true);
        clear.setDisable(true);
    }

    @Override
    public void enableButtons() {
        browse.setDisable(false);
        clear.setDisable(false);
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
        browse.setTooltip(new Tooltip("Upload files"));
        clear.setTooltip(new Tooltip("Clear all"));

        // Map File's fileName to each node's display text
        directory.setCellFactory(treeView -> {
            Glyph folderGlyph = Glyph.create("FontAwesome|FOLDER");
            Glyph fileGlyph = Glyph.create("FontAwesome|FILE");
            folderGlyph.color(Color.GOLDENROD);
            fileGlyph.color(Color.ROYALBLUE);

            return new TreeCell<>() {
                @Override
                protected void updateItem(JavaFile file, boolean empty) {
                    super.updateItem(file, empty);
                    textProperty().unbind();
                    setGraphic(empty ? null : file.isDirectory() ? folderGlyph : fileGlyph);
                    setText(empty ? null : file.getFileName());
                }
            };
        });

        ClearDirectoryController.initialize(this);
        StageObserver.runOnStageSet(this, this::InitControllersNeedingStage);
        browse.setOnMouseClicked(event -> UploadCodeController.uploadCode(projectFiles));
        clear.setOnMouseClicked(event -> ClearDirectoryController.clearDirectory());
        directory.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            StoreScrollPositionController.StorePosition();
            currentSelection = newValue;
            DisplayUploadedCodeController.DisplayCode(newValue);
            DisplayObfuscatedCodeController.DisplayCode(newValue);
        });
    }
}
