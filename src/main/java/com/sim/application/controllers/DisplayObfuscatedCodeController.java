package com.sim.application.controllers;

import com.sim.application.classes.JavaFile;
import com.sim.application.views.components.IOutputDisplay;
import javafx.scene.control.TreeItem;

import java.nio.charset.StandardCharsets;

public final class DisplayObfuscatedCodeController {

    private static IOutputDisplay codeDisplay;

    private DisplayObfuscatedCodeController() {}

    public static void initialize(IOutputDisplay outputDisplay) { DisplayObfuscatedCodeController.codeDisplay = outputDisplay; }

    public static void DisplayCode(TreeItem<JavaFile> newValue) {
        if (newValue != null && newValue.getValue() != null) {
            if (newValue.getValue().isDirectory()) return;
            String code = "";
            byte[] content = newValue.getValue().getObfuscatedContent();
            if (content != null) {
                code = new String(content, StandardCharsets.UTF_8);
                if (codeDisplay != null) {
                    codeDisplay.setCode(code);
                }
            }
        }
        else {
            if (codeDisplay != null) {
                codeDisplay.setCode("");
            }
        }
    }
}
