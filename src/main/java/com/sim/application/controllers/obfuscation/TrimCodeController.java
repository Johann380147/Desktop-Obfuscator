package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.util.List;
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
    public void execute(Map<JavaFile, CompilationUnit> source, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {
        for (CompilationUnit unit : source.values()) {
            for (Comment comment : unit.getAllContainedComments()) {
                comment.remove();
            }
            unit.removeComment();

            for (Comment comment : unit.getOrphanComments()) {
                unit.removeOrphanComment(comment);
            }
        }
    }

}
