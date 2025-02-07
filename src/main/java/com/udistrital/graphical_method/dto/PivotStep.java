package com.udistrital.graphical_method.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PivotStep {
    private int rowPivot;
    private int columnPivot;
    private double[][] tableau;
    private List<Double> z;
    private Map<String, Double> artificialCoefficients;

    // Constructor, getters y setters
    public PivotStep(int rowPivot, int columnPivot, double[][] tableau, List<Double> z,
            Map<String, Double> artificialCoefficients) {
        this.rowPivot = rowPivot;
        this.columnPivot = columnPivot;
        this.tableau = tableau;
        this.z = z;
        this.artificialCoefficients = artificialCoefficients;
    }
}
