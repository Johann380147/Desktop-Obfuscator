package com.sim.application.controllers;

import com.sim.application.classes.Parser;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.IConsole;
import com.sim.application.views.components.IDirectoryBrowser;

public final class ClearDirectoryController {

    private static IDirectoryBrowser directory;

    private ClearDirectoryController() {}

    public static void initialize(IDirectoryBrowser directory) { ClearDirectoryController.directory = directory; }

    public static void clearDirectory() {
        if (directory != null) {
            directory.clearDirectory();
            Parser.clearCache();
            LogStateController.log("Directory cleared", Console.Status.INFO);
        }
    }
}
