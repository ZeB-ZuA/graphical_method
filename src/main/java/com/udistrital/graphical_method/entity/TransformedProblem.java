package com.udistrital.graphical_method.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class TransformedProblem {
    private ObjectiveFunction auxiliaryObjective;
    private List<Restriction> restrictions;

   
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Auxiliary Objective: ").append(auxiliaryObjective.toString()).append("\n");
        sb.append("Transformed Restrictions:\n");
        for (Restriction r : restrictions) {
            sb.append("  ").append(r.toString()).append("\n");
        }
        return sb.toString();
    }
}
