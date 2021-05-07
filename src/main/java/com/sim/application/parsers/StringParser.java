package com.sim.application.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface StringParser {

    default boolean isMethod(String qualifiedSignature) {
        var index = qualifiedSignature.indexOf("(");
        return index != -1;
    }

    default String stripMethodParams(String qualifiedSignature) {
        var index = qualifiedSignature.indexOf("(");
        String str = qualifiedSignature;
        if (index != -1) {
            str = str.substring(0, index);
        }
        return str;
    }

    default int getNumberOfParameters(String qualifiedSignature) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(qualifiedSignature);
        String params = "";
        if (matcher.find()) {
            params = matcher.group(1).trim();
            params = params.replaceAll("<.*>", "");
        }
        if (params.equals("")) {
            return 0;
        } else {
            var arr = params.split(",");
            return arr.length;
        }
    }
}
