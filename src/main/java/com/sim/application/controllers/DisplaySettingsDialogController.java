package com.sim.application.controllers;

import com.sim.application.parsers.JParser;
import com.sim.application.views.components.SettingsDialog;
import javafx.stage.Stage;

public final class DisplaySettingsDialogController {

    private static Stage stage;

    private DisplaySettingsDialogController() {}

    public static void initialize(Stage stage) {
        DisplaySettingsDialogController.stage = stage;
    }

    public static void displayDialog() {
        var dialog = new SettingsDialog();
        dialog.show(stage,
                    JParser.getLanguageLevels(),
                    JParser.getSelectedLanguageLevel(),
                    JParser.getCharsets(),
                    JParser.getSelectedCharEncoding(),
                    JParser.getSources());
        dialog.hide();

        JParser.setupConfig(dialog.getSelectedLanguageLevel(), dialog.getSelectedCharset());
        //dialog.getProjectSources().forEach(Parser::addSource);
    }
}
