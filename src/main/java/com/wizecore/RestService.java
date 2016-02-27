package com.wizecore;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API exposure.
 * @author Ruslan
 */
@RestController
public class RestService {

	@Autowired
	private WorkflowService bpm;

	@RequestMapping("/greeting")
	public String index() {
		return "Greetings from Spring Boot!";
	}
	
	@RequestMapping("/whoami")
	public String whoami(Principal principal) {
		return principal.getName();
	}

	@RequestMapping(value = "/process", method = RequestMethod.POST)
	public void startProcessInstance(HttpServletResponse response) throws IOException {
		bpm.startProcess();
		response.sendRedirect("/hello");
	}

	@RequestMapping(value = "/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TaskRepresentation> getTasks(Principal principal) {
		return bpm.getTasks(principal.getName());
	}
	
	@RequestMapping(value = "/task/complete/{id}", method = RequestMethod.POST)
	public void completeTask(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> taskVars = bpm.getTaskVariables(id);
		
		for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements();) {
			String name = names.nextElement();
			String value = request.getParameter(name);
			if (name.startsWith("process.")) {
				String expr = name.substring("process.".length());
				ExpressionParser parser = new SpelExpressionParser();
				Expression exp = parser.parseExpression("#" + expr);
				//Map<String,Object> map = new HashMap<>();
				StandardEvaluationContext simpleContext = new StandardEvaluationContext();
				simpleContext.setVariables(taskVars);
				System.out.println("Assigning " + exp + "(" + expr + ") = " + value);
				exp.setValue(simpleContext, value);
			}
		}
		
		bpm.completeTask(id);
		response.sendRedirect("/hello");
	}
}