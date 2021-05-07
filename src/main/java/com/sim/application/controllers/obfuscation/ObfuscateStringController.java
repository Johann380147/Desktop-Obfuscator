package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.google.common.collect.BiMap;
import com.sim.application.techniques.ClassMap;
import com.sim.application.entities.JavaFile;
import com.sim.application.techniques.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObfuscateStringController extends Technique {
    private static ObfuscateStringController instance;
    private final String name = "Include classes and method names found in strings";
    private final String description = "";

    public static ObfuscateStringController getInstance() {
        if (instance == null) {
            instance = new ObfuscateStringController();
        }
        return instance;
    }

    private ObfuscateStringController() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(BiMap<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {
        String currFile = "";
        try {
            // Gathering all class declarations
            for (var file : sourceFiles.keySet()) {
                var unit = sourceFiles.get(file);
                currFile = file.getFileName();

                var stringTokens = new HashMap<StringLiteralExpr, TreeMap<Integer, StringToken>>();
                var visitor = new StringVisitor(unit, classMap);
                visitor.visit(unit, stringTokens);

                //stringTokens.values().forEach(tokenMap -> tokenMap.values().sort((a, b) -> a.nameStartIndex - b.nameStartIndex));
                for (var tokenList : stringTokens.values()) {
                    int lengthDiff = 0;
                    for (var token : tokenList.values()) {
                        var str = token.getStringLiteralExpr().asString();
                        var newStr = str.substring(0, token.getNameStartIndex() + lengthDiff) + token.getReplacementName() + str.substring(token.getNameEndIndex() + lengthDiff);
                        token.getStringLiteralExpr().setString(newStr);
                        lengthDiff += token.getLengthDiff();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
        }
    }

    private static class StringVisitor extends ModifierVisitor<HashMap<StringLiteralExpr, TreeMap<Integer, StringToken>>> {
        private final CompilationUnit unit;
        private final Set<String> classSet;
        private final ClassMap classMap;

        private StringVisitor(CompilationUnit unit, ClassMap classMap) {
            this.unit = unit;
            this.classMap = classMap;
            this.classSet = classMap.keySet().stream()
                    .filter(key -> key.matches("[^\\d][^\\s]*([(].*[)])?"))
                    .collect(Collectors.toSet());
        }

        public StringLiteralExpr visit(StringLiteralExpr stringLiteralExpr, HashMap<StringLiteralExpr, TreeMap<Integer, StringToken>> stringTokens) {
            super.visit(stringLiteralExpr, stringTokens);

            var wasAdded = stringTokens.containsKey(stringLiteralExpr);
            String str = stringLiteralExpr.asString();
            for (var className : classSet) {
                var isMethod = isMethod(className);
                var numberParams = getNumberOfParameters(className);
                var strippedName = stripMethodParams(className);
                var index = str.indexOf(strippedName);

                while (index != -1) {
                    if (isMethod && numberParams != getNumberOfParameters(str.substring(index))) {
                        if (index + className.length() < str.length()) {
                            index = str.indexOf(strippedName, index + className.length());
                        } else {
                            index = -1;
                        }
                        continue;
                    }

                    var newName = classMap.get(className);
                    if (wasAdded) {
                        var token = new StringToken(stringLiteralExpr, strippedName, index, newName);
                        stringTokens.get(stringLiteralExpr).putIfAbsent(token.getNameStartIndex(), token);
                    } else {
                        var map = new TreeMap<Integer, StringToken>();
                        var token = new StringToken(stringLiteralExpr, strippedName, index, newName);
                        map.putIfAbsent(token.getNameStartIndex(), token);
                        stringTokens.put(stringLiteralExpr, map);
                        wasAdded = true;
                    }

                    if (index + className.length() < str.length()) {
                        index = str.indexOf(strippedName, index + className.length());
                    } else {
                        index = -1;
                    }
                }
            }

            return  stringLiteralExpr;
        }
    }

    private static boolean isMethod(String qualifiedSignature) {
        var index = qualifiedSignature.indexOf("(");
        return index != -1;
    }

    private static String stripMethodParams(String qualifiedSignature) {
        var index = qualifiedSignature.indexOf("(");
        String str = qualifiedSignature;
        if (index != -1) {
            str = str.substring(0, index);
        }
        return str;
    }

    private static int getNumberOfParameters(String qualifiedSignature) {
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

    private static class StringToken {
        private UUID uuid;
        private int nameStartIndex;
        private int nameEndIndex;
        private int lengthDiff;
        private StringLiteralExpr stringLiteralExpr;
        private String replacementName;

        public StringToken(StringLiteralExpr stringLiteralExpr, String className, int index, String replacementString) {
            this(UUID.randomUUID(), stringLiteralExpr, className, index, replacementString);
        }

        public StringToken(UUID uuid, StringLiteralExpr stringLiteralExpr, String className, int index, String replacementString) {
            this.uuid = uuid;
            this.stringLiteralExpr = stringLiteralExpr;
            this.replacementName = getName(replacementString);
            var name = getName(className);
            this.nameStartIndex = index + className.indexOf(name);
            this.nameEndIndex = nameStartIndex + name.length();
            this.lengthDiff = replacementName.length() - name.length();
        }

        public UUID getUuid() {
            return uuid;
        }

        public int getNameStartIndex() {
            return nameStartIndex;
        }

        public int getNameEndIndex() {
            return nameEndIndex;
        }

        public int getLengthDiff() {
            return lengthDiff;
        }

        public StringLiteralExpr getStringLiteralExpr() {
            return stringLiteralExpr;
        }

        public String getReplacementName() {
            return replacementName;
        }

        public String getName(String qualifiedName) {
            var arr = qualifiedName.split("\\.");
            if (arr.length > 0) {
                return arr[arr.length - 1];
            } else {
                return qualifiedName;
            }
        }
    }
}
