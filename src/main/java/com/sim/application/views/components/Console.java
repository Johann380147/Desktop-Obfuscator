package com.sim.application.views.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Console extends VBox implements Initializable {

    @FXML
    private ScrollPane consoleScrollPane;
    @FXML
    private TextFlow console;

    public enum Status { NORMAL, WARNING, ERROR }

    public Console() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/Console.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (
                IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void addLog(String text, Status status) {
        if (status == Status.ERROR) {
            Label l = new Label();
            console.getChildren().add(addTag("ERROR: ", Color.RED));
            console.getChildren().add(new Text(text + "\n"));
        }
        else if (status == Status.WARNING) {
            Label l = new Label();
            console.getChildren().add(addTag("WARNING: ", Color.YELLOW));
            console.getChildren().add(new Text(text + "\n"));
        }
        else {
            console.getChildren().add(new Text(text + "\n"));
        }
    }

    private Label addTag(String text, Color color) {
        Label tag = new Label(text);
        tag.setTextFill(color);
        return tag;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        console.heightProperty().addListener(observable -> consoleScrollPane.setVvalue(1D));
    }
}
