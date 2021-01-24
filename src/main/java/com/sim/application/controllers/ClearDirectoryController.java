package com.sim.application.controllers;

import com.sim.application.views.MainView;
import com.sim.application.views.components.DirectoryBrowser;

public final class ClearDirectoryController {

    private ClearDirectoryController() {}

    public static void clearDirectory() {
        MainView.getView().getDirectory().setRoot(null);
    }
}
