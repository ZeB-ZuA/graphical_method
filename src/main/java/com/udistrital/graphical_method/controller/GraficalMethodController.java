package com.udistrital.graphical_method.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udistrital.graphical_method.dto.FirstPhaseResponse;
import com.udistrital.graphical_method.dto.LinearProblemResponse;
import com.udistrital.graphical_method.dto.SecondPhaseResponse;
import com.udistrital.graphical_method.dto.TwoPhaseResponse;
import com.udistrital.graphical_method.entity.LinearProblem;
import com.udistrital.graphical_method.entity.ObjectiveFunction;
import com.udistrital.graphical_method.entity.Restriction;
import com.udistrital.graphical_method.entity.TwoPhaseMatrix;
import com.udistrital.graphical_method.service.LinearProblemService;
import com.udistrital.graphical_method.service.TwoPhasesService;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/graphical-method")
@CrossOrigin(origins = "*")

public class GraficalMethodController {
    @Autowired
    private LinearProblemService linearProblemService;
    @Autowired
    private TwoPhasesService twoPhasesService;

    @PostMapping("/linear-problem")
    public ResponseEntity<?> linearProblem(@RequestBody LinearProblem linearProblem) {
        try {
            System.out.println("Received JSON: " + linearProblem.toString());
    
            // Reemplazar las claves x por x_1 y y por x_2 en las restricciones
            List<String> updatedRestrictionsText = new ArrayList<>();
            for (String restriction : linearProblem.getRestrictionsText()) {
                String updatedRestriction = restriction.replace("x", "x_1").replace("y", "x_2");
                updatedRestrictionsText.add(updatedRestriction);
            }
            linearProblem.setRestrictionsText(updatedRestrictionsText);
    
            // Reemplazar las claves x por x_1 y y por x_2 en la función objetivo
            String updatedObjectiveFunctionText = linearProblem.getObjectiveFunctionText().replace("x", "x_1").replace("y", "x_2");
            linearProblem.setObjectiveFunctionText(updatedObjectiveFunctionText);
    
            ObjectiveFunction objectiveFunction = linearProblemService
                    .parseObjectiveFunction(linearProblem.getObjectiveFunctionText());
            List<Restriction> restrictions = linearProblemService
                    .parseRestrictions(linearProblem.getRestrictionsText());
    
            List<Map<String, Double>> allIntersections = linearProblemService.calculateAllIntersections(restrictions);
    
            List<Map<String, Double>> refinedIntersections = linearProblemService.refineIntersections(allIntersections,
                    restrictions);
    
            Map<String, Object> maxResult = linearProblemService.getMax(objectiveFunction, refinedIntersections);
            Map<String, Object> minResult = linearProblemService.getMin(objectiveFunction, refinedIntersections);
    
            Double maxValue = (Double) maxResult.get("value");
            int maxIndex = (int) maxResult.get("index");
    
            Double minValue = (Double) minResult.get("value");
            int minIndex = (int) minResult.get("index");
    
            // Reemplazar las claves x_1 por x y x_2 por y en allIntersections
            List<Map<String, Double>> updatedIntersections = new ArrayList<>();
            for (Map<String, Double> intersection : allIntersections) {
                Map<String, Double> updatedIntersection = new HashMap<>();
                for (Map.Entry<String, Double> entry : intersection.entrySet()) {
                    String newKey = entry.getKey().replace("x_1", "x").replace("x_2", "y");
                    updatedIntersection.put(newKey, entry.getValue());
                }
                updatedIntersections.add(updatedIntersection);
            }
    
            LinearProblemResponse response = new LinearProblemResponse();
            response.setAllIntersections(updatedIntersections);
            response.setMaxValue(maxValue);
            response.setMaxIndex(maxIndex);
            response.setMinValue(minValue);
            response.setMinIndex(minIndex);
    
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/two-phases")
    public ResponseEntity<?> twoPhases(@RequestBody LinearProblem linearProblem) {
        try {
            System.out.println("Received JSON: " + linearProblem.toString());
            TwoPhaseMatrix matrix = twoPhasesService.transformToPhaseOne(linearProblem);

            List<FirstPhaseResponse> firstPhaseResponses = matrix.getFirstPhaseResponses();
            List<SecondPhaseResponse> secondPhaseResponses = matrix.getSecondPhaseResponses();

            TwoPhaseResponse response = new TwoPhaseResponse(firstPhaseResponses, secondPhaseResponses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API is working! HOLA DANNA, KFC <<<<< FRIZZZZBY");
    }
}
