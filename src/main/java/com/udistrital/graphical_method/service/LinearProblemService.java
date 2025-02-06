package com.udistrital.graphical_method.service;

import org.springframework.stereotype.Service;

import com.udistrital.graphical_method.dto.LinearProblemException;
import com.udistrital.graphical_method.entity.ObjectiveFunction;
import com.udistrital.graphical_method.entity.Restriction;
import com.udistrital.graphical_method.entity.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LinearProblemService {

 
    private Map<String, Object> findExtremeValue(ObjectiveFunction objectiveFunction,
            List<Map<String, Double>> intersections, boolean isMax) {
        System.out.println("Buscando el " + (isMax ? "máximo" : "mínimo") + " en las intersecciones: " + intersections);
        Double extremeValue = isMax ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        int extremeIndex = -1;

        for (int i = 0; i < intersections.size(); i++) {
            Double result = objectiveFunction.evaluate(intersections.get(i));
            System.out.println("Evaluando punto " + intersections.get(i) + " -> Resultado: " + result);
            if ((isMax && result > extremeValue) || (!isMax && result < extremeValue)) {
                extremeValue = result;
                extremeIndex = i;
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("value", extremeValue);
        resultMap.put("index", extremeIndex);
        System.out.println("Resultado final: " + resultMap);
        return resultMap;
    }

    public Map<String, Object> getMax(ObjectiveFunction objectiveFunction, List<Map<String, Double>> intersections) {
        try {
            return findExtremeValue(objectiveFunction, intersections, true);
        } catch (Exception e) {
            throw new LinearProblemException("Error while calculating maximum value: " + e.getMessage());
        }
    }

    public Map<String, Object> getMin(ObjectiveFunction objectiveFunction, List<Map<String, Double>> intersections) {
        try {
            return findExtremeValue(objectiveFunction, intersections, false);
        } catch (Exception e) {
            throw new LinearProblemException("Error while calculating minimum value: " + e.getMessage());
        }
    }

    
    public ObjectiveFunction parseObjectiveFunction(String objectiveFunctionText) {
        try {
           // System.out.println("Recibí función objetivo: " + objectiveFunctionText);
            if (objectiveFunctionText == null || objectiveFunctionText.trim().isEmpty()) {
                throw new IllegalArgumentException("Objective function text cannot be empty.");
            }

            objectiveFunctionText = objectiveFunctionText.replace(" ", "");
            List<Term> terms = new ArrayList<>();
            String expression = "([+-]?\\d*\\.?\\d*)?x_(\\d+)";
            Pattern pattern = Pattern.compile(expression);
            Matcher matcher = pattern.matcher(objectiveFunctionText);

            while (matcher.find()) {
                String coefStr = matcher.group(1);
                double coefficient = coefStr == null || coefStr.isEmpty() || coefStr.equals("+") ? 1
                        : coefStr.equals("-") ? -1 : Double.parseDouble(coefStr);
                String variable = "x_" + matcher.group(2);
                terms.add(new Term(coefficient, variable));
            }

           // System.out.println("Función objetivo parseada: " + terms);
            return new ObjectiveFunction(terms);
        } catch (Exception e) {
            throw new LinearProblemException("Error while parsing objective function: " + e.getMessage());
        }
    }


    public List<Map<String, Double>> calculateAxisIntersections(Restriction restriction) {
        try {
            List<Map<String, Double>> points = new ArrayList<>();
            double constant = restriction.getConstant();
            List<Term> terms = restriction.getTerms();

            for (Term term : terms) {
                Map<String, Double> intersection = new HashMap<>();
                double coefficient = term.getCoefficient();

                if (coefficient != 0) {
                    double intersectionValue = constant / coefficient;
                    intersection.put(term.getVariable(), intersectionValue);

                    for (Term otherTerm : terms) {
                        if (!otherTerm.getVariable().equals(term.getVariable())) {
                            intersection.put(otherTerm.getVariable(), 0.0);
                        }
                    }
                    points.add(intersection);
                }
            }
            return points;
        } catch (Exception e) {
            throw new LinearProblemException("Error calculating axis intersections: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Double>> calculateAllIntersections(List<Restriction> restrictions) {
        try {
            List<Map<String, Double>> allIntersections = new ArrayList<>();

            for (int i = 0; i < restrictions.size(); i++) {
                for (int j = i + 1; j < restrictions.size(); j++) {
                    Map<String, Double> intersection = calculateIntersection(restrictions.get(i), restrictions.get(j));
                    if (intersection != null && !containsPoint(allIntersections, intersection)
                            && isFeasible(intersection, restrictions)) {
                        allIntersections.add(intersection);
                    }
                }
            }
            return allIntersections;
        } catch (Exception e) {
            throw new LinearProblemException("Error calculating all intersections: " + e.getMessage(), e);
        }
    }

    public List<Restriction> parseRestrictions(List<String> restrictionsText) {
        try {
          //  System.out.println("Recibí restricciones: " + restrictionsText);
            if (restrictionsText == null || restrictionsText.isEmpty()) {
                throw new IllegalArgumentException("Restrictions text cannot be empty.");
            }

            List<Restriction> restrictions = new ArrayList<>();
            String termPattern = "([+-]?\\d*\\.?\\d*)?x_(\\d+)";
            Pattern pattern = Pattern.compile(termPattern);

            for (String restrictionText : restrictionsText) {
                String operator = extractOperator(restrictionText);
                String[] parts = restrictionText.split(operator);
                String leftPart = parts[0].replace(" ", "");
                double constant = Double.parseDouble(parts[1].trim());

                Matcher matcher = pattern.matcher(leftPart);
                List<Term> terms = new ArrayList<>();

                while (matcher.find()) {
                    String coefStr = matcher.group(1);
                    double coefficient = coefStr == null || coefStr.isEmpty() || coefStr.equals("+") ? 1
                            : coefStr.equals("-") ? -1 : Double.parseDouble(coefStr);
                    String variable = "x_" + matcher.group(2);
                    terms.add(new Term(coefficient, variable));
                }
                Restriction restriction = new Restriction(terms, operator, constant);
                restrictions.add(restriction);
              //  System.out.println("Restricción parseada: " + restriction);
            }
            return restrictions;
        } catch (Exception e) {
            throw new LinearProblemException("Error parsing restrictions: " + e.getMessage(), e);
        }
    }

    private String extractOperator(String restrictionText) {
        if (restrictionText.contains(">=")) {
            return ">=";
        } else if (restrictionText.contains("<=")) {
            return "<=";
        } else if (restrictionText.contains("=")) {
            return "=";
        } else {
            throw new IllegalArgumentException("Invalid restriction operator in: " + restrictionText);
        }
    }

    public Map<String, Double> calculateIntersection(Restriction r1, Restriction r2) {
        try {
            System.out.println("Calculando intersección entre: " + r1 + " y " + r2);
            Map<String, Double> coefficients1 = new HashMap<>();
            Map<String, Double> coefficients2 = new HashMap<>();

            for (Term term : r1.getTerms()) {
                coefficients1.put(term.getVariable(), term.getCoefficient());
            }
            for (Term term : r2.getTerms()) {
                coefficients2.put(term.getVariable(), term.getCoefficient());
            }

            Set<String> variables = new HashSet<>(coefficients1.keySet());
            variables.addAll(coefficients2.keySet());

            if (variables.size() == 2) {
                String[] vars = variables.toArray(new String[0]);

                double a1 = coefficients1.getOrDefault(vars[0], 0.0);
                double b1 = coefficients1.getOrDefault(vars[1], 0.0);
                double c1 = r1.getConstant();

                double a2 = coefficients2.getOrDefault(vars[0], 0.0);
                double b2 = coefficients2.getOrDefault(vars[1], 0.0);
                double c2 = r2.getConstant();

                double determinant = a1 * b2 - a2 * b1;
                if (determinant == 0) {
                    System.out.println("No hay intersección: ecuaciones dependientes");
                    return null;
                }

                double x = (c1 * b2 - c2 * b1) / determinant;
                double y = (a1 * c2 - a2 * c1) / determinant;

                Map<String, Double> intersection = new HashMap<>();
                intersection.put(vars[0], roundToTwoDecimalPlaces(x));
                intersection.put(vars[1], roundToTwoDecimalPlaces(y));

                System.out.println("Intersección encontrada: " + intersection);
                return intersection;
            } else {
                System.out.println("No hay suficientes variables para calcular intersección");
                return null;
            }
        } catch (Exception e) {
            throw new LinearProblemException("Error calculating intersection: " + e.getMessage(), e);
        }
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean containsPoint(List<Map<String, Double>> points, Map<String, Double> newPoint) {
        final double tolerance = 1e-6;
        for (Map<String, Double> point : points) {
            boolean isEqual = true;
            for (String key : newPoint.keySet()) {
                if (Math.abs(point.getOrDefault(key, 0.0) - newPoint.getOrDefault(key, 0.0)) > tolerance) {
                    isEqual = false;
                    break;
                }
            }
            if (isEqual) {
                return true;
            }
        }
        return false;
    }

    public List<Map<String, Double>> refineIntersections(
            List<Map<String, Double>> axisIntersections,
            List<Restriction> restrictions) {
        try {
            List<Map<String, Double>> refinedIntersections = new ArrayList<>(axisIntersections);

            for (int i = 0; i < restrictions.size(); i++) {
                for (int j = i + 1; j < restrictions.size(); j++) {
                    Map<String, Double> intersection = calculateIntersection(restrictions.get(i), restrictions.get(j));
                    if (intersection != null && !containsPoint(refinedIntersections, intersection)) {
                        if (isFeasible(intersection, restrictions)) {
                            refinedIntersections.add(intersection);
                        }
                    }
                }
            }
            return refinedIntersections;
        } catch (Exception e) {
            throw new LinearProblemException("Error refining intersections: " + e.getMessage(), e);
        }
    }

    public boolean isFeasible(Map<String, Double> point, List<Restriction> restrictions) {
        try {
            for (String key : point.keySet()) {
                if (point.getOrDefault(key, 0.0) < 0) {
                    return false;
                }
            }

            for (Restriction restriction : restrictions) {
                double sum = 0;
                for (Term term : restriction.getTerms()) {
                    sum += term.getCoefficient() * point.getOrDefault(term.getVariable(), 0.0);
                }

                switch (restriction.getOperator()) {
                    case ">=":
                        if (sum < restriction.getConstant())
                            return false;
                        break;
                    case "<=":
                        if (sum > restriction.getConstant())
                            return false;
                        break;
                    case "=":
                        if (Math.abs(sum - restriction.getConstant()) > 1e-6)
                            return false;
                        break;
                }
            }
            return true;
        } catch (Exception e) {
            throw new LinearProblemException("Error while checking feasibility: " + e.getMessage());
        }
    }

}
