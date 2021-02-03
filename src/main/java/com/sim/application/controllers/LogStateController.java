package com.sim.application.controllers;

import com.sim.application.views.components.ConsoleImpl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class LogStateController {

    private static ConsoleImpl console;

    private LogStateController() {}

    public static void initialize(ConsoleImpl consoleImpl) { console = consoleImpl; }

    public static void log(String content, ConsoleImpl.Status status) {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'['hh:mm:ss']'");
        if (console != null) {
            console.addLog(time.format(formatter), content, status);
        }
    }
}
