package com.sim.application.controllers;

import com.sim.application.views.MainView;
import com.sim.application.views.components.TechniqueGrid;
import javafx.scene.control.CheckBox;

public final class ToggleTechniquesController {

    private ToggleTechniquesController() {}

    public static void ToggleTechniques() {
        TechniqueGrid techniques = MainView.getView().getTechniques();
        if (techniques.getAllChecked() == true) {
            checkAll(techniques.getCheckBoxes());
            techniques.setAllChecked(false);
        }
        else {
            resetCheckboxes(techniques.getCheckBoxes());
            techniques.setAllChecked(true);
        }
    }

    private static void checkAll(Iterable<CheckBox> checkBoxes) {
        for(CheckBox checkBox : checkBoxes) {
            checkBox.setSelected(true);
        }
    }

    private static void resetCheckboxes(Iterable<CheckBox> checkBoxes) {
        for(CheckBox checkBox : checkBoxes) {
            checkBox.setSelected(false);
        }
    }
}
