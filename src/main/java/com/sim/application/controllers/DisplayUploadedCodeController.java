package com.sim.application.controllers;

import com.sim.application.classes.File;
import com.sim.application.views.components.IInputDisplay;
import javafx.scene.control.TreeItem;

import java.nio.charset.StandardCharsets;

public final class DisplayUploadedCodeController {

    private static IInputDisplay codeDisplay;

    private DisplayUploadedCodeController() {}

    public static void initialize(IInputDisplay codeDisplay) { DisplayUploadedCodeController.codeDisplay = codeDisplay; }

    public static void DisplayCode(TreeItem<File> newValue) {
        if (newValue != null && newValue.getValue() != null) {
            if (newValue.getValue().isFolder()) return;
            String code = "";
            byte[] content = newValue.getValue().getContent();
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
