package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.*;
import com.github.javaparser.utils.LineSeparator;
import com.google.common.collect.BiMap;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.*;

public final class TrimCodeController extends Technique {
    private static TrimCodeController instance;
    private final String name = "Trimming";
    private final String description = "Removes comments, indentations and whitespace";

    public static TrimCodeController getInstance() {
        if (instance == null) {
            instance = new TrimCodeController();
        }
        return instance;
    }

    private TrimCodeController() {}

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
        var trimPrinter = TrimPrinter.getInstance();
        for (CompilationUnit unit : sourceFiles.values()) {
            for (Comment comment : unit.getAllContainedComments()) {
                comment.remove();
            }
            unit.removeComment();

            for (Comment comment : unit.getOrphanComments()) {
                unit.removeOrphanComment(comment);
            }
            unit.printer(trimPrinter);
        }
    }

    private static class TrimPrinter extends DefaultPrettyPrinter {
        private TrimPrinter() {}
        public static TrimPrinter getInstance() {
            var trimPrinter = new TrimPrinter();
            var printerConfiguration = trimPrinter.getConfiguration();
            var indent = new Indentation(Indentation.IndentType.SPACES, 0);

            getOption(printerConfiguration, DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS).get().value(false);
            getOption(printerConfiguration, DefaultPrinterConfiguration.ConfigOption.END_OF_LINE_CHARACTER).get().value(LineSeparator.NONE.asRawString());
            getOption(printerConfiguration, DefaultPrinterConfiguration.ConfigOption.INDENTATION).get().value(indent);
            getOption(printerConfiguration, DefaultPrinterConfiguration.ConfigOption.PRINT_JAVADOC).get().value(false);
            getOption(printerConfiguration, DefaultPrinterConfiguration.ConfigOption.INDENT_CASE_IN_SWITCH).get().value(false);
            getOption(printerConfiguration, DefaultPrinterConfiguration.ConfigOption.SPACE_AROUND_OPERATORS).get().value(false);
            getOption(printerConfiguration, DefaultPrinterConfiguration.ConfigOption.MAX_ENUM_CONSTANTS_TO_ALIGN_HORIZONTALLY).get().value(Integer.MAX_VALUE);
            printerConfiguration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.ORDER_IMPORTS).value(false));
            printerConfiguration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.COLUMN_ALIGN_FIRST_METHOD_CHAIN).value(false));
            printerConfiguration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.COLUMN_ALIGN_PARAMETERS).value(false));

            return trimPrinter;
        }
        private static Optional<ConfigurationOption> getOption(PrinterConfiguration config, DefaultPrinterConfiguration.ConfigOption cOption) {
            return config.get(new DefaultConfigurationOption(cOption));
        }
    }
}
