package com.sim.application.views;

import com.sim.application.controllers.*;
import com.sim.application.techniques.Technique;
import com.sim.application.techniques.TechniqueManager;
import com.sim.application.views.components.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.controlsfx.glyphfont.Glyph;

import java.net.URL;
import java.util.ResourceBundle;

public class MainView implements Initializable, StageObserver {

    @FXML
    private BorderPane mainPane;
    @FXML
    private HBox bottom_container;
    @FXML
    private Label menuAbout;
    @FXML
    private DirectoryBrowser directory;
    @FXML
    private TechniqueGrid techniques;
    @FXML
    private CodeDisplay input;
    @FXML
    private CodeDisplay output;
    @FXML
    private Button expand;
    @FXML
    private Button download;
    @FXML
    private Button obfuscate;
    @FXML
    private Button obfuscateSettings;
    @FXML
    private Console console;
    @FXML
    private StatusBar statusBar;

    private Glyph upGlyph = Glyph.create("FontAwesome|ANGLE_UP");
    private Glyph downGlyph = Glyph.create("FontAwesome|ANGLE_DOWN");

    private void InitListeners() {
        download.setOnMouseClicked(event -> DownloadObfuscatedCodeController.download());
        obfuscate.setOnMouseClicked(event -> ObfuscateCodeController.obfuscate(techniques.getSelectedTechniques()));
        menuAbout.setOnMouseClicked(event -> DisplayAboutDialogController.displayDialog());
        obfuscateSettings.setOnMouseClicked(event -> DisplaySettingsDialogController.displayDialog());
        bottom_container.managedProperty().bind(bottom_container.visibleProperty());
        expand.setOnMouseClicked(event -> {
            if (bottom_container.isVisible()) {
                bottom_container.setVisible(false);
                expand.setGraphic(upGlyph);
            } else {
                bottom_container.setVisible(true);
                expand.setGraphic(downGlyph);
            }
        });
    }

    private void InitControllers() {
        ObfuscateCodeController.initialize(this, directory);
        ClearDirectoryController.initialize(directory);
        AddFileToDirectoryController.initialize(directory);
        StoreScrollPositionController.initialize(directory, input, output);
        ClearCodeDisplayController.initialize(input, output);
        DisplayUploadedCodeController.initialize(input);
        DisplayObfuscatedCodeController.initialize(output);
        ToggleTechniquesController.initialize(techniques);
        LogStateController.initialize(console);
    }

    private void InitControllersNeedingStage(Stage stage) {
        DisplayAboutDialogController.initialize(stage);
        DisplaySettingsDialogController.initialize(stage);
        DownloadObfuscatedCodeController.initialize(stage, this, directory);
        UploadCodeController.initialize(stage, this, directory);
    }

    public void disableObfuscateButton() {
        obfuscate.setDisable(true);
    }

    public void enableObfuscateButton() {
        obfuscate.setDisable(false);
    }

    public void disableDownloadButton() {
        download.setDisable(true);
    }

    public void enableDownloadButton() {
        download.setDisable(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var techniqueList = TechniqueManager.getTechniques();
        for (Technique technique : techniqueList) {
            techniques.addTechnique(technique);
        }

        InitListeners();
        InitControllers();
        StageObserver.runOnStageSet(mainPane, this::InitControllersNeedingStage);
    }
}
