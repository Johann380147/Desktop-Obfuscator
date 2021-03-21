package com.sim.application.controllers;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.Stopwatch;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Parser;
import com.sim.application.controllers.obfuscation.TrimCodeController;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.techniques.TechniqueManager;
import com.sim.application.views.IMainView;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.IConsole;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ObfuscateCodeController {

    private static IMainView mainView;
    private static IDirectoryBrowser directory;
    private static Thread thread;

    private ObfuscateCodeController() {}

    public static void initialize(IMainView mainView, IDirectoryBrowser directory) {
        ObfuscateCodeController.mainView = mainView;
        ObfuscateCodeController.directory = directory;
    }

    public static void obfuscate(List<Technique> techniques) {
        if (directory == null || mainView == null) return;
        var root = directory.getRootDirectory();
        if (root == null || techniques.size() == 0) return;

        mainView.disableObfuscateButton();
        directory.disableButtons();
        var obfuscate = new Obfuscate(techniques, root.getValue().getFullPath());
        var thread = new Thread(obfuscate);
        thread.setDaemon(true);
        thread.start();
    }

    public static class Obfuscate extends Task<Void> {

        private List<Technique> techniques;
        private String projectRoot;

        Obfuscate(List<Technique> techniques, String projectRoot) {
            this.techniques = techniques;
            this.projectRoot = projectRoot;
        }

        @Override
        public Void call() {
            Stopwatch timer = Stopwatch.createStarted();

            // Gather configuration files
            var parseComments =  !(techniques.contains(TrimCodeController.getInstance()));
            var srcDirs = directory.getProjectFiles().getSourceDirectories().stream()
                    .map(JavaFile::getFullPath)
                    .collect(Collectors.toList());
            var libFiles = directory.getProjectFiles().getLibraryFiles().stream()
                    .map(JavaFile::getFullPath)
                    .collect(Collectors.toList());
            // Create parser with specified configuration files
            Parser parser = new Parser(projectRoot, srcDirs, libFiles);

            try {
                // Try to parse files
                Platform.runLater(() -> LogStateController.log("Parsing files...", Console.Status.INFO));
                Map<String, CompilationUnit> compilationMap = parser.parse(parseComments);

                if (compilationMap == null) {
                    Platform.runLater(() -> LogStateController.log("Failed to parse files", Console.Status.ERROR));
                    return null;
                } else {
                    Platform.runLater(() -> LogStateController.log("Parsing done", Console.Status.INFO));
                }

                var sourceFiles = associateFilesToCompilationUnit(
                        directory.getProjectFiles().getJavaFiles(), compilationMap);

                // Run obfuscation techniques
                Platform.runLater(() -> LogStateController.log("Starting obfuscation", Console.Status.INFO));
                TechniqueManager.run(techniques, sourceFiles);
                Platform.runLater(() -> LogStateController.log("Obfuscation complete. Process took: " + timer.stop(), Console.Status.INFO));
                Platform.runLater(() -> DisplayObfuscatedCodeController.DisplayCode(directory.getCurrentSelection()));
            } catch (Exception e) {
                if (e instanceof IOException) {
                    Platform.runLater(() -> LogStateController.log(e.getMessage(), Console.Status.ERROR));
                } else if (e instanceof FailedTechniqueException) {
                    Platform.runLater(() -> LogStateController.log(e.getMessage(), Console.Status.ERROR));
                }
                Platform.runLater(() -> LogStateController.log("Obfuscation failed, tasks ended", Console.Status.WARNING));
            } finally {
                Platform.runLater(() -> mainView.enableObfuscateButton());
                Platform.runLater(() -> directory.enableButtons());
            }

            return null;
        }

        private Map<JavaFile, CompilationUnit> associateFilesToCompilationUnit(
                List<JavaFile> files, Map<String, CompilationUnit> units) {

            var map = new HashMap<JavaFile, CompilationUnit>();
            for (JavaFile file : files) {
                if (units.containsKey(file.getFullPath())) {
                    map.put(file, units.get(file.getFullPath()));
                }
            }
            return map;
        }
    }
}
