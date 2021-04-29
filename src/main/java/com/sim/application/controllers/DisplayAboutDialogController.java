package com.sim.application.controllers;

import com.sim.application.views.components.AboutDialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.controlsfx.control.HyperlinkLabel;

public final class DisplayAboutDialogController {

    private static Stage stage;

    private DisplayAboutDialogController() {}

    public static void initialize(Stage stage) {
        DisplayAboutDialogController.stage = stage;
    }

    public static void displayDialog() {
        setEffect(new GaussianBlur());
        AboutDialog dialog = createAboutDialog();
        dialog.show(stage);
        setEffect(new DropShadow());
    }

    private static AboutDialog createAboutDialog() {
        var dialog = new AboutDialog();
        var header = new Label("Obfuscator");
        var version = new Label("v1.2.1");
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
        dialog.addVerticalContent(feedback, new Text("A Java obfuscator by Rara"));

        return dialog;
    }

    private static void setEffect(Effect effect) {
        if (stage != null) {
            stage.getScene().getRoot().setEffect(effect);
        }
    }
}
