package com.sim.application.controllers;

import com.sim.application.views.components.CodeDisplay;

public class ClearCodeDisplayController {
    private static CodeDisplay input;
    private static CodeDisplay output;

    private ClearCodeDisplayController() {}

    public static void initialize(CodeDisplay input, CodeDisplay output) {
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
