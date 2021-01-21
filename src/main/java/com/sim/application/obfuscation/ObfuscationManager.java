package com.sim.application.obfuscation;

import com.sim.application.classes.TechniqueMap;

import java.util.LinkedHashMap;
import java.util.Objects;


public final class ObfuscationManager {
    private static ObfuscationManager manager;
    private static TechniqueMap techniques = new TechniqueMap() {{
        put(new Trimming());
        put(new NameObfuscation());
        put(new ConstantEncryption());
        put(new FlowObfuscation());
        put(new MethodObfuscation());
    }};

    private ObfuscationManager() {}

    public static ObfuscationManager getInstance() {
        return Objects.requireNonNullElseGet(manager, ObfuscationManager::new);
    }

    public static LinkedHashMap<String, String> getNamesAndDescriptions() {
        LinkedHashMap<String, String> names = new LinkedHashMap<>();
        for (String name : techniques.keySet()) {
            names.put(name, techniques.get(name).getDescription());
        }
        return names;
    }

    public static void run(Iterable<String> techniqueNames, byte[] source) throws Exception {
        for (String name : techniqueNames) {
            Technique technique = techniques.get(name);
            if (technique != null)
                if (technique.execute(source) == false) {
                    throw new Exception(technique.getName() + " failed to complete");
                }
        }
    }
}