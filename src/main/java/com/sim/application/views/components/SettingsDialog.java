package com.sim.application.views.components;

import com.github.javaparser.ParserConfiguration;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;


public class SettingsDialog extends BorderPane implements Initializable {

    @FXML
    private ComboBox<ParserConfiguration.LanguageLevel> languageComboBox;
    @FXML
    private ComboBox<Charset> charsetComboBox;
    @FXML
    private ListView<String> projectSources;
    @FXML
    private Button closeDialog;

    private Stage stage;

    private ParserConfiguration.LanguageLevel selectedLanguageLevel;
    private Charset selectedCharset;

    public SettingsDialog() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/SettingsDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent, Color.TRANSPARENT);
            stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused)
                    hide();
            });

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public ParserConfiguration.LanguageLevel getSelectedLanguageLevel() {
        return selectedLanguageLevel;
    }

    public Charset getSelectedCharset() {
        return selectedCharset;
    }

    public List<String> getProjectSources() {
        return projectSources.getItems();
    }

    public void show(Stage parent,
                     ParserConfiguration.LanguageLevel[] languageLevels,
                     ParserConfiguration.LanguageLevel selectedLanguageLevel,
                     Charset[] charsets,
                     Charset selectedCharset,
                     List<String> projectSources) {

        this.selectedLanguageLevel = selectedLanguageLevel;
        this.selectedCharset = selectedCharset;

        if (languageLevels != null) {
            languageComboBox.setItems(FXCollections.observableList(Arrays.asList(languageLevels)));
        }
        languageComboBox.setValue(selectedLanguageLevel);

        if (charsets != null) {
            charsetComboBox.setItems(FXCollections.observableList(Arrays.asList(charsets)));
        }
        charsetComboBox.setValue(selectedCharset);

        if (projectSources != null) {
            this.projectSources.setItems(FXCollections.observableList(projectSources));
        }

        // Force stage to set width and height
        stage.toBack(); stage.show(); stage.hide(); stage.toFront();
        stage.setX(parent.getX() + parent.getWidth() / 2 - stage.getWidth() / 2);
        stage.setY(parent.getY() + parent.getHeight() / 2 - stage.getHeight() / 2);
        stage.showAndWait();
    }

    public void hide() {
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.setBackground(new Background(new BackgroundFill(null,null,null)));
        languageComboBox.valueProperty().addListener((observableValue, o, t1) ->
                selectedLanguageLevel = observableValue.getValue());
        languageComboBox.setOnShowing(event -> {
            ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>)languageComboBox.getSkin();
            if (skin != null) {
                ((ListView<?>) skin.getPopupContent()).scrollTo(languageComboBox.getSelectionModel().getSelectedIndex());
            }
        });
        charsetComboBox.valueProperty().addListener((observableValue, o, t1) ->
                selectedCharset = observableValue.getValue());
        charsetComboBox.setOnShowing(event -> {
            ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>)charsetComboBox.getSkin();
            if (skin != null) {
                ((ListView<?>) skin.getPopupContent()).scrollTo(charsetComboBox.getSelectionModel().getSelectedIndex());
            }
        });
        /*this.projectSources.setEditable(true);
        this.projectSources.setCellFactory(TextFieldListCell.forListView());*/
        closeDialog.setOnMouseClicked(event -> hide());
    }
}
