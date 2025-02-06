package com.udistrital.graphical_method.entity;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public TwoPhaseMatrix(ObjectiveFunction auxiliaryObjective, List<Restriction> restrictions) {
        this.variables = new ArrayList<>();
        this.objectiveAuxCoefficients = new LinkedHashMap<>();
        this.artificialCoefficients = new LinkedHashMap<>();
        this.z = new ArrayList<>();
        this.tableau = new double[restrictions.size()][];
        buildMatrix(auxiliaryObjective, restrictions);

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
            z.set(j, Math.round((z.get(j) - factor * tableau[rowPivot][j]) * 10.0) / 10.0);
        }
    
        // 🚀 **Actualizar la variable en la base**
        String nuevaVariable = variables.get(columnPivot); // Variable que entra
    
        // Convertir el LinkedHashMap en una lista de entradas
        List<Map.Entry<String, Double>> entradas = new ArrayList<>(artificialCoefficients.entrySet());
    
        // Reemplazar la clave en la posición rowPivot
        Map.Entry<String, Double> entradaAntigua = entradas.get(rowPivot);
        entradas.set(rowPivot, new AbstractMap.SimpleEntry<>(nuevaVariable, objectiveAuxCoefficients.getOrDefault(nuevaVariable, 0.0)));
    
        // Reconstruir el LinkedHashMap
        artificialCoefficients.clear();
        for (Map.Entry<String, Double> entrada : entradas) {
            artificialCoefficients.put(entrada.getKey(), entrada.getValue());
        }
    }
    
    
    
    
    
    
    private void printTableau() {
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



    public void solveSimplex() {
        int iteration = 0;
        while (true) {
            System.out.println("\n===== ITERACIÓN " + iteration + " =====");
            
            printTableau();
    
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
    
            iteration++;
        }
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
    
}
