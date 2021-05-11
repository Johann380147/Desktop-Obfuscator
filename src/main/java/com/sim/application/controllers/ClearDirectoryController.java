package com.sim.application.controllers;

import com.sim.application.parsers.Parser;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.DirectoryBrowser;

public final class ClearDirectoryController {

    private static DirectoryBrowser directory;

    private ClearDirectoryController() {}

    public static void initialize(DirectoryBrowser directory) { ClearDirectoryController.directory = directory; }

    public static void clearDirectory() {
        if (directory != null) {
            directory.clearDirectory();
            Parser.clearStashedDocuments();
            LogStateController.log("Directory cleared", Console.Status.INFO);
        }
    }
}
