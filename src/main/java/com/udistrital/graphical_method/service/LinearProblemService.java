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

    public List<Map<String, Double>> calculateAxisIntersections(Restriction restriction) {
        List<Map<String, Double>> points = new ArrayList<>();
        
        double constant = restriction.getConstant();
        List<Term> terms = restriction.getTerms();
        
        double xCoefficient = 0;
        double yCoefficient = 0;
        
        // Calcular intersección con el eje X (cuando y = 0)
        for (Term term : terms) {
            if (term.getVariable().equals("x")) {
                xCoefficient = term.getCoefficient();
            } else if (term.getVariable().equals("y")) {
                yCoefficient = term.getCoefficient();
            }
        }
    
        // Calcular intersección en el eje X (cuando y = 0)
        double xIntersection = (xCoefficient != 0) ? constant / xCoefficient : 0;
        // Calcular intersección en el eje Y (cuando x = 0)
        double yIntersection = (yCoefficient != 0) ? constant / yCoefficient : 0;
        
        // Crear puntos de intersección
        Map<String, Double> xIntersectionMap = new HashMap<>();
        xIntersectionMap.put("x", xIntersection);
        xIntersectionMap.put("y", 0.0);
        
        Map<String, Double> yIntersectionMap = new HashMap<>();
        yIntersectionMap.put("x", 0.0);
        yIntersectionMap.put("y", yIntersection);
        
        // Verificar si los puntos cumplen con las restricciones antes de agregarlos
        if (isFeasible(xIntersectionMap, List.of(restriction))) {
            points.add(xIntersectionMap);
        }
        if (isFeasible(yIntersectionMap, List.of(restriction))) {
            points.add(yIntersectionMap);
        }
        
        return points;
    }
    
    
    
    
    public List<Map<String, Double>> calculateAllIntersections(List<Restriction> restrictions) {
        List<Map<String, Double>> allIntersections = new ArrayList<>();
        
        // Calcular las intersecciones con los ejes (x = 0 y y = 0)
        for (Restriction restriction : restrictions) {
            List<Map<String, Double>> axisIntersections = calculateAxisIntersections(restriction);
            
            // Filtrar intersecciones válidas antes de agregarlas
            for (Map<String, Double> intersection : axisIntersections) {
                if (!containsPoint(allIntersections, intersection) && isFeasible(intersection, restrictions)) {
                    allIntersections.add(intersection);
                }
            }
        }
        
        // Refinar las intersecciones con las intersecciones entre rectas
        allIntersections = refineIntersections(allIntersections, restrictions);
        
        return allIntersections;
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

    public Map<String, Double> calculateIntersection(Restriction r1, Restriction r2) {
        // Coeficientes de las rectas
        double a1 = 0, b1 = 0, c1 = r1.getConstant();
        double a2 = 0, b2 = 0, c2 = r2.getConstant();
        
        // Obtener los coeficientes de las restricciones
        for (Term term : r1.getTerms()) {
            if ("x".equals(term.getVariable())) {
                a1 = term.getCoefficient();
            } else if ("y".equals(term.getVariable())) {
                b1 = term.getCoefficient();
            }
        }
        
        for (Term term : r2.getTerms()) {
            if ("x".equals(term.getVariable())) {
                a2 = term.getCoefficient();
            } else if ("y".equals(term.getVariable())) {
                b2 = term.getCoefficient();
            }
        }
        
        // Calcular el determinante
        double determinant = a1 * b2 - a2 * b1;
        if (determinant == 0) {
            return null; // Las restricciones son paralelas o coincidentes.
        }
        
        // Calcular las coordenadas de la intersección
        double x = (c1 * b2 - c2 * b1) / determinant;
        double y = (a1 * c2 - a2 * c1) / determinant;
        
        // Guardar la intersección
        Map<String, Double> intersection = new HashMap<>();
        intersection.put("x", roundToTwoDecimalPlaces(x));
        intersection.put("y", roundToTwoDecimalPlaces(y));
        return intersection;
    }
    

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean containsPoint(List<Map<String, Double>> points, Map<String, Double> newPoint) {
        final double tolerance = 1e-6;
        for (Map<String, Double> point : points) {
            double xDiff = Math.abs(point.get("x") - newPoint.get("x"));
            double yDiff = Math.abs(point.get("y") - newPoint.get("y"));
            if (xDiff < tolerance && yDiff < tolerance) {
                return true;
            }
        }
        return false;
    }

    public List<Map<String, Double>> refineIntersections(
            List<Map<String, Double>> axisIntersections,
            List<Restriction> restrictions) {
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
    }

    public boolean isFeasible(Map<String, Double> point, List<Restriction> restrictions) {
        // Validar si x y y son mayores o iguales a 0
        if (point.get("x") < 0 || point.get("y") < 0) {
            return false;  // El punto no es factible si tiene coordenadas negativas
        }
    
        // Validar el cumplimiento de las restricciones de las rectas
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
                    if (sum != restriction.getConstant())
                        return false;
                    break;
            }
        }
        return true;
    }
    
}
