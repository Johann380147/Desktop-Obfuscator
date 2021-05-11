package com.sim.application.controllers;

import com.sim.application.views.components.CodeDisplay;
import com.sim.application.views.components.DirectoryBrowser;

public final class StoreScrollPositionController {

    private static DirectoryBrowser directory;
    private static CodeDisplay input;
    private static CodeDisplay output;

    private StoreScrollPositionController() {}

    public static void initialize(DirectoryBrowser directory, CodeDisplay input, CodeDisplay output) {
        StoreScrollPositionController.directory = directory;
        StoreScrollPositionController.input = input;
        StoreScrollPositionController.output = output;
    }

    public static void StorePosition() {
        if (directory == null || input == null || output == null) return;

        var node = directory.getCurrentSelection();
        if (node == null) return;

        var file = node.getValue();
        if (file == null || file.isDirectory()) return;

        file.setInputPos(input.getScrollPosition());
        file.setOutputPos(output.getScrollPosition());
    }
}
