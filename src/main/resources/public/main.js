var paths = [
"/activiti",
"/autoconfig",
"/beans",
"/configprops",
"/dump",
"/env",
"/error",
"/form/form-data",
"/greeting",
"/health",
"/hello",
"/history/historic-activity-instances",
"/history/historic-detail",
"/history/historic-process-instances",
"/history/historic-task-instances",
"/history/historic-variable-instances",
"/home",
"/identity/groups",
"/identity/users",
"/info",
"/login",
"/management/engine",
"/management/jobs",
"/management/properties",
"/management/tables",
"/mappings",
"/metrics",
"/query/executions",
"/query/historic-activity-instances",
"/query/historic-detail",
"/query/historic-process-instances",
"/query/historic-task-instances",
"/query/historic-variable-instances",
"/query/process-instances",
"/query/tasks",
"/repository/deployments",
"/repository/models",
"/repository/process-definitions",
"/runtime/executions",
"/runtime/process-instances",
"/runtime/signals",
"/runtime/tasks",
"/shutdown",
"/simple-workflow",
"/tasks",
"/trace",
"/whoami"
]

if (!window.ui) {
	window.ui = {};
}

ui.logout = function () {
	document.forms.logoutForm.submit();
}