package com.udistrital.graphical_method.entity;

import java.util.List;
import java.util.Map;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ObjectiveFunction {

    private List<Term> terms;
    private Double constant = 0.0;
    public Double evaluate(Map<String, Double> variables) {
        double result = 0.0;
        for (Term term : terms) {
            Double value = variables.get(term.getVariable());
            if (value == null) {
                value = 0.0; // Asignar 0 si la variable no está presente en el Map
            }
            result += term.getCoefficient() * value;
        }
        return result;
    }
    public ObjectiveFunction(List<Term> terms) {
        this.terms = terms;
    }

 

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ObjectiveFunction {\n");
        sb.append("  terms=[\n");
        for (Term term : terms) {
            sb.append("    ").append(term).append(",\n");
        }
        sb.append("  ]\n");
        sb.append("  constant=").append(constant).append("\n");
        sb.append("}");

        return sb.toString();
    }

}