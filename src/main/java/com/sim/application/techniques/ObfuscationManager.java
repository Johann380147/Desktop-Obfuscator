package com.sim.application.techniques;

import com.sim.application.classes.TechniqueMap;

import java.util.LinkedHashMap;


public final class ObfuscationManager {
    private static TechniqueMap techniques = new TechniqueMap() {{
        put(new Trimming());
        put(new NameObfuscation());
        put(new ConstantEncryption());
        put(new FlowObfuscation());
        put(new MethodObfuscation());
    }};

    private ObfuscationManager() {}

    public static LinkedHashMap<String, String> getNamesAndDescriptions() {
        LinkedHashMap<String, String> names = new LinkedHashMap<>();
        for (String name : techniques.keySet()) {
            names.put(name, techniques.get(name).getDescription());
        }
        return names;
    }

    public static byte[] run(String techniqueName, byte[] source) throws NullTechniqueException, FailedTechniqueException {
        Technique technique = techniques.get(techniqueName);
        if (technique == null) {
            throw new NullTechniqueException(techniqueName + " technique was not found");
        }

        byte[] result = technique.execute(source);
        if (result == null) {
            throw new FailedTechniqueException(technique.getName() + " failed to complete");
        }

        return result;
    }

    public static byte[] run(Iterable<String> techniqueNames, byte[] source) throws NullTechniqueException, FailedTechniqueException {
        byte[] obsCode = source;
        for (String name : techniqueNames) {
            obsCode = run(name, obsCode);
        }

        return obsCode;
    }
}