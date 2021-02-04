package com.sim.application.views.components;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class AboutDialog extends StackPane implements Initializable {

    @FXML
    private Label title;
    @FXML
    private Pane content;
    @FXML
    private Button closeDialog;

    private Stage stage;

    public AboutDialog() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/AboutDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent, Color.TRANSPARENT);
            stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused == false)
                    hide();
            });

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void show() {
        stage.showAndWait();
    }

    public void hide() {
        stage.close();
    }

    public void addVerticalContent(Node... node) {
        content.getChildren().addAll(node);
    }

    public void addHorizontalContent(Node... node) {
        content.getChildren().add(new HBox(node));
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
        this.setBackground(new Background(new BackgroundFill(null,null,null)));
        closeDialog.setOnMouseClicked(event -> hide());
    }
}
