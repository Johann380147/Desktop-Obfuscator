package com.sim.application.views;

import com.sim.application.controllers.*;
import com.sim.application.techniques.Technique;
import com.sim.application.techniques.TechniqueManager;
import com.sim.application.views.components.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainView implements Initializable, StageObserver {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Label menuAbout;
    @FXML
    private DirectoryBrowser directory;
    @FXML
    private TechniqueGrid techniques;
    @FXML
    private InputDisplay before;
    @FXML
    private OutputDisplay after;
    @FXML
    private Button obfuscate;
    @FXML
    private Console console;
    @FXML
    private StatusBar statusBar;

    private void InitListeners() {
        obfuscate.setOnMouseClicked(event -> ObfuscateCodeController.obfuscate(techniques.getSelectedTechniques()));
        menuAbout.setOnMouseClicked(event -> DisplayAboutDialogController.displayDialog());
    }

    public void InitControllersNeedingStage(Stage stage) {
        DisplayAboutDialogController.initialize(stage);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var techniqueList = TechniqueManager.getTechniques();
        for (Technique technique : techniqueList) {
            techniques.addTechnique(technique);
        }

        InitListeners();
        StageObserver.runOnStageSet(mainPane, stage -> InitControllersNeedingStage(stage));
    }
}
