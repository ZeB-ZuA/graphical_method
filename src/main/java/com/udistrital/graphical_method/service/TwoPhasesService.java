package com.udistrital.graphical_method.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.udistrital.graphical_method.entity.LinearProblem;
import com.udistrital.graphical_method.entity.ObjectiveFunction;
import com.udistrital.graphical_method.entity.Restriction;
import com.udistrital.graphical_method.entity.Term;
import com.udistrital.graphical_method.entity.TwoPhaseMatrix;

@Service
public class TwoPhasesService {

    @Autowired
    private LinearProblemService linearProblemService;

    public TwoPhaseMatrix transformToPhaseOne(LinearProblem linearProblem) {
        ObjectiveFunction originalObjective = linearProblemService
                .parseObjectiveFunction(linearProblem.getObjectiveFunctionText());
        List<Restriction> originalRestrictions = linearProblemService
                .parseRestrictions(linearProblem.getRestrictionsText());

        // Mapa para los coeficientes de la función objetivo auxiliar
        Map<String, Double> objectiveAuxCoefficients = new LinkedHashMap<>();

        // Inicializamos los coeficientes de la función objetivo auxiliar
        for (Term term : originalObjective.getTerms()) {
            objectiveAuxCoefficients.put(term.getVariable(), 0.0); // Inicializamos a 0.0
        }

        // Variables de fase 1 (artificiales, H y S)
        int slackCounter = 1, artificialCounter = 1, surplusCounter = 1;
        List<Restriction> transformedRestrictions = new ArrayList<>(); // Lista para las restricciones transformadas

        // Procesamos cada restricción
        for (Restriction restriction : originalRestrictions) {
            List<Term> newTerms = new ArrayList<>(restriction.getTerms()); // Lista de términos de la restricción
                                                                           // original

            switch (restriction.getOperator()) {
                case "=":
                    // Agregar una variable artificial (R)
                    String artificialEq = "R" + artificialCounter++;
                    newTerms.add(new Term(1.0, artificialEq));
                    objectiveAuxCoefficients.put(artificialEq, 1.0);
                    break;

                case ">=":
                    // Agregar variables surplus (S) y artificiales (R)
                    String surplusName = "S" + surplusCounter++;
                    String artificialName = "R" + artificialCounter++;
                    newTerms.add(new Term(-1.0, surplusName)); // -S
                    newTerms.add(new Term(1.0, artificialName)); // +R
                    objectiveAuxCoefficients.put(surplusName, 0.0);
                    objectiveAuxCoefficients.put(artificialName, 1.0);
                    break;

                case "<=":
                    // Agregar variable slack (H)
                    String slackName = "H" + slackCounter++;
                    newTerms.add(new Term(1.0, slackName)); // +H
                    objectiveAuxCoefficients.put(slackName, 0.0);
                    break;
            }

            // Crear una nueva restricción transformada
            transformedRestrictions.add(new Restriction(newTerms, "=", restriction.getConstant()));
        }

        // Crear la nueva función objetivo auxiliar W
        List<Term> newObjectiveTerms = objectiveAuxCoefficients.entrySet().stream()
                .map(entry -> new Term(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

        ObjectiveFunction auxiliaryObjective = new ObjectiveFunction(newObjectiveTerms, 0.0);

        // Crear la matriz de la Fase 1 con las restricciones transformadas y la función
        // objetivo auxiliar
        TwoPhaseMatrix matrix = new TwoPhaseMatrix(auxiliaryObjective, transformedRestrictions, originalObjective);

        return matrix;
    }

}
