package com.sim.application.views.components;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.Glyph;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


public class TechniqueGrid extends VBox implements Initializable {

    @FXML
    private GridPane grid;
    @FXML
    private Button toggle;

    private HashMap<String, CheckBox> checkBoxes = new HashMap<>();
    private Glyph checkedGlyph = Glyph.create("FontAwesome|CHECK_SQUARE");
    private Glyph uncheckedGlyph = Glyph.create("FontAwesome|CHECK_SQUARE_ALT");
    private boolean checkAll = true;

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

    public void addTechnique(String name, String description) {
        List<RowConstraints> constraintList = grid.getRowConstraints();
        int row = grid.getRowCount();

        constraintList.add(new RowConstraints());
        CheckBox checkBox = createCheckBox(name);
        grid.add(checkBox, 0, row);
        checkBoxes.put(name, checkBox);

        if (!description.equals("")) {
            constraintList.add(new RowConstraints());
            Label label = createLabel(description);
            grid.add(label, 0, row + 1);
        }
    }

    private CheckBox createCheckBox(String name) {
        CheckBox checkBox = new CheckBox(name);
        checkBox.setId(name);
        checkBox.setCursor(Cursor.HAND);
        checkBox.selectedProperty().addListener(event -> {
            if (checkBoxes.values().stream()
                    .map(CheckBox::isSelected)
                    .allMatch(c -> c == true)) {
                setCheckAll(false);
            }
            else if (checkBoxes.values().stream()
                    .map(CheckBox::isSelected)
                    .anyMatch(c -> c == false)) {
                setCheckAll(true);
            }
        });

        return checkBox;
    }

    private Label createLabel(String description) {
        Label label = new Label(description);
        label.setWrapText(true);
        label.setPadding(new Insets(0, 0, 0, 22));
        label.setTextFill(Color.color(.49,.49,.49));

        return label;
    }

    public void addToggleClickedListener(EventHandler<? super MouseEvent> listener) {
        toggle.setOnMouseClicked(listener);
    }

    public boolean getCheckAll() {
        return checkAll;
    }

    public void setCheckAll(boolean value) {
        checkAll = value;
        if (value == true)
            toggle.setGraphic(checkedGlyph);
        else
            toggle.setGraphic(uncheckedGlyph);
    }

    public Collection<CheckBox> getCheckBoxes() {
        return checkBoxes.values();
    }

    public List<String> getSelectedTechniques() {
        List<String> techniqueText = checkBoxes.values().stream()
                .filter(CheckBox::isSelected)
                .map(Labeled::getText)
                .collect(Collectors.toList());
        return techniqueText;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        toggle.setGraphic(checkedGlyph);
    }
}
