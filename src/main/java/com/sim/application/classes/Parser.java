package com.sim.application.classes;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Parser
{
    private static SourceRoot cache;
    private Path rootDir;
    private List<String> srcDirs = new ArrayList<>();
    private List<String> libFiles = new ArrayList<>();
    private Map<String, CompilationUnit> parsedCompilationUnits;

    public Parser(String rootDir) {
        this.rootDir = Paths.get(rootDir);
    }

    public Parser(Path rootDir) {
        this.rootDir = rootDir;
    }

    public Parser(String rootDir, String srcDir) {
        this.rootDir = Paths.get(rootDir);
        this.srcDirs.add(srcDir);
    }

    public Parser(String rootDir, List<String> srcDirs) {
        this.rootDir = Paths.get(rootDir);
        this.srcDirs.addAll(srcDirs);
    }

    public Parser(Path rootDir, String srcDir) {
        this.rootDir = rootDir;
        this.srcDirs.add(srcDir);
    }
    public Parser(Path rootDir, List<String> srcDirs) {
        this.rootDir = rootDir;
        this.srcDirs.addAll(srcDirs);
    }

    public Parser(String rootDir, List<String> srcDirs, List<String> libFiles) {
        this.rootDir = Paths.get(rootDir);
        this.srcDirs = srcDirs;
        this.libFiles = libFiles;
    }

    public Parser(Path rootDir, List<String> srcDirs, List<String> libFiles) {
        this.rootDir = rootDir;
        this.srcDirs = srcDirs;
        this.libFiles = libFiles;
    }

    public void addSourceDirectory(String srcDir) {
        this.srcDirs.add(srcDir);
    }

    public void addSourceDirectory(List<String> srcDirs) {
        this.srcDirs.addAll(srcDirs);
    }

    public void removeSourceDirectory(String srcDir) {
        this.srcDirs.removeAll(Collections.singleton(srcDir));
    }

    public void removeSourceDirectory(List<String> srcDirs) {
        this.srcDirs.removeAll(srcDirs);
    }

    public static SourceRoot getCache() {
        return cache;
    }

    public Map<String, CompilationUnit> getParsedCompilationUnits() {
        return parsedCompilationUnits;
    }

    private ParserConfiguration setupConfig(ParserConfiguration.LanguageLevel languageLevel, Charset charEncoding, boolean parseComments) throws IOException {
        // Base reflection type solver
        var combinedTypeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        // Type solver for self declared types
        for (String src : srcDirs) {
            combinedTypeSolver.add(new JavaParserTypeSolver(src));
        }
        // Type solver for jar files
        for (String lib : libFiles) {
            combinedTypeSolver.add(new JarTypeSolver(lib));
        }
        return new ParserConfiguration()
                .setStoreTokens(true)
                .setLanguageLevel(languageLevel)
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver))
                .setLexicalPreservationEnabled(true)
                .setCharacterEncoding(charEncoding)
                .setAttributeComments(parseComments);
    }

    public Map<String, CompilationUnit> parse() throws IOException {
        return parse(true);
    }

    public Map<String, CompilationUnit> parse(boolean parseComments) throws IOException {
        return parse(StandardCharsets.UTF_8, parseComments);
    }

    public Map<String, CompilationUnit> parse(Charset charEncoding, boolean parseComments) throws IOException {
        return parse(ParserConfiguration.LanguageLevel.JAVA_8, StandardCharsets.UTF_8, parseComments);
    }

    public Map<String, CompilationUnit> parse(ParserConfiguration.LanguageLevel languageLevel, Charset charEncoding, boolean parseComments) throws IOException, IllegalStateException {
        var config = setupConfig(languageLevel, charEncoding, parseComments);
        var sourceRoot = new SourceRoot(rootDir);
        sourceRoot.setParserConfiguration(config);
        sourceRoot.parse("", (localPath, absolutePath, result) -> {
            if (result.getProblems().size() > 0) {
                String problems = "";
                int count = 1;
                for (var problem : result.getProblems()) {
                    var tokenStart = problem.getLocation().get().getBegin();
                    problems += "\nProblem " + count + ":";
                    problems += "\n\t" + tokenStart;
                    problems += "\n\t" + problem.getMessage();
                    count++;
                }
                throw new IllegalStateException(absolutePath + " could not be parsed. " + problems);
            }
            result.ifSuccessful(sourceRoot::add);
            return SourceRoot.Callback.Result.DONT_SAVE;
        });
        cache = sourceRoot;
        // Maps to Map<String, CompilationUnit> the following
        // key: file's absolute path
        // value: CompilationUnit
        this.parsedCompilationUnits = sourceRoot.getCompilationUnits().stream()
                .collect(Collectors.toMap(
                        unit -> unit.getStorage().get().getPath().toAbsolutePath().toString(),
                        unit -> unit));

        return parsedCompilationUnits;
    }
}
