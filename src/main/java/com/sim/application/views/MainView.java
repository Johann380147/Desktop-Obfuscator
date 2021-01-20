package com.sim.application.views;

import com.sim.application.obfuscation.ObfuscationManager;
import com.sim.application.views.components.TechniqueGrid;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class MainView implements Initializable {

    @FXML
    private BorderPane mainPane;
    @FXML
    private TechniqueGrid techniques;
    @FXML
    private Button reset;
    @FXML
    private Button preview;
    @FXML
    private TextArea sample;
    @FXML
    private Label leftStatus;
    @FXML
    private Label rightStatus;

    private static double xOffset = 0;
    private static double yOffset = 0;

    private ArrayList<CheckBox> techniqueCheckboxes = new ArrayList<>();


    public MainView() {

    }

    private void InitListeners() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LinkedHashMap<String, String> info = ObfuscationManager.getNamesAndDescriptions();
        for (String name : info.keySet()) {
            CheckBox checkBox = techniques.addTechnique(name, info.get(name));
            techniqueCheckboxes.add(checkBox);
        }

        InitListeners();
    }
}
