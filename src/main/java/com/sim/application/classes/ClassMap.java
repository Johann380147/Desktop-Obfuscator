package com.sim.application.classes;

import java.util.HashMap;

public class ClassMap extends HashMap<String, String> {

    private static final long serialVersionUID = -4849000664306954505L;
    private HashMap<String, String> valueMap = new HashMap<>();

    @Override
    public String put(String from, String to) {
        valueMap.put(to, "");
        return super.put(from, to);
    }

    public boolean containsValue(String value) {
        return valueMap.containsKey(value);
    }
}
