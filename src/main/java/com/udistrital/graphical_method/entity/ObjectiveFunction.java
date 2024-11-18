package com.udistrital.graphical_method.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Double evaluate(Map<String, Double> variableValues) {
        double result = constant;
        for (Term term : terms) {
            Double value = variableValues.getOrDefault(term.getVariable(), 0.0);
            result += term.getCoefficient() * value;
        }
        return result;
    }

    public ObjectiveFunction(List<Term> terms) {
        this.terms = terms;
    }

    public ObjectiveFunction parseObjectiveFunction(String objectiveFunctionText) {
        objectiveFunctionText = objectiveFunctionText.replace(" ", "");
        List<Term> terms = new ArrayList<>();
        String expression = "([+-]?\\d*\\.?\\d*)?([a-zA-Z]+)";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(objectiveFunctionText);
        while (matcher.find()) {
            String coefStr = matcher.group(1);
            double coefficient = coefStr == null || coefStr.isEmpty() ? 1 : Double.parseDouble(coefStr);
            String variable = matcher.group(2);
            terms.add(new Term(coefficient, variable));
        }

        return new ObjectiveFunction(terms);
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