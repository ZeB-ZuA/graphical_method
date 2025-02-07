package com.udistrital.graphical_method.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TwoPhaseResponse {

private List<FirstPhaseResponse> firstPhaseResponses;
private List<SecondPhaseResponse> secondPhaseResponses;

    
}