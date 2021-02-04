package com.sim.application.views.components;

import com.sim.application.controllers.ToggleTechniquesController;
import com.sim.application.techniques.Technique;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
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


public class TechniqueGrid extends VBox implements Initializable, ITechniqueGrid {

    @FXML
    private GridPane grid;
    @FXML
    private Button toggle;

    private HashMap<String, CheckBox> checkBoxes = new HashMap<>();
    private Glyph checkedGlyph = Glyph.create("FontAwesome|CHECK_SQUARE");
    private Glyph uncheckedGlyph = Glyph.create("FontAwesome|CHECK_SQUARE_ALT");
    private boolean allChecked = true;

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

    public void addTechnique(Technique technique) {
        String name = technique.getName();
        String description = technique.getDescription();
        List<RowConstraints> constraintList = grid.getRowConstraints();
        int row = grid.getRowCount();

        constraintList.add(new RowConstraints());
        CheckBox checkBox = createCheckBox(name);
        checkBox.setUserData(technique);
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
                setAllChecked(false);
            }
            else if (checkBoxes.values().stream()
                    .map(CheckBox::isSelected)
                    .anyMatch(c -> c == false)) {
                setAllChecked(true);
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

    public List<Technique> getSelectedTechniques() {
        List<Technique> techniqueText = checkBoxes.values().stream()
                .filter(CheckBox::isSelected)
                .map(Node::getUserData)
                .map(e -> (Technique) e)
                .collect(Collectors.toUnmodifiableList());
        return techniqueText;
    }

    @Override
    public boolean getAllChecked() {
        return allChecked;
    }

    @Override
    public void setAllChecked(boolean value) {
        allChecked = value;
        if (value == true)
            toggle.setGraphic(checkedGlyph);
        else
            toggle.setGraphic(uncheckedGlyph);
    }

    @Override
    public Collection<CheckBox> getCheckBoxes() {
        return checkBoxes.values();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        toggle.setGraphic(checkedGlyph);
        ToggleTechniquesController.initialize(this);
        toggle.setOnMouseClicked(event -> ToggleTechniquesController.ToggleTechniques());
    }
}
