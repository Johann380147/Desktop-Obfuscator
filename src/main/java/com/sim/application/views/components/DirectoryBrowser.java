package com.sim.application.views.components;

import com.sim.application.classes.File;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DirectoryBrowser extends VBox implements Initializable {

    @FXML
    private TreeView<File> directory;
    @FXML
    private Button browse;
    @FXML
    private Button clear;

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

    public void addBrowseClickedListener(EventHandler<? super MouseEvent> listener)
    {
        browse.setOnMouseClicked(listener);
    }

    public void addClearClickedListener(EventHandler<? super MouseEvent> listener) {
        clear.setOnMouseClicked(listener);
    }

    public TreeItem<File> getRoot() {
        return directory.getRoot();
    }

    public void setRoot(TreeItem<File> root) {
        directory.setRoot(root);
        if (root != null) {
            root.setExpanded(true);
        }
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
    }
}
