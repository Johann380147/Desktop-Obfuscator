package com.sim.application.classes;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
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

    private void setupConfig() {
        setupConfig(true);
    }

    public static SourceRoot getCache() {
        return cache;
    }

    public Map<String, CompilationUnit> getParsedCompilationUnits() {
        return parsedCompilationUnits;
    }

    private ParserConfiguration setupConfig(boolean parseComments) {
        // Basic type solver
        var combinedTypeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        // Type solver for self declared types
        for (String src : srcDirs) {
            combinedTypeSolver.add(new JavaParserTypeSolver(src));
        }
        for (String lib : libFiles) {
            try {
                combinedTypeSolver.add(new JarTypeSolver(lib));
            } catch (IOException e) {}
        }
        var config = new ParserConfiguration()
                .setStoreTokens(true)
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver))
                .setLexicalPreservationEnabled(true)
                .setCharacterEncoding(StandardCharsets.UTF_8)
                .setAttributeComments(parseComments);
        return config;
    }

    public Map<String, CompilationUnit> parse(boolean parseComments) throws IOException {
        var config = setupConfig(parseComments);
        var sourceRoot = new SourceRoot(rootDir);
        sourceRoot.setParserConfiguration(config);

        sourceRoot.tryToParse("");
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