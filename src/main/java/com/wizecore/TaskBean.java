package com.wizecore;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/**
 * Provides request based task information.
 * 
 * @author Ruslan
 */
@Component
@Scope(value = "request")
public class TaskBean {
	@Autowired
	WorkflowService workflow;
	
	@Autowired
	private HttpServletRequest request;

	/**
	 * Return current tasks available for user.
	 * 
	 * @return
	 */
	public TaskRepresentation[] getTasks() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<TaskRepresentation> tasks = workflow.getTasks(user.getUsername());
		return tasks.toArray(new TaskRepresentation[tasks.size()]);
	}
	
	/**
	 * Return variable for current task
	 * 
	 * @param name Variable name
	 * @return
	 */
	public Object var(String name) {
		String taskId = request.getParameter("taskId");
		return workflow.getTaskVariables(taskId).get(name);
	}
}