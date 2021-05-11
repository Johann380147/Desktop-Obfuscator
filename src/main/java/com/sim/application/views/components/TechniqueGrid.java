package com.sim.application.views.components;

import com.sim.application.controllers.ToggleTechniquesController;
import com.sim.application.techniques.MultiStepTechnique;
import com.sim.application.techniques.Technique;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.Glyph;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class TechniqueGrid extends VBox implements Initializable {

    @FXML
    private GridPane grid;
    @FXML
    private Button toggle;

    private HashMap<String, CheckBox> checkBoxes = new HashMap<>();
    private Glyph checkedGlyph = Glyph.create("FontAwesome|CHECK_SQUARE");
    private Glyph uncheckedGlyph = Glyph.create("FontAwesome|CHECK_SQUARE_ALT");
    private Tooltip checkedTooltip = new Tooltip("Check all");
    private Tooltip uncheckedTooltip = new Tooltip("Uncheck all");
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
        if (technique instanceof MultiStepTechnique) {
            var multiTechnique = (MultiStepTechnique)technique;
            var subTechniques = multiTechnique.getSubTechniques();
            var checkBox = addCheckBox(multiTechnique, null);
            var subCheckBoxes = new ArrayList<CheckBox>();
            for (var subTechnique : subTechniques) {
                subCheckBoxes.add(addCheckBox(subTechnique, new Insets(0, 0, 0, 22)));
            }
            AtomicBoolean block = new AtomicBoolean(false);
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (!block.get()) {
                    if (!newValue) {
                        subCheckBoxes.forEach(subCheckBox -> subCheckBox.setSelected(false));
                    }
                }
                checkBox.setSelected(newValue);
            });
            subCheckBoxes.forEach(subCheckBox -> subCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                block.set(true);
                var anySelected = subCheckBoxes.stream().anyMatch(CheckBox::isSelected);
                if (anySelected) {
                    checkBox.setSelected(true);
                }
                block.set(false);
            }));
        } else {
            addCheckBox(technique, null);
        }
    }

    public CheckBox addCheckBox(Technique technique, Insets insets) {
        String name = technique.getName();
        String description = technique.getDescription();
        List<RowConstraints> constraintList = grid.getRowConstraints();
        int row = grid.getRowCount();

        constraintList.add(new RowConstraints());
        CheckBox checkBox = createCheckBox(name);
        checkBox.allowIndeterminateProperty().set(false);
        checkBox.paddingProperty().set(insets == null ? new Insets(0, 0, 0, 0) : insets);
        checkBox.setUserData(technique);
        grid.add(checkBox, 0, row);
        checkBoxes.put(name, checkBox);

        if (!description.equals("")) {
            RowConstraints rowConstraint = new RowConstraints();
            constraintList.add(rowConstraint);
            Label label = createLabel(description);
            grid.add(label, 0, row + 1);
        }
        return checkBox;
    }

    private CheckBox createCheckBox(String name) {
        CheckBox checkBox = new CheckBox(name);
        checkBox.setText(name);
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

    public boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(boolean value) {
        allChecked = value;
        if (value == true) {
            toggle.setGraphic(checkedGlyph);
            toggle.setTooltip(checkedTooltip);
        }
        else {
            toggle.setGraphic(uncheckedGlyph);
            toggle.setTooltip(uncheckedTooltip);
        }
    }

    public Collection<CheckBox> getCheckBoxes() {
        return checkBoxes.values();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        toggle.setTooltip(checkedTooltip);
        toggle.setGraphic(checkedGlyph);
        toggle.setOnMouseClicked(event -> ToggleTechniquesController.ToggleTechniques());
    }
}
