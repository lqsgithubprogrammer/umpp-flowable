package com.tyflow.service.impl;

import java.util.List;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.CallActivity;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tyflow.service.BpmnCustomService;

@Transactional
@Service("BpmnCustomService")
public class BpmnCustomServiceImpl implements BpmnCustomService {

	private static Logger logger = LoggerFactory.getLogger(BpmnCustomServiceImpl.class);

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ModelService modelService;

	@Override
	public JSONArray findBpmnUserTaskByProcessDefinitionId(String processDefinitionId) {
		logger.info("根据流程定义获取任务  流程定义id :{}", processDefinitionId);
		BpmnModel bpmdModel = repositoryService.getBpmnModel(processDefinitionId);
		JSONArray tasksJson = new JSONArray();
		if (bpmdModel != null) {
			// 遍历一个model下的所有流程
			for (Process process : bpmdModel.getProcesses()) {
				// 获取此流程下的“可调用子流程” , 并进行递归调用
				List<CallActivity> callActivities = process.findFlowElementsOfType(CallActivity.class);
				for (CallActivity ca : callActivities) {
					String caProcessDefinitionId = ca.getCalledElement();// 在流程定义里，必须吧callActivitType定义为id，否则无效
					logger.info("递归获取子流程下的任务，子流程定义Id:{}", caProcessDefinitionId);
					tasksJson.addAll(this.findBpmnUserTaskByProcessDefinitionId(caProcessDefinitionId));
				}
				// 获取此流程下的userTask
				List<UserTask> tasks = process.findFlowElementsOfType(UserTask.class);
				for (UserTask task : tasks) {
					JSONObject obj = new JSONObject();
                    obj.put("id", task.getId());
                    obj.put("name", task.getName());
                    obj.put("formKey",task.getFormKey());
                    obj.put("documentation",task.getDocumentation());
					tasksJson.add(obj);
				}

			}
		}
		return tasksJson;
	}

	@Override
	public JSONArray findBpmnUserTaskByModelId(String modelId) {
		logger.info("通过模型ID查找用户任务。 模型ID：{}", modelId);
		BpmnModel bpmnModel = null;
		try {
			Model model = modelService.getModel(modelId);
			bpmnModel = modelService.getBpmnModel(model);
		} catch (Exception e) {
			logger.error("模型转化出错，或者模型不存在", e);
			return null;
		}
		JSONArray array = new JSONArray();
		// 遍历一个model下的所有流程
		for (Process process : bpmnModel.getProcesses()) {
			// 获取此流程下的“可调用子流程” , 并进行递归调用
			List<CallActivity> callActivities = process.findFlowElementsOfType(CallActivity.class);
			for (CallActivity ca : callActivities) {
				String caProcessDefinitionId = ca.getCalledElement();// 在流程定义里，必须吧callActivitType定义为id，否则无效
				logger.info("递归获取子流程下的任务，子流程定义Id:{}", caProcessDefinitionId);
				array.addAll(this.findBpmnUserTaskByProcessDefinitionId(caProcessDefinitionId));
			}
			// 获取此流程下的userTask
			List<UserTask> tasks = process.findFlowElementsOfType(UserTask.class);
			for (UserTask task : tasks) {
				JSONObject obj = new JSONObject();
				obj.put("id", task.getId());
				obj.put("name", task.getName());
				obj.put("formKey",task.getFormKey());
				obj.put("documentation",task.getDocumentation());
				array.add(obj);
			}

		}
		return array;
	}

}
