package com.sim.application.controllers;

import com.sim.application.views.MainView;
import com.sim.application.views.components.Console;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class LogStateController {

    private LogStateController() {}

    public static void log(String content, Console.Status status) {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'['hh:mm:ss']'");
        MainView.getView().getConsole().addLog(time.format(formatter), content, status);
    }
}
