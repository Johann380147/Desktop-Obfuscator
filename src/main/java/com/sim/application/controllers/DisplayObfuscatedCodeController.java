package com.sim.application.controllers;

import com.sim.application.entities.JavaFile;
import com.sim.application.views.components.CodeDisplay;
import javafx.scene.control.TreeItem;

public final class DisplayObfuscatedCodeController {

    private static CodeDisplay codeDisplay;

    private DisplayObfuscatedCodeController() {}

    public static void initialize(CodeDisplay outputDisplay) { DisplayObfuscatedCodeController.codeDisplay = outputDisplay; }

    public static void displayCode(TreeItem<JavaFile> newValue) {
        if (codeDisplay == null) return;
        if ((newValue == null || newValue.getValue() == null)) {
            codeDisplay.setCode("");
            return;
        }
        if (newValue.getValue().isDirectory()) return;

        JavaFile file = newValue.getValue();
        String content = file.getObfuscatedContent();
        if (content != null) {
            codeDisplay.setCode(content);
            codeDisplay.setScrollPosition(file.getOutputPos());
        } else {
            codeDisplay.setCode("");
        }
    }
}
