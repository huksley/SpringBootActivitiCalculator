package com.wizecore;

import org.activiti.engine.task.Task;

/**
 * Simplified task representation.
 * 
 * @author Ruslan
 */
public class TaskRepresentation {

	private String id;
	private String name;
	private String processDefinitionId;
	private String processDefinitionName;
	private String processInstanceId;
	private String processName;
	private String formKey;

	public TaskRepresentation(WorkflowService bpm, Task task) {
		this.id = task.getId();
		this.name = task.getName();
		this.setProcessDefinitionId(task.getProcessDefinitionId());
		this.setProcessInstanceId(task.getProcessInstanceId());
		formKey = task.getFormKey();
		processName = bpm.processInstanceName(task.getProcessInstanceId());
		processDefinitionName = bpm.processInstanceDefinitionName(task.getProcessInstanceId());
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