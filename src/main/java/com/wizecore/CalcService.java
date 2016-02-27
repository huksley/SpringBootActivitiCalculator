package com.wizecore;

import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.runtime.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Calculation service.
 * Implements service task part of web process.
 * 
 * @author Ruslan
 */
@Component
public class CalcService {
	Logger log = Logger.getLogger(getClass().getName());
	
	@Autowired
    private RuntimeService bpm;
	
	@Autowired
	private EntityManager entityManager;

    public void calculate(Execution exec) {
    	log.info("Calculating (" + bpm + ", " + exec + ") ...");
        try {
	        CalcObject o = (CalcObject) bpm.getVariable(exec.getId(), "calc");
	        log.info("Expression " + o.getA() + " " + o.getOperation() + " " + o.getB() + " = ?");
	        if (o.getOperation().equals("+")) {
	        	o.setC(o.getA() + o.getB());
	        } else
	        if (o.getOperation().equals("-")) {
	        	o.setC(o.getA() - o.getB());
	        } else
	        if (o.getOperation().equals("*")) {
	        	o.setC(o.getA() * o.getB());
	        } else
	        if (o.getOperation().equals("/")) {
	        	o.setC(o.getA() / o.getB());
	        } else {
	        	throw new RuntimeException("Unknown op: " + o.getOperation());
	        }
	        log.info("Conclusion " + o.getA() + " " + o.getOperation() + " " + o.getB() + " = " + o.getC());
	        bpm.setVariable(exec.getId(), "calc", o);
        } catch (Exception e) {
        	bpm.setVariable(exec.getId(), "error", e.getMessage());
        	throw new BpmnError("CalcErr", e.getMessage());
        }
    }

    public void starting(Execution exec) {
    	log.info("Starting (" + bpm + ", " + exec + ") ...");
    	CalcObject o = new CalcObject();
    	o.setA(1);
    	o.setB(2);
    	o.setOperation("+");
    	entityManager.persist(o);
    	bpm.setVariable(exec.getId(), "calc", o);
    }
}