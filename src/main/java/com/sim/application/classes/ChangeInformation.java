package com.sim.application.classes;

import com.github.javaparser.ast.Node;

public class ChangeInformation {
    private Node nodeToChange;
    private String qualifiedName;
    private String scope;
    private String qualifiedScope;
    private boolean isEnum;


    public ChangeInformation() { }

    public ChangeInformation(Node nodeToChange, String qualifiedName) {
        this(nodeToChange, qualifiedName, null, null);
    }

    public ChangeInformation(Node nodeToChange, String qualifiedName, String scope) {
        this(nodeToChange, qualifiedName, scope, null);
    }

    public ChangeInformation(Node nodeToChange, String qualifiedName, String scope, String qualifiedScope) {
        this(nodeToChange, qualifiedName, scope, qualifiedScope, false);
    }

    public ChangeInformation(Node nodeToChange, String qualifiedName, String scope, String qualifiedScope, boolean isEnum) {
        this.nodeToChange = nodeToChange;
        this.qualifiedName = qualifiedName;
        this.scope = scope;
        this.qualifiedScope = qualifiedScope;
        this.isEnum = isEnum;
    }

    public Node getNode() {
        return nodeToChange;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getScope() {
        return scope;
    }

    public String getQualifiedScope() {
        return qualifiedScope;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public void setNodeToChange(Node nodeToChange) {
        this.nodeToChange = nodeToChange;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setQualifiedScope(String qualifiedScope) {
        this.qualifiedScope = qualifiedScope;
    }
}
