package com.sim.application.techniques;

import com.sim.application.classes.File;

import java.util.Collections;
import java.util.List;


public final class TechniqueManager {
    private static final List<Technique> techniques = Collections.unmodifiableList(List.of (
            new Trimming(),
            new NameObfuscation(),
            new ConstantEncryption(),
            new FlowObfuscation(),
            new MethodObfuscation()));

    private TechniqueManager() {}

    public static List<Technique> getTechniques() {
        return techniques;
    }

    public static byte[] run(Technique technique, byte[] source) throws NullTechniqueException, FailedTechniqueException {
        if (techniques.contains(technique) == false) {
            throw new NullTechniqueException(technique.getName() + " technique was not found");
        }

        byte[] result = technique.execute(source);
        if (result == null) {
            throw new FailedTechniqueException(technique.getName() + " failed to complete");
        }

        return result;
    }

    public static void run(Iterable<Technique> techniqueList, Iterable<File> fileList) throws NullTechniqueException, FailedTechniqueException {
        byte[] obsCode;
        for (Technique technique : techniqueList) {
            for (File file : fileList) {
                try {
                    if (file.getObfuscatedContent() == null)
                        obsCode = run(technique, file.getContent());
                    else
                        obsCode = run(technique, file.getObfuscatedContent());

                    file.setObfuscatedContent(obsCode);
                }
                catch (FailedTechniqueException e) {
                    e.setFileName(file.getFileName());
                    throw e;
                }
            }
        }
    }
}