package com.tyflow.service.umpp.impl;

import com.alibaba.fastjson.JSONObject;
import com.tyflow.service.umpp.UmppCustomService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UmppCustomServiceImpl implements UmppCustomService {

    private static final Logger logger = LoggerFactory.getLogger(UmppCustomServiceImpl.class);

    @Autowired
    private RuntimeService runtimeSerivce;
    @Autowired
    private TaskService taskService;

    @Override
    public void taskAssign(TaskEntity taskEntity) {
        logger.info("\n【MES系统 开始指定任务执行人】");
        logger.info("------流程定义ID：{} 实例ID：{} ", taskEntity.getProcessDefinitionId(), taskEntity.getProcessInstanceId());
        logger.info("------任务实例ID：{}  key:{}  执行ID：{}------", taskEntity.getId(), taskEntity.getTaskDefinitionKey(), taskEntity.getExecutionId());

        String userName = (String) runtimeSerivce.getVariable(taskEntity.getExecutionId(), "defaultUser");
        String assignString = (String) runtimeSerivce.getVariable(taskEntity.getExecutionId(), "assignPersons");
        JSONObject sysAssignee = JSONObject.parseObject(assignString);
        if (sysAssignee != null) {
            for (String taskKey : sysAssignee.keySet()) {
                //符合该key的，值为空就设置defaultUser
                if (taskEntity.getTaskDefinitionKey().equals(taskKey)) {
                    if (org.apache.commons.lang3.StringUtils.isBlank(sysAssignee.getString(taskKey))) {
                        //设置默认的执行人
                        if (!StringUtils.isEmpty(userName)) {
                            try {
                                taskService.setAssignee(taskEntity.getId(), userName);
                                logger.info("设置执行人成功 用户：{}", userName);
                            } catch (Exception e) {
                                logger.error("设置执行人失败.", e);
                            }
                        } else {
                            logger.info("当前流程未指定执行人");
                        }
                    } else {
                        try {
                            taskService.setAssignee(taskEntity.getId(), sysAssignee.getString(taskKey));
                            logger.info("设置执行人成功 用户：{}", sysAssignee.getString(taskKey));
                        } catch (Exception e) {
                            logger.error("设置执行人失败.", e);
                        }
                    }
                }
            }
        }
    }
}
