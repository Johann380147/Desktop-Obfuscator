package com.sim.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("com/sim/application/views/MainView.fxml"));
        primaryStage.getIcons().add(new Image("images/favicon.png"));
        primaryStage.setTitle("Obfuscator");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(new Scene(root, Color.TRANSPARENT));

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
