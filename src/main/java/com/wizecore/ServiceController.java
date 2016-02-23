package com.wizecore;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.task.Task;
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
public class ServiceController {

	@Autowired
	private WorkflowService myService;

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
		myService.startProcess();
		response.sendRedirect("/hello");
	}

	@RequestMapping(value = "/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TaskRepresentation> getTasks(Principal principal) {
		List<Task> tasks = myService.getTasks(principal.getName());
		List<TaskRepresentation> dtos = new ArrayList<TaskRepresentation>();
		for (Task task : tasks) {
			dtos.add(new TaskRepresentation(task));
		}
		return dtos;
	}
	
	@RequestMapping(value = "/task/complete/{id}", method = RequestMethod.POST)
	public void completeTask(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> taskVars = myService.taskVars(id);
		
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
		
		myService.completeTask(id);
		response.sendRedirect("/hello");
	}

	class TaskRepresentation {

		private String id;
		private String name;
		private String processDefinitionId;
		private String processDefinitionName;
		private String processInstanceId;
		private String processName;
		private String formKey;

		public TaskRepresentation(Task task) {
			this.id = task.getId();
			this.name = task.getName();
			this.setProcessDefinitionId(task.getProcessDefinitionId());
			this.setProcessInstanceId(task.getProcessInstanceId());
			formKey = task.getFormKey();
			processName = myService.processName(task.getProcessInstanceId());
			processDefinitionName = myService.processDefinitionName(task.getProcessInstanceId());
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getProcessInstanceId() {
			return processInstanceId;
		}

		public void setProcessInstanceId(String processInstanceId) {
			this.processInstanceId = processInstanceId;
		}

		public String getProcessDefinitionId() {
			return processDefinitionId;
		}

		public void setProcessDefinitionId(String processDefinitionId) {
			this.processDefinitionId = processDefinitionId;
		}

		public String getProcessName() {
			return processName;
		}

		public void setProcessName(String processName) {
			this.processName = processName;
		}

		public String getFormKey() {
			return formKey;
		}

		public void setFormKey(String formKey) {
			this.formKey = formKey;
		}

		public String getProcessDefinitionName() {
			return processDefinitionName;
		}

		public void setProcessDefinitionName(String processDefinitionName) {
			this.processDefinitionName = processDefinitionName;
		}

	}
}