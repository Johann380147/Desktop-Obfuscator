package com.sim.application.controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;


public final class SendFeedbackController {

    private SendFeedbackController() {}

    public static void SendFeedback() {
        openWebpage("https://fyp-21-s1-15.wixsite.com/rara-obfuscator/#comp-km799luf");
    }

    private static void openWebpage(String link) {
        if (Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)){
                URI uri = URI.create(link);
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
