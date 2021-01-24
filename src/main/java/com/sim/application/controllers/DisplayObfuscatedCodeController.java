package com.sim.application.controllers;

import com.sim.application.classes.File;
import com.sim.application.views.MainView;
import javafx.scene.control.TreeItem;

import java.nio.charset.StandardCharsets;

public final class DisplayObfuscatedCodeController {

    private DisplayObfuscatedCodeController() {}

    public static void DisplayCode(TreeItem<File> newValue) {
        if (newValue != null && newValue.getValue() != null) {
            if (newValue.getValue().isFolder()) return;
            String code = "";
            byte[] content = newValue.getValue().getObfuscatedContent();
            if (content != null) {
                code = new String(content, StandardCharsets.UTF_8);
                MainView.getView().getAfter().setCode(code);
            }
        }
        else {
            MainView.getView().getAfter().setCode("");
        }
    }
}
