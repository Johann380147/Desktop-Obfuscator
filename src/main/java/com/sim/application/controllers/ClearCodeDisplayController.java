package com.sim.application.controllers;

import com.sim.application.views.components.ICodeDisplay;

public class ClearCodeDisplayController {
    private static ICodeDisplay input;
    private static ICodeDisplay output;

    private ClearCodeDisplayController() {}

    public static void initialize(ICodeDisplay input, ICodeDisplay output) {
        ClearCodeDisplayController.input = input;
        ClearCodeDisplayController.output = output;
    }

    public static void clearDisplay() {
        if (input != null) {
            input.setCode("");
        }
        if (output != null) {
            output.setCode("");
        }
    }
}
