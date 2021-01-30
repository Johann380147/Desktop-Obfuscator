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
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class MainView implements Initializable, BaseView {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Label menuAbout;
    @FXML
    private DirectoryBrowser directory;
    @FXML
    private TechniqueGrid techniques;
    @FXML
    private CodeDisplay before;
    @FXML
    private CodeDisplay after;
    @FXML
    private Button obfuscate;
    @FXML
    private Console console;
    @FXML
    private StatusBar statusBar;

    private static MainView view;

    public MainView() {
        view = this;
    }

    public static MainView getView() {
        return view;
    }

    public DirectoryBrowser getDirectory() {
        return directory;
    }

    public TechniqueGrid getTechniques() {
        return techniques;
    }

    public CodeDisplay getBefore() {
        return before;
    }

    public CodeDisplay getAfter() {
        return after;
    }

    public Console getConsole() {
        return console;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    private void InitListeners() {
        directory.addClearClickedListener(event -> ClearDirectoryController.clearDirectory());
        directory.addFileSelectionChangedListener((observableValue, oldValue, newValue) -> {
            DisplayUploadedCodeController.DisplayCode(newValue);
            DisplayObfuscatedCodeController.DisplayCode(newValue);
        });

        techniques.addToggleClickedListener(event -> ToggleTechniquesController.ToggleTechniques());

        obfuscate.setOnMouseClicked(event -> ObfuscateCodeController.obfuscate(techniques.getSelectedTechniques()));
    }

    public void InitListenersNeedingStage(Stage stage) {
        directory.addBrowseClickedListener(event -> UploadCodeController.UploadCode(stage));
        menuAbout.setOnMouseClicked(event -> DisplayAboutDialogController.displayDialog(stage));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //LinkedHashMap<String, String> info = TechniqueManager.getNamesAndDescriptions();
        var techniqueList = TechniqueManager.getTechniques();
        for (Technique technique : techniqueList) {
            techniques.addTechnique(technique);
        }

        InitListeners();
        BaseView.runOnStageSet(mainPane, stage -> InitListenersNeedingStage(stage));
    }
}
