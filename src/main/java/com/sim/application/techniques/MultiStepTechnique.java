package com.sim.application.techniques;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiStepTechnique extends Technique {
    private List<Technique> subTechniques = new ArrayList<>();

    protected abstract void setSubTechniques();

    public List<Technique> getSubTechniques() {
        return subTechniques;
    }
}
