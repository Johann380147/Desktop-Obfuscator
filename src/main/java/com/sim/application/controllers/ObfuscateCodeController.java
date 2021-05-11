package com.sim.application.controllers;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sim.application.entities.JavaFile;
import com.sim.application.parsers.JParser;
import com.sim.application.parsers.TextParser;
import com.sim.application.parsers.XmlParser;
import com.sim.application.techniques.Technique;
import com.sim.application.techniques.TechniqueManager;
import com.sim.application.views.MainView;
import com.sim.application.views.components.Console;
import com.sim.application.views.components.DirectoryBrowser;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;

public final class ObfuscateCodeController {

    private static MainView mainView;
    private static DirectoryBrowser directory;
    private static Thread thread;

    private ObfuscateCodeController() {}

    public static void initialize(MainView mainView, DirectoryBrowser directory) {
        ObfuscateCodeController.mainView = mainView;
        ObfuscateCodeController.directory = directory;
    }

    public static void obfuscate(List<Technique> techniques) {
        if (directory == null || mainView == null) return;
        var root = directory.getRootDirectory();
        if (root == null || techniques.size() == 0) return;
        if (directory.getProjectFiles().size() == 0) {
            LogStateController.log("There are no .java files present in this directory", Console.Status.WARNING);
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

            var tempXmlFiles = XmlParser.getStashedDocuments();
            var tempTextFiles = TextParser.getStashedDocuments();
            XmlParser.clearStashedDocuments();
            TextParser.clearStashedDocuments();
            try {
                // Try to parse files
                log("Parsing files...", Console.Status.INFO);
                Map<String, CompilationUnit> compilationMap = JParser.parse();
                if (compilationMap.size() == 0) {
                    log("Failed to parse files", Console.Status.ERROR);
                    return null;
                } else {
                    log("Parsing done", Console.Status.INFO);
                }

                var sourceFiles = associateFilesToCompilationUnit(
                        directory.getProjectFiles(), compilationMap);

                // Run obfuscation techniques
                log("Running obfuscation", Console.Status.INFO);
                TechniqueManager.run(techniques, sourceFiles, (technique) ->
                    log(technique.getName() + " done", Console.Status.INFO)
                );
                JavaFile.setProjectObfuscated(true);
                log("Obfuscation complete. Process took: " + timer.stop(), Console.Status.INFO);
                Platform.runLater(() -> DisplayObfuscatedCodeController.displayCode(directory.getCurrentSelection()));
            } catch (Throwable e) {
                e.printStackTrace();
                StringBuilder sb = new StringBuilder();
                sb.append(e.toString()).append("\n").append(e.getMessage()).append("\n");
                for (var st : e.getStackTrace()) {
                    sb.append(st.toString() + "\n");
                }
                log(sb.toString(), Console.Status.ERROR);
                log("Obfuscation failed, tasks ended", Console.Status.WARNING);
                tempXmlFiles.forEach(XmlParser::stashDocument);
                tempTextFiles.forEach(TextParser::stashDocument);
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

        private static void log(String msg, Console.Status status) {
            Platform.runLater(() -> LogStateController.log(msg, status));
        }
    }
}
