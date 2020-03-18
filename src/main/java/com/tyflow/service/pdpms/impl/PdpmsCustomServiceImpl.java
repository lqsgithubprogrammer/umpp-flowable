package com.tyflow.service.pdpms.impl;

import java.util.HashMap;

import org.flowable.engine.FormService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyflow.service.pdpms.PdpmsCustomService;

@Service("PdpmsCustomService")
@Transactional
public class PdpmsCustomServiceImpl implements PdpmsCustomService{

	private static final Logger logger = LoggerFactory.getLogger(PdpmsCustomServiceImpl.class);
	
	@Autowired
	private RuntimeService runtimeSerivce;
	@Autowired
	private TaskService taskService;
	@Autowired 
	private FormService formService;
	
	@SuppressWarnings("unchecked")
	@Override
	public void taskAssign(TaskEntity taskEntity) {
		logger.info("-------Pdpms系统 任务执行人设置----");
		logger.info("------流程定义ID：{} 实例ID：{} ",taskEntity.getProcessDefinitionId(),taskEntity.getProcessInstanceId());
		logger.info("------任务实例ID：{}  key:{}  执行ID：{}------",taskEntity.getId(),taskEntity.getTaskDefinitionKey(),taskEntity.getExecutionId());
		/*
		 * 当触发了任务create监听器，获取了任务实体，就会调用此接口，来为任务设定执行人
		 * 在pdpms系统的业务中，任务的执行人是在 “任务定制”时就已经定制好的，所以需要从那里提取出来。
		 * 在流程启动时，会设置流程参数，通过流程参数进行查找人员
		 */
		HashMap<String, String> taskAssigner = null;
		String str = (String) runtimeSerivce.getVariable(taskEntity.getExecutionId(), "task-assigner");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			if(str!=null&&!"".equals(str)) {
				taskAssigner = objectMapper.readValue(str, HashMap.class);				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		if(taskAssigner!=null) {
			String assginer = taskAssigner.get(taskEntity.getTaskDefinitionKey());
			if(StringUtils.isEmpty(assginer)) {
				logger.error("当前任务没有设定执行人  任务ID:{}",taskEntity.getId());
			}else {
				taskService.setAssignee(taskEntity.getId(), assginer);
				logger.info("---任务执行人：{}  指定完成 ",assginer);
			}
		}else {
			logger.info("---未查找到执行人指定变量---");

		}
	}

}
