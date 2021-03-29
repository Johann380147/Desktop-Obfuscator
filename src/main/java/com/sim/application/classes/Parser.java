package com.sim.application.classes;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Parser
{
    private static ProjectRoot projectRoot;
    private static Path projectDir;
    private static Map<String, CompilationUnit> parsedCompilationUnits;
    private static final SymbolSolverCollectionStrategy collectionStrategy = new SymbolSolverCollectionStrategy();

    private Parser() { }

    public static void collectSources(String projectDir) {
        Parser.projectDir = Paths.get(projectDir);
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

    public static void addSource(Path src) {
        projectRoot.addSourceRoot(src);
    }

    public static List<String> getSources() {
        return projectRoot.getSourceRoots().stream()
                .map(src -> src.getRoot().toAbsolutePath().toString())
                .collect(Collectors.toList());
    }

    public static Collection<CompilationUnit> getCompilationUnits() {
        return parsedCompilationUnits == null ? null : parsedCompilationUnits.values();
    }

    public static void setupConfig(ParserConfiguration.LanguageLevel languageLevel, Charset charEncoding) {
        collectionStrategy.getParserConfiguration()
                .setStoreTokens(true)
                .setAttributeComments(true)
                .setTabSize(4)
                .setLanguageLevel(languageLevel)
                .setCharacterEncoding(charEncoding);
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
