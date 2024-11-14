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
public class Restriction {
    
      private List<Term> terms;       
    private String operator;      
    private double constant;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terms.size(); i++) {
            Term term = terms.get(i);
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(term.toString());
        }
        sb.append(" ").append(operator).append(" ").append(constant);
        return sb.toString();
    }
}
