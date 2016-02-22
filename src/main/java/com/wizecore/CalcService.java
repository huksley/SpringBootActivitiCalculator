package com.wizecore;

import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Calculation service.
 * 
 * @author Ruslan
 */
@Component
public class CalcService {
	
	@Autowired
    private RuntimeService bpm;

    public void calculate() {
        System.out.println("Calculating (" + bpm + ") ...");
    }
}