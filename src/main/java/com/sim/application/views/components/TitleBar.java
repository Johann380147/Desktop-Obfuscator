package com.sim.application.views.components;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class TitleBar extends BorderPane implements Initializable {

    @FXML
    private Label title;
    @FXML
    private Button minimise;
    @FXML
    private Button close;

    private static double xOffset = 0;
    private static double yOffset = 0;

    public TitleBar() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/TitleBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public final String getTitle() {
        return title.textProperty().get();
    }

    public final void setTitle(String text) {
        title.textProperty().set(text);
    }

    public final StringProperty titleProperty() { return title.textProperty(); }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        determinePrimaryStage();
    }

    private void InitListeners(Stage stage) {
        this.setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });
        this.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });
        minimise.setOnMouseClicked(event -> {
            stage.setIconified(true);
        });
        close.setOnMouseClicked(event -> {
            stage.close();
        });
    }

    private void determinePrimaryStage() {
        this.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                // scene is set for the first time. Now its the time to listen stage changes.
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        InitListeners((Stage)newWindow);
                    }
                });
            }
        });
    }
}
