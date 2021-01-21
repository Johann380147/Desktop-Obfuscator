package com.sim.application.views.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class StatusBar extends VBox {

    @FXML
    private Label leftStatus;
    @FXML
    private Label rightStatus;

    public StatusBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/StatusBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setLeftStatus(String status) {
        this.leftStatus.setText(status);
    }

    public void setRightStatus(String status) {
        this.rightStatus.setText(status);
    }
}
