package com.sim.application.controllers;

import com.sim.application.classes.Parser;
import com.sim.application.views.components.SettingsDialog;
import javafx.stage.Stage;

public class DisplaySettingsDialogController {

    private static Stage stage;

    private DisplaySettingsDialogController() {}

    public static void initialize(Stage stage) {
        DisplaySettingsDialogController.stage = stage;
    }

    public static void displayDialog() {
        var dialog = new SettingsDialog();
        dialog.show(stage,
                    Parser.getLanguageLevels(),
                    Parser.getSelectedLanguageLevel(),
                    Parser.getCharsets(),
                    Parser.getSelectedCharEncoding(),
                    Parser.getSources());
        dialog.hide();

        Parser.setupConfig(dialog.getSelectedLanguageLevel(), dialog.getSelectedCharset());
        //dialog.getProjectSources().forEach(Parser::addSource);
    }
}
