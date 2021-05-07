package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.sim.application.entities.JavaFile;
import com.sim.application.parsers.JParser;
import com.sim.application.parsers.StringParser;
import com.sim.application.parsers.TextParser;
import com.sim.application.parsers.XmlParser;
import com.sim.application.techniques.ClassMap;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Problem;
import com.sim.application.techniques.Technique;
import com.sim.application.utils.FileUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChangeConfigurationFilesController extends Technique {
    private static ChangeConfigurationFilesController instance;
    private final String name = "Include configuration files (e.g. pom.xml)";
    private final String description = "";

    public static ChangeConfigurationFilesController getInstance() {
        if (instance == null) {
            instance = new ChangeConfigurationFilesController();
        }
        return instance;
    }

    private ChangeConfigurationFilesController() {}

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
            var projectDir = JParser.getProjectDir();
            if (projectDir == null) return;

            File file = new File(projectDir);
            FileChanger fileChanger = new FileChanger(file, classMap);
            fileChanger.begin();

        } catch (Exception e) {
            e.printStackTrace();
            throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
        }
    }

    private static class FileChanger implements StringParser {
        private final File rootFile;
        private final Map<String, String> nameMap;
        private final Set<String> classSet;

        private FileChanger(File rootFile, ClassMap classMap) {
            this.rootFile = rootFile;
            this.nameMap = getRelevantNames(classMap);
            this.classSet = nameMap.keySet();
        }

        void begin() {
            if (rootFile.exists()) {
                File[] fileList = rootFile.listFiles();
                if (fileList != null) {
                    for (File file : fileList) {
                        iterateFiles(file);
                    }
                }
            }
        }

        private void iterateFiles(File file) {
            if (file == null) return;

            if (file.isDirectory()) {
                File[] fileList = file.listFiles();
                if (fileList != null) {
                    for (File child : fileList) {
                        iterateFiles(child);
                    }
                }
            } else if ("xml".equals(FileUtil.getFileExt(file.toPath()))) {
                searchAndChangeXml(file);
            } else if ("bat".equals(FileUtil.getFileExt(file.toPath())) ||
                      ("config").equals(FileUtil.getFileExt(file.toPath()))) {
                searchAndChangeText(file);
            }
        }

        private void searchAndChangeXml(File file) {
            try {
                int changeCount = 0;
                XmlParser xmlParser = new XmlParser(file);
                for (var name : classSet) {
                    var strippedName = stripMethodParams(name);
                    var elements = xmlParser.findElementsContainingText(strippedName);
                    for (var element : elements) {
                        String replacementText = nameMap.get(name);
                        if (replacementText != null) {
                            XmlParser.replaceText(element, replacementText);
                            changeCount ++;
                        }
                    }
                }
                if (changeCount > 0) {
                    XmlParser.stashDocument(xmlParser.getDocument());
                }

            } catch (Exception ignored) { }
        }

        private void searchAndChangeText(File file) {
            try {
                TextParser textParser = new TextParser(file);
                var changeList = textParser.findAllMatchingText(nameMap);
                if (changeList.size() > 0) {
                    textParser.replaceAll();
                }
            } catch (Exception ignored) { }
        }

        private Map<String, String> getRelevantNames(ClassMap classMap) {
            return classMap.entrySet().stream()
                    .filter(map -> map.getKey().matches("[^\\d][^\\s]*([(].*[)])?"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
