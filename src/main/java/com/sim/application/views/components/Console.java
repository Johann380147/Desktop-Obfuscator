package com.sim.application.views.components;

import com.sim.application.controllers.LogStateController;
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

    public enum Status {INFO, WARNING, ERROR }

    @FXML
    private ScrollPane consoleScrollPane;
    @FXML
    private TextFlow console;

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

    public void addLog(String timeStamp, String content, Status status) {
        console.getChildren().add(new Text(timeStamp + " "));
        if (status == Status.ERROR) {
            console.getChildren().add(addTag("ERROR: ", Color.RED));
            console.getChildren().add(new Text(content + "\n"));
        }
        else if (status == Status.WARNING) {
            console.getChildren().add(addTag("WARNING: ", Color.ORANGE));
            console.getChildren().add(new Text(content + "\n"));
        }
        else {
            console.getChildren().add(addTag("INFO: ", Color.LIGHTSKYBLUE));
            console.getChildren().add(new Text(content + "\n"));
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
