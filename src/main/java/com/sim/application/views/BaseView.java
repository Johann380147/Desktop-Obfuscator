package com.sim.application.views;

import javafx.scene.Node;
import javafx.stage.Stage;

public interface BaseView {

    interface Executable {
        void execute(Stage stage);
    }

    public static void runOnStageSet(Node node, Executable exc) {
        node.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                // scene is set for the first time. Now its the time to listen stage changes.
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        exc.execute((Stage)newWindow);
                    }
                });
            }
        });
    }
}
