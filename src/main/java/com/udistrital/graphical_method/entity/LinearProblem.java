package com.udistrital.graphical_method.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LinearProblem {
    
    private String objectiveFunctionText;
    private List<String> restrictionsText;
    private Boolean isMaximization;

    @Override
    public String toString() {
        return "LinearProblem{" +
                "objectiveFunctionText='" + objectiveFunctionText + '\'' +
                ", restrictionsText=" + restrictionsText +
                ", isMaximization=" + isMaximization +
                '}';
    }
}