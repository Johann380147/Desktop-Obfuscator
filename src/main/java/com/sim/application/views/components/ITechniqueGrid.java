package com.sim.application.views.components;

import javafx.scene.control.CheckBox;

import java.util.Collection;

public interface ITechniqueGrid {

    public boolean getAllChecked();
    public Collection<CheckBox> getCheckBoxes();
    public void setAllChecked(boolean value);
}
