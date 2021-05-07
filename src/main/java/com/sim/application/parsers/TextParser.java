package com.sim.application.parsers;

import com.sim.application.entities.JavaFile;
import com.sim.application.utils.FileUtil;

import java.io.File;
import java.util.*;

public class TextParser implements StringParser {
    private final File file;
    private final String[] contents;
    private StringChangeMap<Integer, String> changeMap = new StringChangeMap<>();
    private static List<JavaFile> stashedDocuments = new ArrayList<>();

    public TextParser(File file) {
        this.file = file;
        contents = FileUtil.getFileContent(file.toPath()).split("\\R");
    }

    public StringChangeMap<Integer, String> findAllMatchingText(Map<String, String> searchMap) {
        for (var searchEntry : searchMap.entrySet()) {
            findMatchingText(searchEntry.getKey(), searchEntry.getValue());
        }
        return changeMap;
    }

    public StringChangeMap<Integer, String> findMatchingText(String searchText, String replacementText) {
        if (contents == null) return null;

        for (int lineNum = 0; lineNum < contents.length; lineNum++) {
            var line = contents[lineNum];
            //var isMethod = isMethod(searchText);
            //var numberParams = getNumberOfParameters(searchText);
            var strippedName = stripMethodParams(searchText);
            int index = line.indexOf(strippedName);

            while (index != -1) {
                if (changeMap.containsKey(lineNum)) {
                    var token = new StringToken<>(strippedName, strippedName, index, replacementText);
                    changeMap.get(lineNum).putIfAbsent(token.getNameStartIndex(), token);
                } else {
                    var token = new StringToken<>(strippedName, strippedName, index, replacementText);
                    var map = new TreeMap<Integer, StringToken<String>>();
                    map.put(token.getNameStartIndex(), token);
                    changeMap.put(lineNum, map);
                }

                if (index + searchText.length() < line.length()) {
                    index = line.indexOf(strippedName, index + strippedName.length());
                } else {
                    index = -1;
                }
            }
        }

        return changeMap;
    }

    public void replaceAll() {
        for (var change : changeMap.entrySet()) {
            int lineNum = change.getKey();
            var tokens = change.getValue();

            int lengthDiff = 0;
            for (var token : tokens.values()) {
                var str = contents[lineNum];
                var newStr = str.substring(0, token.getNameStartIndex() + lengthDiff) + token.getReplacementName() + str.substring(token.getNameEndIndex() + lengthDiff);
                contents[lineNum] = newStr;
                lengthDiff += token.getLengthDiff();
            }
        }

        var sb = new StringBuilder();
        for (var str : contents) {
            sb.append(str).append("\n");
        }
        stashedDocuments.add(new JavaFile(file.getAbsolutePath(), file, sb.toString()));
    }

    public static void stashDocument(JavaFile doc) {
        stashedDocuments.add(doc);
    }

    public static List<JavaFile> getStashedDocuments() {
        return stashedDocuments;
    }

    public static void clearStashedDocuments() {
        stashedDocuments.clear();
    }
}
