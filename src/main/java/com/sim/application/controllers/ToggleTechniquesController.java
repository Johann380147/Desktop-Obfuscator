package com.sim.application.controllers;

import com.sim.application.views.components.ITechniqueGrid;
import javafx.scene.control.CheckBox;

public final class ToggleTechniquesController {

    private static ITechniqueGrid techniques;

    private ToggleTechniquesController() {}

    public static void initialize(ITechniqueGrid techniques) { ToggleTechniquesController.techniques = techniques; }

    public static void ToggleTechniques() {
        if (techniques == null) return;
        if (techniques.getAllChecked() == true) {
            setChecked(techniques.getCheckBoxes(), true);
            techniques.setAllChecked(false);
        }
        else {
            setChecked(techniques.getCheckBoxes(), false);
            techniques.setAllChecked(true);
        }
    }

    private static void setChecked(Iterable<CheckBox> checkBoxes, boolean checked) {
        for(CheckBox checkBox : checkBoxes) {
            checkBox.setSelected(checked);
        }
    }
}
