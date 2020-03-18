package com.tyflow.service;

import com.alibaba.fastjson.JSONArray;
/**
 * Bpmn模型相关的自定义服务
 * @author xwq
 *
 */
public interface BpmnCustomService {
	
	/**
	 * 通过流程定义ID，查找该流程模型中的所有任务
	 * @param processDefionId 流程定义id
	 * @return
	 */
	JSONArray findBpmnUserTaskByProcessDefinitionId(String processDefinitionId);
	
	/**
	 * 通过模型ID，查找流程模型中的所有用户任务
	 * @param modelId
	 * @return
	 */
	JSONArray findBpmnUserTaskByModelId(String modelId);
}
