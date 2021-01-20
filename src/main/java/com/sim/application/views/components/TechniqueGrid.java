package com.sim.application.views.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;


public class TechniqueGrid extends GridPane {

    public TechniqueGrid() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/TechniqueGrid.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public CheckBox addTechnique(String name, String description) {
        List<RowConstraints> constraints = this.getRowConstraints();
        constraints.add(new RowConstraints());

        CheckBox checkBox = createCheckBox(name);
        Label label = createLabel(description);
        int row = this.getRowCount() - 1;
        this.add(checkBox, 0, row);
        this.add(label, 1, row);

        return checkBox;
    }

    private CheckBox createCheckBox(String name) {
        CheckBox checkBox = new CheckBox(name);
        checkBox.setId(name);
        checkBox.setCursor(Cursor.HAND);
        checkBox.selectedProperty().addListener(event -> {
            // To implement ?
        });

        return checkBox;
    }

    private Label createLabel(String description) {
        Label label = new Label(description);
        label.setTextFill(Color.color(.49,.49,.49));
        return label;
    }
}
