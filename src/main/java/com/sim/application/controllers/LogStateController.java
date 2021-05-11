package com.sim.application.controllers;

import com.sim.application.views.components.Console;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class LogStateController {

    private static Console console;

    private LogStateController() {}

    public static void initialize(Console console) { LogStateController.console = console; }

    public static void log(String content, Console.Status status) {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'['hh:mm:ss']'");
        if (console != null) {
            console.addLog(time.format(formatter), content, status);
        }
    }
}
