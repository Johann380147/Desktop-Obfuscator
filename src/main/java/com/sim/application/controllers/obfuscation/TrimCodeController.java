package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

public final class TrimCodeController {

    private TrimCodeController() {}

    public static boolean obfuscate(CompilationUnit source) {
        for (Comment child : source.getAllContainedComments()) {
            child.remove();
        }
        return true;
    }
}
