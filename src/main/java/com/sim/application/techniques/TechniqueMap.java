package com.sim.application.techniques;

import com.sim.application.techniques.Technique;
import java.util.LinkedHashMap;


public class TechniqueMap extends LinkedHashMap<String, Technique> {
    private static final long serialVersionUID = 4045363209170072101L;

    public Technique put(Technique value) {
        return super.put(value.getName(), value);
    }
}
