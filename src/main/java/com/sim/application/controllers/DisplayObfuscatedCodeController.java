package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.views.components.ICodeDisplay;
import javafx.scene.control.TreeItem;

import java.nio.charset.StandardCharsets;

public final class DisplayObfuscatedCodeController {

    private static ICodeDisplay codeDisplay;

    private DisplayObfuscatedCodeController() {}

    public static void initialize(ICodeDisplay outputDisplay) { DisplayObfuscatedCodeController.codeDisplay = outputDisplay; }

    public static void DisplayCode(TreeItem<JavaFile> newValue) {
        if (codeDisplay == null) return;
        if ((newValue == null || newValue.getValue() == null)) {
            codeDisplay.setCode("");
            return;
        }
        if (newValue.getValue().isDirectory()) return;

        JavaFile file = newValue.getValue();
        byte[] content = file.getObfuscatedContent();
        if (content != null) {
            String code = new String(content, StandardCharsets.UTF_8);
            codeDisplay.setCode(code);
            codeDisplay.setScrollPosition(file.getOutputPos());
        }
    }
}
