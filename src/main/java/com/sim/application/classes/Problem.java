package com.sim.application.classes;

import com.github.javaparser.ast.Node;

public class Problem<T extends Node, E extends Exception> {
    private final T problemNode;
    private final E exception;
    private final String fileName;

    public Problem(T problemNode, E exception, String fileName) {
        this.problemNode = problemNode;
        this.exception = exception;
        this.fileName = fileName;
    }

    public T getProblemNode() {
        return problemNode;
    }

    public E getException() {
        return exception;
    }

    public Class<? extends Node> getNodeType() {
        return problemNode.getClass();
    }

    public Class<? extends Exception> getExceptionType() {
        return exception.getClass();
    }

    public String getErrorMsg() {
        return exception.getMessage();
    }

    @Override
    public String toString() {
        String str = "";
        var cu = problemNode.findCompilationUnit();

        str += "\n\"" + problemNode.toString() + "\"\n";
        str += "[ " + fileName;
        if (cu.isPresent()) {
            str += " | " + cu.get().getStorage().get().getFileName() + " ] : ";
        } else {
            str += " ] : ";
        }
        var range = problemNode.getRange();
        if (range.isPresent()) {
            str += problemNode.getRange().get().begin + " - " + problemNode.getRange().get().end + "\n";
        } else {
            str += "\n";
        }
        str += "[" + problemNode.getClass().getSimpleName() + "] > ";
        str += " (" + getExceptionType().getSimpleName() + ") > ";
        str += getErrorMsg();
        return str;
    }
}
