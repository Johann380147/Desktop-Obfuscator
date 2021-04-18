package com.sim.application.classes;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class Parser
{
    private static ProjectRoot projectRoot;
    private static Path projectDir;
    private static Map<String, CompilationUnit> parsedCompilationUnits;
    private static final SymbolSolverCollectionStrategy collectionStrategy = new SymbolSolverCollectionStrategy();

    private static ParserConfiguration.LanguageLevel selectedLanguageLevel = ParserConfiguration.LanguageLevel.JAVA_12;
    private static Charset selectedCharEncoding = StandardCharsets.UTF_8;

    private Parser() { }

    public static void init(String projectDir) {
        Parser.projectDir = Paths.get(projectDir);
        setupConfig(selectedLanguageLevel, selectedCharEncoding);
        projectRoot = collectionStrategy.collect(Parser.projectDir);
    }

    public static String getProjectDir() {
        return projectDir == null ? null : projectDir.toAbsolutePath().toString();
    }

    public static String getProjectFileName() {
        return projectDir == null ? null : projectDir.getFileName().toString();
    }

    public static void clearCache() {
        projectRoot = null;
        projectDir = null;
        parsedCompilationUnits = null;
    }

    public static void addSource(String src) {
        projectRoot.addSourceRoot(Paths.get(src));
    }

    public static SourceRoot getSourceRoot(Path sourceRoot) {
        return projectRoot.getSourceRoot(sourceRoot).orElse(null);
    }

    public static List<String> getSources() {
        if (projectRoot == null) return null;
        return projectRoot.getSourceRoots().stream()
                .map(src -> src.getRoot().toAbsolutePath().toString())
                .collect(Collectors.toList());
    }

    public static void addCompilationUnit(String sourceRootPath, CompilationUnit unit) {
        projectRoot.getSourceRoot(Paths.get(sourceRootPath)).ifPresent(sourceRoot -> {
            var key = unit.getStorage().get().getPath().toAbsolutePath().toString();
            if (!parsedCompilationUnits.containsKey(key)) {
                parsedCompilationUnits.put(key, unit);
                sourceRoot.add(unit);
            }
        });
    }

    public static void removeCompilationUnit(CompilationUnit unit) {
        var key = unit.getStorage().get().getPath().toAbsolutePath().toString();
        parsedCompilationUnits.remove(key);
    }

    public static void replaceCompilationUnit(CompilationUnit unit) {
        var key = unit.getStorage().get().getPath().toAbsolutePath().toString();
        parsedCompilationUnits.replace(key, unit);
    }

    public static Collection<CompilationUnit> getCompilationUnits() {
        return parsedCompilationUnits == null ? null : parsedCompilationUnits.values();
    }

    public static ParserConfiguration.LanguageLevel[] getLanguageLevels() {
        return ParserConfiguration.LanguageLevel.values();
    }

    public static ParserConfiguration.LanguageLevel getSelectedLanguageLevel() {
        return selectedLanguageLevel;
    }

    public static Charset[] getCharsets() {
        var charsets = Charset.availableCharsets().values();
        return charsets.toArray(new Charset[0]);
    }

    public static Charset getSelectedCharEncoding() {
        return selectedCharEncoding;
    }

    public static void setupConfig(ParserConfiguration.LanguageLevel languageLevel, Charset charEncoding) {
        collectionStrategy.getParserConfiguration()
                .setLanguageLevel(languageLevel)
                .setCharacterEncoding(charEncoding)
                .setStoreTokens(true)
                .setAttributeComments(true)
                .setTabSize(4);
        selectedLanguageLevel = languageLevel;
        selectedCharEncoding = charEncoding;
        StaticJavaParser.setConfiguration(collectionStrategy.getParserConfiguration());
    }

    public static Map<String, CompilationUnit> parse() throws IOException, IllegalStateException {
        ThrowableConsumer<SourceRoot> tc = (sourceRoot) -> {
            sourceRoot.parse("", ((localPath, absolutePath, result) -> {
                if (result.getProblems().size() > 0) {
                    StringBuilder problems = new StringBuilder();
                    int count = 1;
                    for (var problem : result.getProblems()) {
                        var tokenStart = problem.getLocation().get().getBegin();
                        problems.append("\nProblem ").append(count).append(":")
                                .append("\n\t").append(tokenStart).append("\n\t")
                                .append(problem.getMessage());
                        count++;
                    }
                    throw new IllegalStateException(absolutePath + " could not be parsed. " + problems);
                }
                result.ifSuccessful(cu ->
                    parsedCompilationUnits.put(cu.getStorage().get().getPath().toAbsolutePath().toString(), cu)
                );

                return SourceRoot.Callback.Result.DONT_SAVE;
            }));
        };
        parsedCompilationUnits = new HashMap<>();
        projectRoot.getSourceRoots().forEach(tc);

        return parsedCompilationUnits;
    }
}
