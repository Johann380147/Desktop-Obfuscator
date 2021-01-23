package com.sim.application.views;

import com.sim.application.controllers.SendFeedbackController;
import com.sim.application.obfuscation.ObfuscationManager;
import com.sim.application.views.components.CodeDisplay;
import com.sim.application.views.components.DirectoryBrowser;
import com.sim.application.views.components.GlassDialog;
import com.sim.application.views.components.TechniqueGrid;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.controlsfx.control.HyperlinkLabel;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class MainView implements Initializable, BaseView {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Label menu_about;
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
    private Label leftStatus;
    @FXML
    private Label rightStatus;
    @FXML
    private TextFlow console;

    private static double xOffset = 0;
    private static double yOffset = 0;

    public MainView() {

    }

    private void InitListeners() {
        directory.addFileSelectionChangedListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || newValue.getValue() == null || newValue.getValue().isFolder()) return;
            String code = "";
            byte[] content = newValue.getValue().getContent();
            if (content != null) {
                code = new String(content, StandardCharsets.UTF_8);
            }
            before.setCode(code);
        });

        obfuscate.setOnMouseClicked(event -> techniques.getSelectedTechniques());
    }

    public void InitListenersNeedingStage(Stage stage) {
        menu_about.setOnMouseClicked(event -> {
            stage.getScene().getRoot().setEffect(new GaussianBlur());
            GlassDialog dialog = createAboutDialog();
            dialog.show();
            stage.getScene().getRoot().setEffect(new DropShadow());
        });
    }

    private GlassDialog createAboutDialog() {
        var dialog = new GlassDialog();
        var header = new Label("Obfuscator");
        var version = new Label("v1.2.0");
        var feedback = new HyperlinkLabel("[Feedback]");

        header.setFont(Font.font(20));
        version.setFont(Font.font(9));
        feedback.setOnAction(event -> {
            Hyperlink link = event.getSource() instanceof Hyperlink ? (Hyperlink) event.getSource() : null;
            String str = link == null ? "" : link.getText();
            if (!str.equals("")) {
                SendFeedbackController.SendFeedback();
            }
        });
        dialog.setTitle("About");
        dialog.addHorizontalContent(header, version);
        dialog.addVerticalContent(feedback, new Text("A Java obfuscator by team name"));

        return dialog;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LinkedHashMap<String, String> info = ObfuscationManager.getNamesAndDescriptions();
        for (String name : info.keySet()) {
            techniques.addTechnique(name, info.get(name));
        }
        BaseView.runOnStageSet(mainPane, stage -> InitListenersNeedingStage(stage));
        InitListeners();
    }
}
