package com.sim.application.parsers;

public class Parser {

    public static void clearStashedDocuments() {
        JParser.clearCache();
        XmlParser.clearStashedDocuments();
        TextParser.clearStashedDocuments();
    }
}
