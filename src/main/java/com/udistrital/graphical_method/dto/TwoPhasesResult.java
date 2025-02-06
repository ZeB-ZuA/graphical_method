package com.udistrital.graphical_method.dto;

import java.util.List;

import com.udistrital.graphical_method.entity.ObjectiveFunction;
import com.udistrital.graphical_method.entity.Restriction;

public class TwoPhasesResult {
       private ObjectiveFunction auxiliaryObjective;
    private List<Restriction> transformedRestrictions;

    public TwoPhasesResult(ObjectiveFunction auxiliaryObjective, List<Restriction> transformedRestrictions) {
        this.auxiliaryObjective = auxiliaryObjective;
        this.transformedRestrictions = transformedRestrictions;
    }

    public ObjectiveFunction getAuxiliaryObjective() {
        return auxiliaryObjective;
    }

    public List<Restriction> getTransformedRestrictions() {
        return transformedRestrictions;
    }
}
