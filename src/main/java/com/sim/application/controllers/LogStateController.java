package com.sim.application.controllers;

import com.sim.application.views.MainView;
import com.sim.application.views.components.Console;

public final class LogStateController {

    private LogStateController() {}

    public static void log(String content, Console.Status status) {
        MainView.getView().getConsole().addLog(content, status);
    }
}
