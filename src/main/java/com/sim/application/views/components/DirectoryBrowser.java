package com.sim.application.views.components;

import com.sim.application.entities.JavaFile;
import com.sim.application.controllers.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.Glyph;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DirectoryBrowser extends VBox implements Initializable, IDirectoryBrowser {

    @FXML
    private Label fileSizeLabel;
    @FXML
    private TreeView<JavaFile> directory;
    @FXML
    private Button browse;
    @FXML
    private Button clear;

    private TreeItem<JavaFile> currentSelection;
    private List<JavaFile> projectFiles = new ArrayList<>();

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
    public List<JavaFile> getProjectFiles() {
        return projectFiles;
    }

    @Override
    public void clearProjectFiles() {
        projectFiles.clear();
        Platform.runLater(() -> fileSizeLabel.setText(""));
    }

    @Override
    public void addProjectFile(JavaFile file) {
        projectFiles.add(file);
        Platform.runLater(() -> fileSizeLabel.setText(projectFiles.size() + " file(s)"));
    }

    @Override
    public void removeFilesAddedPostObfuscation() {
        projectFiles.removeIf(JavaFile::isAddedPostObfuscation);
        if (currentSelection != null && currentSelection.getValue().isAddedPostObfuscation()) {
            ClearCodeDisplayController.clearDisplay();
            directory.getSelectionModel().selectFirst();
        }
        for (var child : getRootDirectory().getChildren()) {
            removeFile(child);
        }
    }

    private void removeFile(TreeItem<JavaFile> file) {
        file.getChildren().removeIf(treeItem -> treeItem.getValue().isAddedPostObfuscation());
        for (var child : file.getChildren()) {
            removeFile(child);
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
            sortDirectory(root);
        }
    }

    public void sortDirectory(TreeItem<JavaFile> treeItem) {
        if (treeItem.getChildren() != null) {
            treeItem.getChildren().sort((child, child2) -> {
                if (child.getValue().isDirectory()) {
                    if (child2.getValue().isDirectory()) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else {
                    if (child2.getValue().isDirectory()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            for (var child : treeItem.getChildren()) {
                sortDirectory(child);
            }
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
        projectFiles.clear();
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

        browse.setOnMouseClicked(event -> UploadCodeController.uploadCode());
        clear.setOnMouseClicked(event -> ClearDirectoryController.clearDirectory());
        directory.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            StoreScrollPositionController.StorePosition();
            currentSelection = newValue;
            DisplayUploadedCodeController.displayCode(newValue);
            DisplayObfuscatedCodeController.displayCode(newValue);
        });
    }
}
