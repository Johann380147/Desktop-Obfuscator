package com.sim.application.controllers;

import com.sim.application.views.components.GlassDialog;
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

    private DisplayAboutDialogController() {}

    public static void displayDialog(Stage stage) {
        setEffect(stage, new GaussianBlur());
        GlassDialog dialog = createAboutDialog();
        dialog.show();
        setEffect(stage, new DropShadow());
    }

    private static GlassDialog createAboutDialog() {
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

    private static void setEffect(Stage stage, Effect effect) {
        stage.getScene().getRoot().setEffect(effect);
    }
}
