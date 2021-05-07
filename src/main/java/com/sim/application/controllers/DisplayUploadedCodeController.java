package com.sim.application.controllers;

import com.sim.application.entities.JavaFile;
import com.sim.application.views.components.ICodeDisplay;
import javafx.scene.control.TreeItem;

public final class DisplayUploadedCodeController {

    private static ICodeDisplay codeDisplay;

    private DisplayUploadedCodeController() {}

    public static void initialize(ICodeDisplay codeDisplay) { DisplayUploadedCodeController.codeDisplay = codeDisplay; }

    public static void displayCode(TreeItem<JavaFile> newValue) {
        if (codeDisplay == null) return;
        if ((newValue == null || newValue.getValue() == null)) {
            codeDisplay.setCode("");
            return;
        }
        if (newValue.getValue().isDirectory()) return;

        JavaFile file = newValue.getValue();
        String content = file.getContent();
        if (content != null) {
            codeDisplay.setCode(content);
            codeDisplay.setScrollPosition(file.getInputPos());
        } else {
            codeDisplay.setCode("");
        }
    }
}
