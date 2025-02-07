package com.udistrital.graphical_method.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class SecondPhaseResponse {
    private double[][] tableau;
    private List<Double> z;
    private Map<String, Double> cx;
    private Map<String, Double> cj;
}
