package com.sim.application.controllers;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sim.application.entities.JavaFile;
import com.sim.application.parsers.Parser;
import com.sim.application.techniques.Technique;
import com.sim.application.techniques.TechniqueManager;
import com.sim.application.views.IMainView;
import com.sim.application.views.components.IConsole;
import com.sim.application.views.components.IDirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;

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
        if (directory.getProjectFiles().size() == 0) {
            LogStateController.log("There are no .java files present in this directory", IConsole.Status.WARNING);
            return;
        }

        mainView.disableObfuscateButton();
        mainView.disableDownloadButton();
        directory.disableButtons();
        directory.removeFilesAddedPostObfuscation();
        var obfuscate = new Obfuscate(techniques);
        var thread = new Thread(obfuscate);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        thread.start();
    }

    public static class Obfuscate extends Task<Void> {

        private List<Technique> techniques;

        Obfuscate(List<Technique> techniques) {
            this.techniques = techniques;
        }

        @Override
        public Void call() {
            Stopwatch timer = Stopwatch.createStarted();
            // Create parser with specified configuration files

            try {
                // Try to parse files
                log("Parsing files...", IConsole.Status.INFO);
                Map<String, CompilationUnit> compilationMap = Parser.parse();

                if (compilationMap.size() == 0) {
                    log("Failed to parse files", IConsole.Status.ERROR);
                    return null;
                } else {
                    log("Parsing done", IConsole.Status.INFO);
                }

                var sourceFiles = associateFilesToCompilationUnit(
                        directory.getProjectFiles(), compilationMap);

                // Run obfuscation techniques
                log("Running obfuscation", IConsole.Status.INFO);
                TechniqueManager.run(techniques, sourceFiles, (technique) ->
                    log(technique.getName() + " done", IConsole.Status.INFO)
                );
                JavaFile.setProjectObfuscated(true);
                log("Obfuscation complete. Process took: " + timer.stop(), IConsole.Status.INFO);
                Platform.runLater(() -> DisplayObfuscatedCodeController.displayCode(directory.getCurrentSelection()));
            } catch (Throwable e) {
                e.printStackTrace();
                StringBuilder sb = new StringBuilder();
                sb.append(e.toString()).append("\n").append(e.getMessage()).append("\n");
                for (var st : e.getStackTrace()) {
                    sb.append(st.toString() + "\n");
                }
                log(sb.toString(), IConsole.Status.ERROR);
                log("Obfuscation failed, tasks ended", IConsole.Status.WARNING);
            } finally {
                Platform.runLater(() -> {
                    mainView.enableObfuscateButton();
                    mainView.enableDownloadButton();
                    directory.enableButtons();
                });
            }

            return null;
        }

        private BiMap<JavaFile, CompilationUnit> associateFilesToCompilationUnit(
                List<JavaFile> files, Map<String, CompilationUnit> units) {

            HashBiMap<JavaFile, CompilationUnit> map = HashBiMap.create();
            for (JavaFile file : files) {
                if (units.containsKey(file.getFullPath())) {
                    file.reset();
                    map.putIfAbsent(file, units.get(file.getFullPath()));
                }
            }
            return map;
        }

        private static void log(String msg, IConsole.Status status) {
            Platform.runLater(() -> LogStateController.log(msg, status));
        }
    }
}
