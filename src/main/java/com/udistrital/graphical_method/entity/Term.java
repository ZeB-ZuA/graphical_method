package com.udistrital.graphical_method.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Term {
    
    private Double coefficient;
    private String variable; 

    @Override
public String toString() {
    return "Term {coefficient=" + coefficient + ", variable='" + variable + "'}";
}

}