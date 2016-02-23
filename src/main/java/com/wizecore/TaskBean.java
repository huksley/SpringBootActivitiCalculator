package com.wizecore;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request")
public class TaskBean {
	@Autowired
	WorkflowService workflow;
	
	public Task[] getTasks() {
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Task> tasks = workflow.getTasks(user.getUsername());
		return tasks.toArray(new Task[tasks.size()]);
	}
	
	@Autowired
	private HttpServletRequest request;
	
	public Object var(String name) {
		String taskId = request.getParameter("taskId");
		return workflow.taskVars(taskId).get(name);
	}
}