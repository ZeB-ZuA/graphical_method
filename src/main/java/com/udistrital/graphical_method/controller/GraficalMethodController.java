package com.udistrital.graphical_method.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/graphical-method")
@CrossOrigin(origins = "*")

public class GraficalMethodController {
    @Autowired
    private LinearProblemService linearProblemService;

    @PostMapping("/show")
    public ResponseEntity<?> show(@RequestBody LinearProblem linearProblem) {
        ObjectiveFunction objectiveFunction = linearProblemService
                .parseObjectiveFunction(linearProblem.getObjectiveFunctionText());
        List<Restriction> restrictions = linearProblemService.parseRestrictions(linearProblem.getRestrictionsText());
        List<Map<String, Double>> intersectionsList = new ArrayList<>();
        for (Restriction restriction : restrictions) {
            Map<String, Double> intersections = linearProblemService.calculateIntersections(restriction);
            intersectionsList.add(intersections);
        }
        Double max = linearProblemService.getMax(objectiveFunction, intersectionsList);
        Double min = linearProblemService.getMin(objectiveFunction, intersectionsList);
        LinearProblemResponse response = new LinearProblemResponse(intersectionsList, max, min);
        return ResponseEntity.ok(response);
    }
}
