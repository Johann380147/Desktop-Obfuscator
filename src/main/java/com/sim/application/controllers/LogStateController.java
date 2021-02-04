package com.sim.application.controllers;

import com.sim.application.views.components.IConsole;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class LogStateController {

    private static IConsole console;

    private LogStateController() {}

    public static void initialize(IConsole console) { LogStateController.console = console; }

    public static void log(String content, IConsole.Status status) {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'['hh:mm:ss']'");
        if (console != null) {
            console.addLog(time.format(formatter), content, status);
        }
    }
}
