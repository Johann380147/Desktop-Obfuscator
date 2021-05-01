package com.sim.application.utils;

import com.sim.application.controllers.obfuscation.ObfuscateArtController;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class StringUtil {
    private static String[] dictionary;
    private static String ALPHANUMERIC_CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
            + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)

    public static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public static String randomString(int maxLength, boolean randomLength) {
        String str = "";
        int numOfChars = randomLength ? new Random().nextInt(maxLength - 1) + 1 : maxLength;
        boolean isFirstCharacter = true;

        while (str.length() < numOfChars) {
            int offset = new Random().nextInt(ALPHANUMERIC_CHARACTERS.length());
            String substring = ALPHANUMERIC_CHARACTERS.substring(offset, offset + 1);
            if (isFirstCharacter && isInteger(substring)) {
                continue;
            } else {
                str += substring;
                isFirstCharacter = false;
            }
        }
        final String name = str;
        if (Arrays.stream(KEYWORDS).anyMatch(name::equals)) return randomString(maxLength, randomLength);

        return str;
    }

    public static String randomWord() {
        if (dictionary == null) {
            var result = initDictionary();
            if (result == false) return null;
        }
        UniformRandomProvider rng = RandomSource.create(RandomSource.MT);
        var word = dictionary[rng.nextInt(dictionary.length)];

        var index = rng.nextInt(3);
        switch (index) {
            case 0:
                break;
            case 1: word = "get" + capitalize(word);
                break;
            case 2: word = "set" + capitalize(word);
                break;
        }
        return word;
    }

    public static String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private static boolean initDictionary() {
        InputStream stream = ObfuscateArtController.class.getClassLoader()
                .getResourceAsStream("text/dictionary.txt");
        if (stream == null) return false;
        try {
            var words = IOUtils.toString(stream, StandardCharsets.UTF_8);
            dictionary = words.split("\\r?\\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }

    public static byte[] appendByteArray(byte[] arr1, byte[] arr2) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(arr1);
            output.write(arr2);

            return output.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
