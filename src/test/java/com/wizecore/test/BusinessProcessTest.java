package com.wizecore.test;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Title;

import com.wizecore.Application;
import com.wizecore.CalcObject;
import com.wizecore.CalcService;
import com.wizecore.SecurityConfig;
import com.wizecore.TaskRepresentation;
import com.wizecore.WorkflowService;

/**
 * Test business processes flow, multiple variants. 
 * @author Ruslan
 */
@Features("bpm")
@Title("Business Process Multiple Tests")
@RunWith(Parameterized.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusinessProcessTest extends Assert {
	Logger log = Logger.getLogger(getClass().getName());
	
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
	
	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();
	
	@Autowired
	RuntimeService runtimeService;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	ProcessEngine engine;
	
	@Rule
	public ActivitiRule rule;
	
	@Autowired
	CalcService calcService;
	
	@Autowired
	WorkflowService bpm;
	
	@Parameter(0)
	public int a;
	@Parameter(1)
	public int b;
	
	@Parameter(2)
	public String operation;
	
	@Parameter(3)
	public int c;
	
	@Parameter(4)
	public boolean fails;
	
	@Before
	public void init() {
		rule = new ActivitiRule(engine);
	}
	
	@Parameters(name = "{0} {2} {1} == {3} (fails? {4})")
	public static Object[][] processVariants() {
		return new Object[][] {
			{ 1, 2, "+", 3, false },
			{ 0, 311, "-", -311, false },
			{ 100, 0, "/", 0, true }
		};
	}
	
	/**
	 * Complete test using only Activiti API
	 */
	@Test
	@Deployment(resources = { "processes/calc.bpmn20.xml" })
	@Transactional
	@Title("Complete test using only Activiti API")
	public void testProcessViaActiviti() {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("calc");
		assertNotNull("Process instance started", pi);
		
		verify((CalcService) calcService, atLeastOnce()).starting(any());
		
		CalcObject calc = (CalcObject) runtimeService.getVariable(pi.getId(), "calc");
		assertNotNull(calc);
		
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
		assertNotNull("Having a list of user tasks", tasks);
		assertEquals("Only one task", 1, tasks.size());
		Task task = tasks.get(0);
		assertNotNull("Task is not null", task);
		
		task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskCandidateUser(SecurityConfig.TEST_USERNAME).singleResult();
		assertNotNull(task);
		assertEquals("First task is prompt", "prompt", task.getFormKey());
		
		taskService.claim(task.getId(), SecurityConfig.TEST_USERNAME);
		assertEquals("Task was assigned", 1, taskService.createTaskQuery().taskAssignee(SecurityConfig.TEST_USERNAME).processInstanceId(pi.getId()).taskId(task.getId()).count());
		
		calc = (CalcObject) runtimeService.getVariable(pi.getId(), "calc");
		calc.setA(a);
		calc.setB(b);
		calc.setOperation(operation);
		runtimeService.setVariable(pi.getId(), "calc", calc);
		taskService.complete(task.getId());
		
		HistoricTaskInstance completedTask = engine.getHistoryService().createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
		assertEquals("Task was completed", "completed", completedTask.getDeleteReason());
		
		verify((CalcService) calcService, atLeastOnce()).calculate(any());
		
		if (fails) {
			// invalid operation
			task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskCandidateUser(SecurityConfig.TEST_USERNAME).singleResult();
			assertNotNull(task);
			assertEquals("Error task is errors", "errors", task.getFormKey());
			
			String errors = (String) runtimeService.getVariable(pi.getId(), "error");
			assertNotNull(errors);
			
			taskService.claim(task.getId(), SecurityConfig.TEST_USERNAME);
			assertEquals("Task was assigned", 1, taskService.createTaskQuery().taskAssignee(SecurityConfig.TEST_USERNAME).processInstanceId(pi.getId()).taskId(task.getId()).count());
			taskService.complete(task.getId()); 
			completedTask = engine.getHistoryService().createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
			assertEquals("Task was completed", "completed", completedTask.getDeleteReason());
		} else {
			task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskCandidateUser(SecurityConfig.TEST_USERNAME).singleResult();
			assertNotNull(task);
			assertEquals("Second task is show", "show", task.getFormKey());
			
			calc = (CalcObject) runtimeService.getVariable(pi.getId(), "calc");
			assertNotNull(calc);
			assertEquals("calc.a successfully set", (Integer) a, calc.getA());
			assertEquals("calc.b successfully set", (Integer) b, calc.getB());
			assertEquals("calc.operation successfully set", operation, calc.getOperation());
	
			assertEquals("calc.c calculated", (Integer) c, calc.getC());
			
			taskService.claim(task.getId(), SecurityConfig.TEST_USERNAME);
			assertEquals("Task was assigned", 1, taskService.createTaskQuery().taskAssignee(SecurityConfig.TEST_USERNAME).processInstanceId(pi.getId()).taskId(task.getId()).count());
			taskService.complete(task.getId()); 
			completedTask = engine.getHistoryService().createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
			assertEquals("Task was completed", "completed", completedTask.getDeleteReason());
		}
	}
	
	/**
	 * Complete test using workflow API
	 */
	@Test
	@Deployment(resources = { "processes/calc.bpmn20.xml" })
	@Transactional
	@Title("Complete test using workflow API")
	public void testProcessViaWorkflow() {
		bpm.startProcess();
		List<TaskRepresentation> tasks = bpm.getTasks(SecurityConfig.TEST_USERNAME);
		assertNotNull("Having a list of user tasks", tasks);
		assertEquals("Only one task", 1, tasks.size());
		TaskRepresentation task = tasks.get(0);
		assertNotNull("Task is not null", task);
		assertEquals("Task is prompt", "prompt", task.getFormKey());
		
		Map<String, Object> taskVars = bpm.getTaskVariables(task.getId());
		CalcObject calc = (CalcObject) taskVars.get("calc");
		assertNotNull(calc);
		calc.setA(a);
		calc.setB(b);
		calc.setOperation(operation);
		bpm.completeTask(task.getId());
		
		tasks = bpm.getTasks(SecurityConfig.TEST_USERNAME);
		assertNotNull("Having a list of user tasks", tasks);
		assertEquals("Only one task", 1, tasks.size());
		task = tasks.get(0);
		
		if (fails) {
			assertEquals("Task is errors", "errors", task.getFormKey());
			taskVars = bpm.getTaskVariables(task.getId());
			String errors = (String) taskVars.get("error");
			assertNotNull(errors);
			bpm.completeTask(task.getId());
		} else {
			assertEquals("Task is show", "show", task.getFormKey());
			taskVars = bpm.getTaskVariables(task.getId());
			calc = (CalcObject) taskVars.get("calc");
			assertNotNull(calc);
			assertEquals((Integer)c, calc.getC());
			bpm.completeTask(task.getId());
		}
	}
}
