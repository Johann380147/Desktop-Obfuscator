package com.sim.application.controllers;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.sim.application.classes.JavaFile;
import com.sim.application.controllers.obfuscation.TrimCodeController;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.NullTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.techniques.TechniqueManager;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ObfuscateCodeController {

    private static IDirectoryBrowser directory;
    private static Thread thread;

    private ObfuscateCodeController() {}

    public static void initialize(IDirectoryBrowser directory) { ObfuscateCodeController.directory = directory; }

    public static void obfuscate(List<Technique> techniques) {
        if (directory == null) return;

        var root = directory.getRootDirectory();
        if (root == null) return;

        stopThread();

        LogStateController.log("Parsing files...", Console.Status.INFO);
        //Runnable runnable = () -> {
            Map<String, CompilationUnit> compilationMap;
            if (techniques.contains(TrimCodeController.getInstance())) {
                compilationMap = ObfuscateCodeController.parse(root.getValue().getFullPath(), directory.getSrcDirs(), false);
            } else {
                compilationMap = ObfuscateCodeController.parse(root.getValue().getFullPath(),directory.getSrcDirs(), true);
            }

            if (compilationMap == null) {
                Platform.runLater(() -> LogStateController.log("Failed to parse files", Console.Status.ERROR));
            } else {
                Platform.runLater(() -> LogStateController.log("Parsing done", Console.Status.INFO));
            }

            var currFile = "";
            var classMap = new HashMap<String, String>();
            try {
                Platform.runLater(() -> LogStateController.log("Starting obfuscation", Console.Status.INFO));
                // Process 1 technique at a time to all files
                // Obfuscate (button) -> ObfuscateCodeController -> TechniqueManager ->  ObfuscateNameController
                for (Technique technique : techniques) {
                    for (JavaFile file : directory.getJavaFiles()) {
                        currFile = file.getFileName();
                        if (compilationMap.containsKey(file.getFullPath())) {
                            CompilationUnit compilationUnit = compilationMap.get(file.getFullPath());
                            TechniqueManager.run(technique, compilationUnit, classMap);
                            file.setObfuscatedContent(compilationUnit.toString().getBytes());
                        }
                    }
                }
                Platform.runLater(() -> LogStateController.log("Obfuscation complete", Console.Status.INFO));
            }
            catch (NullTechniqueException | FailedTechniqueException e) {
                if (e instanceof NullTechniqueException) {
                    Platform.runLater(() -> LogStateController.log(e.getMessage(), Console.Status.ERROR));
                }
                else if (e instanceof FailedTechniqueException) {
                    final String fileName = currFile;
                    Platform.runLater(() -> LogStateController.log(fileName + ": " + e.getMessage(), Console.Status.ERROR));
                }
                Platform.runLater(() -> LogStateController.log("Obfuscation failed, ending tasks", Console.Status.ERROR));
            }
            Platform.runLater(() -> DisplayObfuscatedCodeController.DisplayCode(directory.getCurrentSelection()));
        //};

        //startThread(runnable);
    }

    private static Map<String, CompilationUnit> parse(String source, List<JavaFile> srcDirs, boolean parseComments) {
        // Setup parsing configuration
        var sourcePath = Paths.get(source);
        var combinedTypeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        for (JavaFile src : srcDirs) {
            combinedTypeSolver.add(new JavaParserTypeSolver(src.getFullPath()));
        }
        var config = new ParserConfiguration()
                .setStoreTokens(true)
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver))
                .setLexicalPreservationEnabled(true)
                .setCharacterEncoding(StandardCharsets.UTF_8)
                .setAttributeComments(parseComments);
        var sourceRoot = new SourceRoot(sourcePath);
        sourceRoot.setParserConfiguration(config);

        try {
            // Parse all files in directory
            sourceRoot.tryToParse("");
        } catch (IOException e) {
            LogStateController.log(e.getMessage(), Console.Status.ERROR);
            return null;
        }

        // Maps to units
        // key: file's absolute path
        // value: LexicalPreservingPrinter CompilationUnit (preserves original formatting)
        var units = sourceRoot.getCompilationUnits().stream().collect(Collectors.toMap(
                unit -> unit.getStorage().get().getPath().toAbsolutePath().toString(),
                unit -> unit));
        //LexicalPreservingPrinter.setup(unit)));
        return units;
    }

    private static void startThread(Runnable target) {
        thread = new Thread(target);
        thread.start();
    }

    private static void stopThread() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
