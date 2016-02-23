package com.wizecore;

import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.StaticMetamodel;
import javax.transaction.Transactional;

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
 *
 */
@Service
@Component
public class WorkflowService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

	@Transactional
    public void startProcess() {
        runtimeService.startProcessInstanceByKey("calc");
    }
	
	public String processName(String id) {
		return runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult().getName();
	}
	
	public String processDefinitionName(String id) {
		return runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult().getProcessDefinitionKey();
	}
	
	public void completeTask(String id) {
		taskService.complete(id);
	}

	@Transactional
    public List<Task> getTasks(String assignee) {
        List<Task> l = taskService.createTaskQuery().taskAssignee(assignee).list();
        if (l.size() == 0) {
        	l = taskService.createTaskQuery().taskCandidateUser(assignee).list();
        }
        return l;
    }

	public Map<String, Object> taskVars(String taskId) {
		Map<String, Object> vars = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult().getProcessVariables();
		return vars;
	}
}