package com.sim.application.views.components;

import com.sim.application.controllers.DisplayObfuscatedCodeController;
import com.sim.application.controllers.DownloadObfuscatedCodeController;
import com.sim.application.utils.StringUtil;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.reactfx.collection.ListModification;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputDisplay extends VBox implements Initializable, IOutputDisplay {

    @FXML
    private Label label;
    @FXML
    private CodeArea code;
    @FXML
    private Button button;

    public OutputDisplay() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "com/sim/application/views/components/CodeDisplay.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void setCode(String code) {
        this.code.replaceText(code);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DisplayObfuscatedCodeController.initialize(this);
        button.setOnMouseClicked(event -> DownloadObfuscatedCodeController.download());

        code.setParagraphGraphicFactory(LineNumberFactory.get(code));
        code.getVisibleParagraphs().addModificationObserver
        (
            new VisibleParagraphStyler<>( code, this::computeHighlighting )
        );
        // auto-indent: insert previous line's indents on enter
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );
        code.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
        {
            if ( KE.getCode() == KeyCode.ENTER ) {
                int caretPosition = code.getCaretPosition();
                int currentParagraph = code.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher( code.getParagraph( currentParagraph ).getSegments().get( 0 ) );

                if ( m0.find() ) Platform.runLater( () -> code.insertText( caretPosition + 1, m0.group() ) );
            }
        });

        // Tab indentation set to 4 spaces
        InputMap<KeyEvent> im = InputMap.consume(
                EventPattern.keyPressed(KeyCode.TAB),
                e -> code.replaceSelection("    ")
        );
        Nodes.addInputMap(code, im);
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = StringUtil.PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                            matcher.group("BRACE") != null ? "brace" :
                            matcher.group("BRACKET") != null ? "bracket" :
                            matcher.group("SEMICOLON") != null ? "semicolon" :
                            matcher.group("STRING") != null ? "string" :
                            matcher.group("COMMENT") != null ? "comment" :
                            null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>>
    {
        private final GenericStyledArea<PS, SEG, S> area;
        private final Function<String,StyleSpans<S>> computeStyles;
        private int prevParagraph, prevTextLength;

        public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area, Function<String,StyleSpans<S>> computeStyles )
        {
            this.computeStyles = computeStyles;
            this.area = area;
        }

        @Override
        public void accept( ListModification<? extends Paragraph<PS, SEG, S>> lm )
        {
            if ( lm.getAddedSize() > 0 )
            {
                int paragraph = Math.min( area.firstVisibleParToAllParIndex() + lm.getFrom(), area.getParagraphs().size()-1 );
                String text = area.getText( paragraph, 0, paragraph, area.getParagraphLength( paragraph ) );

                if ( paragraph != prevParagraph || text.length() != prevTextLength )
                {
                    int startPos = area.getAbsolutePosition( paragraph, 0 );
                    Platform.runLater( () -> area.setStyleSpans( startPos, computeStyles.apply( text ) ) );
                    prevTextLength = text.length();
                    prevParagraph = paragraph;
                }
            }
        }
    }

    public Button getButton() { return button; }

    public final String getLabel() {
        return label.textProperty().get();
    }

    public final void setLabel(String text) {
        label.textProperty().set(text);
    }

    public final StringProperty labelProperty() { return label.textProperty(); }

    public final String getButtonText() {
        return button.textProperty().get();
    }

    public final void setButtonText(String text) {
        button.textProperty().set(text);
        button.setVisible(true);
    }

    public final StringProperty buttonTextProperty() { return button.textProperty(); }
}
