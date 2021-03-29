package com.sim.application.views.components;

import com.sim.application.views.StageObserver;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.glyphfont.Glyph;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class TitleBar extends BorderPane implements Initializable, StageObserver {

    @FXML
    private ImageView icon;
    @FXML
    private Label title;
    @FXML
    private Button minimise;
    @FXML
    private Button maximise;
    @FXML
    private Button close;

    private static double xOffset = 0;
    private static double yOffset = 0;

    private StringProperty urlProperty = new SimpleStringProperty();
    private Glyph minimiseGlyph = Glyph.create("FontAwesome|MINUS");
    private Glyph maximiseGlyph = Glyph.create("FontAwesome|EXPAND");
    private Glyph closeGlyph = Glyph.create("FontAwesome|TIMES");

    public TitleBar() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/TitleBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public final String getTitle() {
        return title.textProperty().get();
    }

    public final void setTitle(String text) {
        title.textProperty().set(text);
    }

    public final StringProperty titleProperty() { return title.textProperty(); }

    public final String getIcon() {
        return icon.imageProperty().get().getUrl();
    }

    public final void setIcon(String url) {
        icon.imageProperty().set(new Image(url));
    }

    public final StringProperty iconProperty() { return new SimpleStringProperty(icon.imageProperty().get().getUrl()); }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        StageObserver.runOnStageSet(this, this::InitListeners);
    }

    private void InitListeners(Stage stage) {
        this.setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });
        this.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });
        minimise.setOnMouseClicked(event -> stage.setIconified(true));
        minimise.setOnMouseEntered(event -> minimise.setGraphic(minimiseGlyph));
        minimise.setOnMouseExited(event -> minimise.setGraphic(null));

        maximise.setOnMouseClicked(event -> {
            if (stage.isMaximized()) {
                stage.setMaximized(false);
            }
            else {
                stage.setMaximized(true);
            }
        });
        maximise.setOnMouseEntered(event -> maximise.setGraphic(maximiseGlyph));
        maximise.setOnMouseExited(event -> maximise.setGraphic(null));

        close.setOnMouseClicked(event -> stage.close());
        close.setOnMouseEntered(event -> close.setGraphic(closeGlyph));
        close.setOnMouseExited(event -> close.setGraphic(null));
    }
}
