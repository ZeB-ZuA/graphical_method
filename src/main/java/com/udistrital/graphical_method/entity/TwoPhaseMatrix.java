package com.udistrital.graphical_method.entity;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.udistrital.graphical_method.dto.FirstPhaseResponse;
import com.udistrital.graphical_method.dto.SecondPhaseResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TwoPhaseMatrix {
    private List<String> variables;
    private double[][] tableau;
    private Map<String, Double> objectiveAuxCoefficients;
    private Map<String, Double> artificialCoefficients;
    private List<Double> z;
    private double[][] tableau2;
    private Map<String, Double> objectiveAuxCoefficients2;
    private Map<String, Double> artificialCoefficients2;
    private List<Double> z2;
    private List<FirstPhaseResponse> firstPhaseResponses;
    private List<SecondPhaseResponse> secondPhaseResponses;

    public TwoPhaseMatrix(ObjectiveFunction auxiliaryObjective, List<Restriction> restrictions,
            ObjectiveFunction originalObjective) {
        this.variables = new ArrayList<>();
        this.objectiveAuxCoefficients = new LinkedHashMap<>();
        this.artificialCoefficients = new LinkedHashMap<>();
        this.z = new ArrayList<>();
        this.tableau = new double[restrictions.size()][];
        this.objectiveAuxCoefficients2 = new LinkedHashMap<>();
        this.artificialCoefficients2 = new LinkedHashMap<>();
        this.z2 = new ArrayList<>();
        this.firstPhaseResponses = new ArrayList<>();
        this.secondPhaseResponses = new ArrayList<>();
        buildMatrix(auxiliaryObjective, restrictions);
        buildSecondPhaseMatrix(originalObjective);
    }

    private void buildMatrix(ObjectiveFunction auxiliaryObjective, List<Restriction> restrictions) {
        try {
            Set<String> variableSet = new LinkedHashSet<>();

            for (Restriction restriction : restrictions) {
                for (Term term : restriction.getTerms()) {
                    variableSet.add(term.getVariable());
                }
            }

            this.variables.addAll(variableSet);
            System.out.println("Variables: " + variables);

            for (Term term : auxiliaryObjective.getTerms()) {
                objectiveAuxCoefficients.put(term.getVariable(), term.getCoefficient());
            }

            this.tableau = new double[restrictions.size()][variables.size() + 1];

            for (int i = 0; i < restrictions.size(); i++) {
                Restriction restriction = restrictions.get(i);

                for (Term term : restriction.getTerms()) {
                    int colIndex = variables.indexOf(term.getVariable());
                    tableau[i][colIndex] = term.getCoefficient();
                }

                tableau[i][variables.size()] = restriction.getConstant();
            }

            // Paso 8: Llenar la fila de la función objetivo auxiliar
            for (Map.Entry<String, Double> entry : objectiveAuxCoefficients.entrySet()) {
                int colIndex = variables.indexOf(entry.getKey());
            }

            for (Map.Entry<String, Double> entry : objectiveAuxCoefficients.entrySet()) {
                if (entry.getKey().startsWith("R") || entry.getKey().startsWith("H")) {
                    artificialCoefficients.put(entry.getKey(), entry.getValue());
                }
            }

            calculateZ(); // Calcular Z
        } catch (Exception e) {
            throw new RuntimeException("Error al construir la matriz: " + e.getMessage(), e);
        }
    }

    private void calculateZ() {
        int filas = tableau.length;
        int columnas = tableau[0].length;
        this.z = new ArrayList<>(Collections.nCopies(columnas, 0.0));

        List<Double> artificialCoeffList = new ArrayList<>(artificialCoefficients.values());

        // Recorrer las columnas del tableau (incluyendo la última columna 'b')
        for (int j = 0; j < columnas; j++) {
            double sumatoria = 0.0;

            // Sumar el producto de los valores de la columna por los coeficientes
            // artificiales
            for (int i = 0; i < filas; i++) {
                // Obtener el coeficiente artificial para la fila i
                double coeficiente = artificialCoeffList.get(i);
                double valorTableau = tableau[i][j];
                double producto = valorTableau * coeficiente;
                sumatoria += producto;
            }

            // Restar el coeficiente de la función objetivo auxiliar (solo para columnas no
            // básicas)
            if (j < columnas - 1) { // No restar para la columna 'b'
                String variable = variables.get(j);
                double objetivoCoef = objectiveAuxCoefficients.getOrDefault(variable, 0.0);
                sumatoria -= objetivoCoef;
            }
            z.set(j, sumatoria);
        }

        solveSimplex();
    }

    private void calculateZ2() {
        int filas = tableau2.length;
        int columnas = tableau2[0].length;
        this.z2 = new ArrayList<>(Collections.nCopies(columnas, 0.0));
        List<String> variables2 = new ArrayList<>(objectiveAuxCoefficients2.keySet());

        List<Double> artificialCoeffList = new ArrayList<>(artificialCoefficients2.values());

        // Recorrer las columnas del tableau2 (incluyendo la última columna 'b')
        for (int j = 0; j < columnas; j++) {
            double sumatoria = 0.0;

            // Sumar el producto de los valores de la columna por los coeficientes
            // artificiales
            for (int i = 0; i < filas; i++) {
                // Obtener el coeficiente artificial para la fila i
                double coeficiente = artificialCoeffList.get(i);
                double valorTableau = tableau2[i][j];
                double producto = valorTableau * coeficiente;
                sumatoria += producto;
            }

            // Restar el coeficiente de la función objetivo auxiliar (solo para columnas no
            // básicas)
            if (j < columnas - 1) { // No restar para la columna 'b'
                String variable2 = variables2.get(j);
                double objetivoCoef = objectiveAuxCoefficients2.getOrDefault(variable2, 0.0);
                sumatoria -= objetivoCoef;
            }
            z2.set(j, sumatoria);
        }

         solveSimplex2();
    }

    private void pivot2(int rowPivot2, int columnPivot2) {
        double pivotValue = tableau2[rowPivot2][columnPivot2];

        // Normalizar la fila pivote
        for (int j = 0; j < tableau2[0].length; j++) {
            tableau2[rowPivot2][j] = tableau2[rowPivot2][j] / pivotValue;
        }

        // Ajustar las demás filas
        for (int i = 0; i < tableau2.length; i++) {
            if (i != rowPivot2) {
                double factor = tableau2[i][columnPivot2];
                for (int j = 0; j < tableau2[0].length; j++) {
                    tableau2[i][j] = tableau2[i][j] - factor * tableau2[rowPivot2][j];
                }
            }
        }

        // Actualizar la función objetivo Z
        double factor = z2.get(columnPivot2);
        for (int j = 0; j < z2.size(); j++) {
            z2.set(j, z2.get(j) - factor * tableau2[rowPivot2][j]);
        }

        // 🚀 **Actualizar la variable en la base**
        String nuevaVariable = variables.get(columnPivot2); // Variable que entra

        // Convertir el LinkedHashMap en una lista de entradas
        List<Map.Entry<String, Double>> entradas = new ArrayList<>(artificialCoefficients2.entrySet());

        // Reemplazar la clave en la posición rowPivot
        entradas.set(rowPivot2, new AbstractMap.SimpleEntry<>(nuevaVariable,
                objectiveAuxCoefficients2.getOrDefault(nuevaVariable, 0.0)));

        // Reconstruir el LinkedHashMap
        artificialCoefficients2.clear();
        for (Map.Entry<String, Double> entrada : entradas) {
            artificialCoefficients2.put(entrada.getKey(), entrada.getValue());
        }

    }

    private void pivot(int rowPivot, int columnPivot) {
        double pivotValue = tableau[rowPivot][columnPivot];

        // Normalizar la fila pivote
        for (int j = 0; j < tableau[0].length; j++) {
            tableau[rowPivot][j] = tableau[rowPivot][j] / pivotValue;
        }

        // Ajustar las demás filas
        for (int i = 0; i < tableau.length; i++) {
            if (i != rowPivot) {
                double factor = tableau[i][columnPivot];
                for (int j = 0; j < tableau[0].length; j++) {
                    tableau[i][j] = tableau[i][j] - factor * tableau[rowPivot][j];
                }
            }
        }

        // Actualizar la función objetivo Z
        double factor = z.get(columnPivot);
        for (int j = 0; j < z.size(); j++) {
            z.set(j, z.get(j) - factor * tableau[rowPivot][j]);
        }

        // 🚀 **Actualizar la variable en la base**
        String nuevaVariable = variables.get(columnPivot); // Variable que entra

        // Convertir el LinkedHashMap en una lista de entradas
        List<Map.Entry<String, Double>> entradas = new ArrayList<>(artificialCoefficients.entrySet());

        // Reemplazar la clave en la posición rowPivot
        entradas.set(rowPivot, new AbstractMap.SimpleEntry<>(nuevaVariable,
                objectiveAuxCoefficients.getOrDefault(nuevaVariable, 0.0)));

        // Reconstruir el LinkedHashMap
        artificialCoefficients.clear();
        for (Map.Entry<String, Double> entrada : entradas) {
            artificialCoefficients.put(entrada.getKey(), entrada.getValue());
        }

    }

    private void printTableau(double tableau[][], List<Double> z, Map<String, Double> artificialCoefficients,
            Map<String, Double> objectiveAuxCoefficients) {
        // Imprimir las claves del mapa objectiveAuxCoefficients
        System.out.println("Claves de objectiveAuxCoefficients:");
        for (String key : objectiveAuxCoefficients.keySet()) {
            System.out.print(key + "\t");
        }
        System.out.println();

        // Imprimir el tableau con redondeo a 1 decimal
        for (int i = 0; i < tableau.length; i++) {
            for (int j = 0; j < tableau[0].length; j++) {
                System.out.print(Math.round(tableau[i][j] * 10.0) / 10.0 + "\t");
            }
            System.out.println();
        }

        // Imprimir Z con redondeo a 1 decimal
        System.out.print("Z: ");
        for (double value : z) {
            System.out.print(Math.round(value * 10.0) / 10.0 + "\t");
        }
        System.out.println();

        // Imprimir Cx y Cj
        System.out.println("Cx: " + artificialCoefficients);
        System.out.println("Cj: " + objectiveAuxCoefficients);
    }

    private void solveSimplex() {
        int iteration = 0;
        while (true) {
            System.out.println("===== ITERACIÓN " + iteration + " =====");
            printTableau(tableau, z, artificialCoefficients, objectiveAuxCoefficients);

            // Verificar si la solución es óptima
            if (isOptimal()) {
                System.out.println("✅ Se alcanzó la solución óptima.");
                break;
            }

            // Encontrar la columna pivote
            int columnPivot = getColumnPivot(z);
            if (columnPivot == -1) {
                System.out.println("❌ No se encontró columna pivote, problema no acotado.");
                break;
            }

            // Encontrar la fila pivote
            int rowPivot = getRowPivot(columnPivot);
            if (rowPivot == -1) {
                System.out.println("❌ No se encontró fila pivote, problema no acotado.");
                break;
            }

            System.out.println("Columna pivote: " + columnPivot);
            System.out.println("Fila pivote: " + rowPivot);

            // Realizar el pivoteo
            pivot(rowPivot, columnPivot);

            // Guardar el estado actual en firstPhaseResponses después del pivoteo
            double[][] deepTableuCopy = new double[tableau.length][tableau[0].length];
            for (int i = 0; i < tableau.length; i++) {
                for (int j = 0; j < tableau[0].length; j++) {
                    deepTableuCopy[i][j] = tableau[i][j];
                }
            }

            List<Double> deepZCopy = new ArrayList<>(z);
            Map<String, Double> deepArtificialCoefficientsCopy = new LinkedHashMap<>(artificialCoefficients);
            Map<String, Double> deepObjectiveAuxCoefficientsCopy = new LinkedHashMap<>(objectiveAuxCoefficients);

            firstPhaseResponses.add(new FirstPhaseResponse(deepTableuCopy, deepZCopy, deepArtificialCoefficientsCopy,
                    deepObjectiveAuxCoefficientsCopy));

            iteration++;
        }

        // Guardar el estado final en firstPhaseResponses
        double[][] finalTableuCopy = new double[tableau.length][tableau[0].length];
        for (int i = 0; i < tableau.length; i++) {
            for (int j = 0; j < tableau[0].length; j++) {
                finalTableuCopy[i][j] = tableau[i][j];
            }
        }

        List<Double> finalZCopy = new ArrayList<>(z);
        Map<String, Double> finalArtificialCoefficientsCopy = new LinkedHashMap<>(artificialCoefficients);
        Map<String, Double> finalObjectiveAuxCoefficientsCopy = new LinkedHashMap<>(objectiveAuxCoefficients);

        firstPhaseResponses.add(new FirstPhaseResponse(finalTableuCopy, finalZCopy, finalArtificialCoefficientsCopy,
                finalObjectiveAuxCoefficientsCopy));
    }

    private void solveSimplex2() {
        int iteration = 0;
        while (true) {
            System.out.println("===== ITERACIÓN " + iteration + " =====");
            printTableau(tableau2, z2, artificialCoefficients2, objectiveAuxCoefficients2);

            // Verificar si la solución es óptima
            if (isOptimal2()) {
                System.out.println("✅ Se alcanzó la solución óptima.");
                break;
            }

            // Encontrar la columna pivote
            int columnPivot = getColumnPivot2(z2);
            if (columnPivot == -1) {
                System.out.println("❌ No se encontró columna pivote, problema no acotado.");
                break;
            }

            // Encontrar la fila pivote
            int rowPivot = getRowPivot2(columnPivot);
            if (rowPivot == -1) {
                System.out.println("❌ No se encontró fila pivote, problema no acotado.");
                break;
            }

            System.out.println("Columna pivote: " + columnPivot);
            System.out.println("Fila pivote: " + rowPivot);

            // Realizar el pivoteo
            pivot2(rowPivot, columnPivot);

            // Guardar el estado actual en secondPhaseResponses después del pivoteo
            double[][] deepTableuCopy = new double[tableau2.length][tableau2[0].length];
            for (int i = 0; i < tableau2.length; i++) {
                for (int j = 0; j < tableau2[0].length; j++) {
                    deepTableuCopy[i][j] = tableau2[i][j];
                }
            }

            List<Double> deepZCopy = new ArrayList<>(z2);
            Map<String, Double> deepArtificialCoefficientsCopy = new LinkedHashMap<>(artificialCoefficients2);
            Map<String, Double> deepObjectiveAuxCoefficientsCopy = new LinkedHashMap<>(objectiveAuxCoefficients2);

            secondPhaseResponses.add(new SecondPhaseResponse(deepTableuCopy, deepZCopy, deepArtificialCoefficientsCopy,
                    deepObjectiveAuxCoefficientsCopy));

            iteration++;
        }

        // Guardar el estado final en secondPhaseResponses
        double[][] finalTableuCopy = new double[tableau2.length][tableau2[0].length];
        for (int i = 0; i < tableau2.length; i++) {
            for (int j = 0; j < tableau2[0].length; j++) {
                finalTableuCopy[i][j] = tableau2[i][j];
            }
        }
        List<Double> finalZCopy = new ArrayList<>(z2);
        Map<String, Double> finalArtificialCoefficientsCopy = new LinkedHashMap<>(artificialCoefficients2);
        Map<String, Double> finalObjectiveAuxCoefficientsCopy = new LinkedHashMap<>(objectiveAuxCoefficients2);
        secondPhaseResponses.add(new SecondPhaseResponse(finalTableuCopy, finalZCopy, finalArtificialCoefficientsCopy,
                finalObjectiveAuxCoefficientsCopy));
      
    }

    public int getColumnPivot(List<Double> z) {
        int columnPivot = -1;
        double maxValue = Double.NEGATIVE_INFINITY;

        // Iterar sobre la lista z, omitiendo la última posición
        for (int j = 0; j < z.size() - 1; j++) {
            if (z.get(j) > maxValue) {
                maxValue = z.get(j);
                columnPivot = j;
            }
        }

        return columnPivot;
    }

    public int getRowPivot(int columnPivot) {
        int rowPivot = -1;
        double minRatio = Double.POSITIVE_INFINITY;

        for (int i = 0; i < tableau.length; i++) {
            double valueInPivotColumn = tableau[i][columnPivot];
            double valueInBColumn = tableau[i][tableau[0].length - 1];

            if (valueInPivotColumn > 0) {
                double ratio = valueInBColumn / valueInPivotColumn;
                if (ratio >= 0 && ratio < minRatio) {
                    minRatio = ratio;
                    rowPivot = i;
                }
            }
        }

        return rowPivot;
    }

    private boolean isOptimal() {
        for (int i = 0; i < z.size() - 1; i++) { // Excluir el valor de B
            if (z.get(i) > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isOptimal2() {
        // Verificar si todos los valores en z2 (excepto el último valor) son menores o iguales a 0 (para minimización)
        for (int j = 0; j < z2.size() - 1; j++) {
            if (z2.get(j) > 0) {
                return false;
            }
        }
        return true;
    }

    private void buildSecondPhaseMatrix(ObjectiveFunction originalObjective) {

        System.out.println("===== FASE 2 =====");
        // Identificar variables artificiales y eliminarlas
        List<Integer> columnsToRemove = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).startsWith("R")) {
                columnsToRemove.add(i);
            }
        }

        // Definir nuevo tamaño de tableau sin las columnas R
        int newCols = variables.size() - columnsToRemove.size() + 1;
        tableau2 = new double[tableau.length][newCols];

        // Copiar datos de tableau a tableau2 sin columnas R
        for (int i = 0; i < tableau.length; i++) {
            int colIndex = 0;
            for (int j = 0; j < variables.size(); j++) {
                if (!columnsToRemove.contains(j)) {
                    tableau2[i][colIndex++] = tableau[i][j];
                }
            }
            tableau2[i][newCols - 1] = tableau[i][variables.size()]; // Copiar la columna B
        }

        // Actualizar los coeficientes de la función objetivo original
        for (Term term : originalObjective.getTerms()) {
            if (!term.getVariable().startsWith("R")) {
                objectiveAuxCoefficients2.put(term.getVariable(), term.getCoefficient());
            }
        }

        // Añadir coeficientes cero para las variables de holgura y artificiales que no
        // están en la función objetivo original
        for (String var : variables) {
            if (!objectiveAuxCoefficients2.containsKey(var) && !var.startsWith("R")) {
                objectiveAuxCoefficients2.put(var, 0.0);
            }
        }
        // Armar artificialCoefficients2 con los valores de objectiveAuxCoefficients2
        for (String key : artificialCoefficients.keySet()) {
            if (objectiveAuxCoefficients2.containsKey(key)) {
                artificialCoefficients2.put(key, objectiveAuxCoefficients2.get(key));
            }
        }
        // Recalcular Z2 en base a la nueva función objetivo
        calculateZ2();
        printTableau(tableau2, z2, artificialCoefficients2, objectiveAuxCoefficients2);
    }

    private int getRowPivot2(int columnPivot) {
        int rowPivot = -1;
        double minRatio = Double.POSITIVE_INFINITY;

        for (int i = 0; i < tableau2.length; i++) {
            double valueInPivotColumn = tableau2[i][columnPivot];
            double valueInBColumn = tableau2[i][tableau2[0].length - 1];

            if (valueInPivotColumn > 0) {
                double ratio = valueInBColumn / valueInPivotColumn;
                if (ratio >= 0 && ratio < minRatio) {
                    minRatio = ratio;
                    rowPivot = i;
                }
            }
        }

        return rowPivot;
    }

    private int getColumnPivot2(List<Double> z2) {
        int columnPivot = -1;
        double maxValue = Double.NEGATIVE_INFINITY;

        // Iterar sobre la lista z, omitiendo la última posición
        for (int j = 0; j < z2.size() - 1; j++) {
            if (z2.get(j) > maxValue) {
                maxValue = z2.get(j);
                columnPivot = j;
            }
        }

        return columnPivot;
    }

}