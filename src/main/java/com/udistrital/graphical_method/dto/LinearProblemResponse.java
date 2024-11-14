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
    private List<Map<String, Double>> intersections;
    private Double maxValue;
    private Double minValue;
}
