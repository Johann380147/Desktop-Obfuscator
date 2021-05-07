package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.Printer;
import com.github.javaparser.printer.configuration.*;
import com.github.javaparser.utils.LineSeparator;
import com.google.common.collect.BiMap;
import com.sim.application.techniques.ClassMap;
import com.sim.application.entities.JavaFile;
import com.sim.application.techniques.Problem;
import com.sim.application.techniques.TrimPrinterVisitor;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.*;
import java.util.function.Function;

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

    private static class TrimPrinter implements Printer {
        private static final TrimPrinter trimPrinter = new TrimPrinter();
        private static PrinterConfiguration configuration = new DefaultPrinterConfiguration();
        private static Function<PrinterConfiguration, VoidVisitor<Void>> visitorFactory;

        private static boolean isInitialized = false;

        private TrimPrinter() {}

        public static TrimPrinter getInstance() {
            if (!isInitialized) {
                initialize();
                isInitialized = true;
            }
            return trimPrinter;
        }

        private static void initialize() {
            configuration.get().clear();
            visitorFactory = createDefaultVisitor();
            var indent = new Indentation(Indentation.IndentType.SPACES, 0);
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS).value(false));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.END_OF_LINE_CHARACTER).value(LineSeparator.NONE.asRawString()));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.INDENTATION).value(indent));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_JAVADOC).value(false));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.INDENT_CASE_IN_SWITCH).value(false));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.SPACE_AROUND_OPERATORS).value(false));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.MAX_ENUM_CONSTANTS_TO_ALIGN_HORIZONTALLY).value(Integer.MAX_VALUE));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.ORDER_IMPORTS).value(false));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.COLUMN_ALIGN_FIRST_METHOD_CHAIN).value(false));
            configuration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.COLUMN_ALIGN_PARAMETERS).value(false));
        }

        private static Function<PrinterConfiguration, VoidVisitor<Void>> createDefaultVisitor() {
            return (config) -> new TrimPrinterVisitor(config);
        }

        @Override
        public String print(Node node) {
            final VoidVisitor<Void> visitor = visitorFactory.apply(configuration);
            node.accept(visitor, null);
            return visitor.toString();
        }

        @Override
        public Printer setConfiguration(PrinterConfiguration configuration) {
            TrimPrinter.configuration = configuration;
            return this;
        }

        @Override
        public PrinterConfiguration getConfiguration() {
            return configuration;
        }
    }
}
