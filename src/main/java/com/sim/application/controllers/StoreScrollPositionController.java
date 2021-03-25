package com.sim.application.controllers;

import com.sim.application.views.components.ICodeDisplay;
import com.sim.application.views.components.IDirectoryBrowser;

public final class StoreScrollPositionController {

    private static IDirectoryBrowser directory;
    private static ICodeDisplay input;
    private static ICodeDisplay output;

    private StoreScrollPositionController() {}

    public static void initialize(IDirectoryBrowser directory, ICodeDisplay input, ICodeDisplay output) {
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
