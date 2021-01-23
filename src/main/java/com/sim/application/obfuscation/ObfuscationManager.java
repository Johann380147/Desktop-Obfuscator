package com.sim.application.obfuscation;

import com.sim.application.classes.FailedTechniqueException;
import com.sim.application.classes.NullTechniqueException;
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

    public static void run(String techniqueName, byte[] source) throws NullTechniqueException, FailedTechniqueException {
        Technique technique = techniques.get(techniqueName);
        if (technique == null)
            throw new NullTechniqueException(techniqueName + " technique was not found");
        if (technique.execute(source) == false)
            throw new FailedTechniqueException(technique.getName() + " failed to complete");
    }

    public static void run(Iterable<String> techniqueNames, byte[] source) throws NullTechniqueException, FailedTechniqueException {
        for (String name : techniqueNames) {
            run(name, source);
        }
    }
}