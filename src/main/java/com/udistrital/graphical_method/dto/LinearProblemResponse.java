package com.udistrital.graphical_method.dto;

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
public class LinearProblemResponse {
    private List<Map<String, Double>> allIntersections; 
    private Double maxValue;
    private int maxIndex;
    private Double minValue;
    private int minIndex;
}
