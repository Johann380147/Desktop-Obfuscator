package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.google.common.collect.BiMap;
import com.sim.application.parsers.StringChangeMap;
import com.sim.application.parsers.StringParser;
import com.sim.application.parsers.StringToken;
import com.sim.application.techniques.ClassMap;
import com.sim.application.entities.JavaFile;
import com.sim.application.techniques.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.*;
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

                var changeMap = new StringChangeMap<StringLiteralExpr, StringLiteralExpr>();
                var visitor = new StringVisitor(unit, classMap);
                visitor.visit(unit, changeMap);

                //stringTokens.values().forEach(tokenMap -> tokenMap.values().sort((a, b) -> a.nameStartIndex - b.nameStartIndex));
                for (var change : changeMap.entrySet()) {
                    var expr = change.getKey();
                    var tokens = change.getValue();

                    int lengthDiff = 0;
                    for (var token : tokens.values()) {
                        var str = expr.asString();
                        var newStr = str.substring(0, token.getNameStartIndex() + lengthDiff) + token.getReplacementName() + str.substring(token.getNameEndIndex() + lengthDiff);
                        expr.setString(newStr);
                        lengthDiff += token.getLengthDiff();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
        }
    }

    private static class StringVisitor extends ModifierVisitor<StringChangeMap<StringLiteralExpr, StringLiteralExpr>> implements StringParser {
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

        public StringLiteralExpr visit(StringLiteralExpr stringLiteralExpr, StringChangeMap<StringLiteralExpr, StringLiteralExpr> changeMap) {
            super.visit(stringLiteralExpr, changeMap);

            String str = stringLiteralExpr.asString();
            for (var className : classSet) {
                var isMethod = isMethod(className);
                var numberParams = getNumberOfParameters(className);
                var strippedName = stripMethodParams(className);
                var index = str.indexOf(strippedName);

                while (index != -1) {
                    // Is not method or if is method, parameter length must match
                    if (!isMethod || numberParams == getNumberOfParameters(str.substring(index))) {
                        var newName = classMap.get(className);
                        if (changeMap.containsKey(stringLiteralExpr)) {
                            var token = new StringToken<>(stringLiteralExpr, strippedName, index, newName);
                            changeMap.get(stringLiteralExpr).putIfAbsent(token.getNameStartIndex(), token);
                        } else {
                            var token = new StringToken<>(stringLiteralExpr, strippedName, index, newName);
                            var map = new TreeMap<Integer, StringToken<StringLiteralExpr>>();
                            map.put(token.getNameStartIndex(), token);
                            changeMap.put(stringLiteralExpr, map);
                        }
                    }

                    if (index + className.length() < str.length()) {
                        index = str.indexOf(strippedName, index + strippedName.length());
                    } else {
                        index = -1;
                    }
                }
            }

            return  stringLiteralExpr;
        }
    }
}
