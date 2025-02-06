package com.udistrital.graphical_method.dto;

import java.util.List;
import java.util.Map;

public class PivotStep {
    private int rowPivot;
    private int columnPivot;
    private double[][] tableau;
    private List<Double> z;
    private Map<String, Double> artificialCoefficients;

    // Constructor, getters y setters
    public PivotStep(int rowPivot, int columnPivot, double[][] tableau, List<Double> z, Map<String, Double> artificialCoefficients) {
        this.rowPivot = rowPivot;
        this.columnPivot = columnPivot;
        this.tableau = tableau;
        this.z = z;
        this.artificialCoefficients = artificialCoefficients;
    }

    // Getters y setters
    public int getRowPivot() {
        return rowPivot;
    }

    public void setRowPivot(int rowPivot) {
        this.rowPivot = rowPivot;
    }

    public int getColumnPivot() {
        return columnPivot;
    }

    public void setColumnPivot(int columnPivot) {
        this.columnPivot = columnPivot;
    }

    public double[][] getTableau() {
        return tableau;
    }

    public void setTableau(double[][] tableau) {
        this.tableau = tableau;
    }

    public List<Double> getZ() {
        return z;
    }

    public void setZ(List<Double> z) {
        this.z = z;
    }

    public Map<String, Double> getArtificialCoefficients() {
        return artificialCoefficients;
    }

    public void setArtificialCoefficients(Map<String, Double> artificialCoefficients) {
        this.artificialCoefficients = artificialCoefficients;
    }
}
