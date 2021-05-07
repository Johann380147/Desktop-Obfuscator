package com.sim.application.parsers;


public class StringToken<T> {
    private T originalString;
    private int nameStartIndex;
    private int nameEndIndex;
    private int lengthDiff;
    private String replacementName;

    public StringToken(T originalString, String qualifiedName, int startIndex, String replacementQualifiedName) {
        this.originalString = originalString;
        this.replacementName = getName(replacementQualifiedName);
        var name = getName(qualifiedName);
        this.nameStartIndex = startIndex + qualifiedName.indexOf(name);
        this.nameEndIndex = nameStartIndex + name.length();
        this.lengthDiff = this.replacementName.length() - name.length();
    }

    public int getNameStartIndex() {
        return nameStartIndex;
    }

    public int getNameEndIndex() {
        return nameEndIndex;
    }

    public int getLengthDiff() {
        return lengthDiff;
    }

    public T getOriginalString() {
        return originalString;
    }

    public String getReplacementName() {
        return replacementName;
    }

    public String getName(String qualifiedName) {
        var arr = qualifiedName.split("\\.");
        if (arr.length > 0) {
            return arr[arr.length - 1];
        } else {
            return qualifiedName;
        }
    }
}