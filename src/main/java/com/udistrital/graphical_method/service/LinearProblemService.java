package com.udistrital.graphical_method.service;

import org.springframework.stereotype.Service;

import com.udistrital.graphical_method.entity.ObjectiveFunction;
import com.udistrital.graphical_method.entity.Restriction;
import com.udistrital.graphical_method.entity.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LinearProblemService {

    public Map<String, Object> getMax(ObjectiveFunction objectiveFunction, List<Map<String, Double>> intersections) {
        Double max = Double.NEGATIVE_INFINITY;
        int maxIndex = -1;
        for (int i = 0; i < intersections.size(); i++) {
            Double result = objectiveFunction.evaluate(intersections.get(i));
            if (result > max) {
                max = result;
                maxIndex = i;
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("value", max);
        resultMap.put("index", maxIndex);
        return resultMap;
    }

    public Map<String, Object> getMin(ObjectiveFunction objectiveFunction, List<Map<String, Double>> intersections) {
        Double min = Double.POSITIVE_INFINITY;
        int minIndex = -1;
        for (int i = 0; i < intersections.size(); i++) {
            Double result = objectiveFunction.evaluate(intersections.get(i));
            if (result < min) {
                min = result;
                minIndex = i;
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("value", min);
        resultMap.put("index", minIndex);
        return resultMap;
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

    public Map<String, Double> calculateIntersections(Restriction restriction) {
        Map<String, Double> intersections = new HashMap<>();

        double constant = restriction.getConstant();
        List<Term> terms = restriction.getTerms();

        double xCoefficient = 0;
        boolean hasY = false;

        for (Term term : terms) {
            if (term.getVariable().equals("x")) {
                xCoefficient = term.getCoefficient();
            } else if (term.getVariable().equals("y")) {
                hasY = true;
                constant -= term.getCoefficient() * 0;
            }
        }

        if (xCoefficient != 0) {
            intersections.put("x", constant / xCoefficient);
        } else {
            intersections.put("x", 0.0);
        }
        constant = restriction.getConstant();
        double yCoefficient = 0;
        boolean hasX = false;

        for (Term term : terms) {
            if (term.getVariable().equals("y")) {
                yCoefficient = term.getCoefficient();
            } else if (term.getVariable().equals("x")) {
                hasX = true;
                constant -= term.getCoefficient() * 0;
            }
        }

        if (yCoefficient != 0) {
            intersections.put("y", constant / yCoefficient);
        } else {
            intersections.put("y", 0.0);
        }

        return intersections;
    }

    public List<Restriction> parseRestrictions(List<String> restrictionsText) {
        List<Restriction> restrictions = new ArrayList<>();
        String termPattern = "([+-]?\\d*\\.?\\d*)?([a-zA-Z]+)";
        Pattern pattern = Pattern.compile(termPattern);

        for (String restrictionText : restrictionsText) {
            String operator;
            if (restrictionText.contains(">=")) {
                operator = ">=";
            } else if (restrictionText.contains("<=")) {
                operator = "<=";
            } else if (restrictionText.contains("=")) {
                operator = "=";
            } else {
                throw new IllegalArgumentException("Operador de restricción no válido en: " + restrictionText);
            }
            String[] parts = restrictionText.split(operator);
            String leftPart = parts[0].replace(" ", "");
            double constant = Double.parseDouble(parts[1].trim());
            Matcher matcher = pattern.matcher(leftPart);
            List<Term> terms = new ArrayList<>();
            while (matcher.find()) {
                String coefStr = matcher.group(1);
                double coefficient = coefStr == null || coefStr.isEmpty() ? 1 : Double.parseDouble(coefStr);
                String variable = matcher.group(2);
                terms.add(new Term(coefficient, variable));
            }
            restrictions.add(new Restriction(terms, operator, constant));
        }

        return restrictions;
    }

}
