package com.sim.application.classes;

import com.sim.application.obfuscation.Technique;
import java.util.LinkedHashMap;

public class TechniqueMap extends LinkedHashMap<String, Technique> {

    public Technique put(Technique value) {
        return super.put(value.getName(), value);
    }
}
