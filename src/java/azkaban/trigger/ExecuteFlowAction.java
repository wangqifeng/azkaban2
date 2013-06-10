package azkaban.trigger;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import azkaban.executor.ExecutableFlow;
import azkaban.executor.ExecutionOptions;
import azkaban.executor.ExecutorManager;
import azkaban.executor.ExecutorManagerException;
import azkaban.flow.Flow;
import azkaban.project.Project;
import azkaban.project.ProjectManager;

public class ExecuteFlowAction implements TriggerAction {

	public static final String type = "ExecuteFlow";
	
	private static ExecutorManager executorManager;
	private int projectId;
	private String flowName;
	private String submitUser;
	private static ProjectManager projectManager;
	private ExecutionOptions executionOptions;
	

	private static Logger logger;
	
	public ExecuteFlowAction(int projectId, String flowName, String submitUser, ExecutionOptions executionOptions) {
		this.projectId = projectId;
		this.flowName = flowName;
		this.submitUser = submitUser;
		this.executionOptions = executionOptions;
	}
	
	public static void setLogger(Logger logger) {
		ExecuteFlowAction.logger = logger;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getSubmitUser() {
		return submitUser;
	}

	public void setSubmitUser(String submitUser) {
		this.submitUser = submitUser;
	}

	public ExecutionOptions getExecutionOptions() {
		return executionOptions;
	}

	public void setExecutionOptions(ExecutionOptions executionOptions) {
		this.executionOptions = executionOptions;
	}

	public static void setExecutorManager(ExecutorManager executorManager) {
		ExecuteFlowAction.executorManager = executorManager;
	}

	public static void setProjectManager(ProjectManager projectManager) {
		ExecuteFlowAction.projectManager = projectManager;
	}

	@Override
	public String getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TriggerAction fromJson(Object obj) {
		Map<String, Object> jsonObj = (HashMap<String, Object>) obj;
		String type = (String) jsonObj.get("type");
		if(! type.equals(ExecuteFlowAction.type)) {
			throw new RuntimeException("Cannot create action of " + ExecuteFlowAction.type + " from " + type);
		}
		int projectId = Integer.valueOf((String)jsonObj.get("projectId"));
		String flowName = (String) jsonObj.get("flowName");
		String submitUser = (String) jsonObj.get("submitUser");
		ExecutionOptions executionOptions = ExecutionOptions.createFromObject(jsonObj.get("executionOptions"));
		return new ExecuteFlowAction(projectId, flowName, submitUser, executionOptions);
	}

	@SuppressWarnings("unchecked")
	public static TriggerAction createFromJson(HashMap obj) {
		Map<String, Object> jsonObj = (HashMap<String, Object>) obj;
		String type = (String) jsonObj.get("type");
		if(! type.equals(ExecuteFlowAction.type)) {
			throw new RuntimeException("Cannot create action of " + ExecuteFlowAction.type + " from " + type);
		}
		int projectId = Integer.valueOf((String)jsonObj.get("projectId"));
		String flowName = (String) jsonObj.get("flowName");
		String submitUser = (String) jsonObj.get("submitUser");
		ExecutionOptions executionOptions = ExecutionOptions.createFromObject(jsonObj.get("executionOptions"));
		return new ExecuteFlowAction(projectId, flowName, submitUser, executionOptions);
	}
	
	@SuppressWarnings("unchecked")
	public static TriggerAction createFromJson(Object obj) {
		Map<String, Object> jsonObj = (HashMap<String, Object>) obj;
		String type = (String) jsonObj.get("type");
		if(! type.equals(ExecuteFlowAction.type)) {
			throw new RuntimeException("Cannot create action of " + ExecuteFlowAction.type + " from " + type);
		}
		int projectId = Integer.valueOf((String)jsonObj.get("projectId"));
		String flowName = (String) jsonObj.get("flowName");
		String submitUser = (String) jsonObj.get("submitUser");
		ExecutionOptions executionOptions = ExecutionOptions.createFromObject(jsonObj.get("executionOptions"));
		return new ExecuteFlowAction(projectId, flowName, submitUser, executionOptions);
	}
	
	@Override
	public Object toJson() {
		Map<String, Object> jsonObj = new HashMap<String, Object>();
		jsonObj.put("type", type);
		jsonObj.put("projectId", String.valueOf(projectId));
		jsonObj.put("flowName", flowName);
		jsonObj.put("submitUser", submitUser);
		jsonObj.put("executionOptions", executionOptions.toObject());

		return jsonObj;
	}

	@Override
	public void doAction() {
		Project project = projectManager.getProject(projectId);
		if(project == null) {
			logger.error("Project to execute " + projectId + " does not exist!");
			throw new RuntimeException("Error finding the project to execute " + projectId);
		}
		
		Flow flow = project.getFlow(flowName);
		if(flow == null) {
			logger.error("Flow " + flowName + " cannot be found in project " + project.getName());
			throw new RuntimeException("Error finding the flow to execute " + flowName);
		}
		
		ExecutableFlow exflow = new ExecutableFlow(flow);
		exflow.setSubmitUser(submitUser);
		exflow.addAllProxyUsers(project.getProxyUsers());
		
		if(!executionOptions.isFailureEmailsOverridden()) {
			executionOptions.setFailureEmails(flow.getFailureEmails());
		}
		if(!executionOptions.isSuccessEmailsOverridden()) {
			executionOptions.setSuccessEmails(flow.getSuccessEmails());
		}
		exflow.setExecutionOptions(executionOptions);
		
		try{
			executorManager.submitExecutableFlow(exflow);
			logger.info("Invoked flow " + project.getName() + "." + flowName);
		} catch (ExecutorManagerException e) {
			throw new RuntimeException(e);
		}
		
	}


}