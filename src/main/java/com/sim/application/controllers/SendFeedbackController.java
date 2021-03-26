package com.sim.application.controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;


public final class SendFeedbackController {

    private SendFeedbackController() {}

    public static void SendFeedback() {
        openWebpage("https://joshuakhoshauwei.wixsite.com/sandbox/#comp-km799luf");
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
