package com.sim.application.classes;

import com.github.javaparser.ast.Node;

import java.util.HashMap;
import java.util.HashSet;

public class ClassMap extends HashMap<String, String> {

    private static final long serialVersionUID = -4849000664306954505L;
    private HashSet<String> qualifiedNameMap = new HashSet<>();
    private HashSet<String> nameMap = new HashSet<>();
    private HashMap<String, Node> associatedNodeMap = new HashMap<>();

    @Override
    public String put(String from, String to) {
        qualifiedNameMap.add(to);
        nameMap.add(getName(to));
        return super.put(from, to);
    }

    public String put(String from, String to, Node associatedNode) {
        qualifiedNameMap.add(to);
        nameMap.add(getName(to));
        associatedNodeMap.put(from, associatedNode);
        return super.put(from, to);
    }

    public Node getAssociatedNode(String from) {
        return associatedNodeMap.get(from);
    }

    private String getName(String name) {
        var arr = name.split("\\.");
        var str = arr[arr.length - 1];
        var arr2 = str.split(" ");
        str = arr2[arr2.length - 1];
        return str;
    }

    public boolean containsValue(String qualifiedName) {
        var name = getName(qualifiedName);
        return (qualifiedNameMap.contains(qualifiedName) || nameMap.contains(name));
    }
}
