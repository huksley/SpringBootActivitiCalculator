package com.wizecore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Provides internal API for workflow (BPM)
 * 
 * @author Ruslan
 */
@Service
@Component
public class WorkflowService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    /**
     * Start process.
     */
	@Transactional
    public void startProcess() {
        runtimeService.startProcessInstanceByKey("calc");
    }
	
	/**
	 * Return current process name.
	 * @param id Process instance Id
	 */
	public String processInstanceName(String id) {
		return runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult().getName();
	}
	
	/**
	 * Return current process definition name.
	 * 
	 */
	public String processInstanceDefinitionName(String id) {
		return runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult().getProcessDefinitionKey();
	}
	
	/**
	 * Complete task.
	 * @param taskId Task Id
	 */
	public void completeTask(String taskId) {
		taskService.complete(taskId);
	}

	/**
	 * Return list of tasks assigned or available for assignment for specified user.
	 * @param assignee User name
	 * @return List of tasks, can be empty.
	 */
	@Transactional
	@NotNull
    public List<TaskRepresentation> getTasks(String assignee) {
        List<Task> l = taskService.createTaskQuery().taskAssignee(assignee).list();
        if (l.size() == 0) {
        	l = taskService.createTaskQuery().taskCandidateUser(assignee).list();
        }
        
		List<TaskRepresentation> dtos = new ArrayList<TaskRepresentation>();
		for (Task task : l) {
			dtos.add(new TaskRepresentation(this, task));
		}
		return dtos;
    }

	/**
	 * Returns task variables for specified task.
	 * 
	 * @param taskId Task id
	 * @return
	 */
	public Map<String, Object> getTaskVariables(String taskId) {
		Map<String, Object> vars = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult().getProcessVariables();
		return vars;
	}
}