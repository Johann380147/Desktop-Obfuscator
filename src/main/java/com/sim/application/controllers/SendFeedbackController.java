package com.sim.application.controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;


public class SendFeedbackController {

    public static SendFeedbackController getInstance() {
        return new SendFeedbackController();
    }

    public static void SendFeedback() {
        openWebpage("https://www.google.com");
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
