package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.sim.application.techniques.Technique;

import java.util.Map;

public final class TrimCodeController extends Technique {
    private static TrimCodeController instance;
    private String name = "Trimming";
    private String description = "Removes unused class, fields, methods and whitespace";

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
    public boolean execute(CompilationUnit source, Map<String, String> classMap) {
        return true;
    }

}
