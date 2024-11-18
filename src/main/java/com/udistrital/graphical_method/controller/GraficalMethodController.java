package com.udistrital.graphical_method.controller;

import java.util.ArrayList;
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

import com.udistrital.graphical_method.dto.LinearProblemResponse;
import com.udistrital.graphical_method.entity.LinearProblem;
import com.udistrital.graphical_method.entity.ObjectiveFunction;
import com.udistrital.graphical_method.entity.Restriction;
import com.udistrital.graphical_method.service.LinearProblemService;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/graphical-method")
@CrossOrigin(origins = "*")

public class GraficalMethodController {
    @Autowired
    private LinearProblemService linearProblemService;

   @PostMapping("/solve")
    public ResponseEntity<?> solve(@RequestBody LinearProblem linearProblem) {
        try {
            ObjectiveFunction objectiveFunction = linearProblemService
                    .parseObjectiveFunction(linearProblem.getObjectiveFunctionText());
            List<Restriction> restrictions = linearProblemService.parseRestrictions(linearProblem.getRestrictionsText());

            List<Map<String, Double>> intersections = new ArrayList<>();
            for (Restriction restriction : restrictions) {
                intersections.add(linearProblemService.calculateIntersections(restriction));
            }

            Map<String, Object> maxResult = linearProblemService.getMax(objectiveFunction, intersections);
            Map<String, Object> minResult = linearProblemService.getMin(objectiveFunction, intersections);

            Double maxValue = (Double) maxResult.get("value");
            int maxIndex = (int) maxResult.get("index");

            Double minValue = (Double) minResult.get("value");
            int minIndex = (int) minResult.get("index");

            LinearProblemResponse response = new LinearProblemResponse();
            response.setIntersections(intersections);
            response.setMaxValue(maxValue);
            response.setMaxIndex(maxIndex);
            response.setMinValue(minValue);
            response.setMinIndex(minIndex);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API is working! HOLA DANNA, KFC >>>> FRIZZZZBY");
    }
}
