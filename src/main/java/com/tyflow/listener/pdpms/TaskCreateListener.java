package com.tyflow.listener.pdpms;

import com.tyflow.service.umpp.UmppCustomService;
import org.checkerframework.checker.guieffect.qual.UI;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tyflow.common.CustomConstants;
import com.tyflow.common.utils.SpringUtil;
import com.tyflow.service.pdpms.PdpmsCustomService;
import com.tyflow.service.tymes.TymesCustomService;
/**
 * pdpms系统定制监听器
 * 任务创建时 指定 执行人
 * @author xwq
 *
 */
public class TaskCreateListener implements FlowableEventListener{

	private static final Logger logger = LoggerFactory.getLogger(TaskCreateListener.class);
	
	@Autowired
	private PdpmsCustomService pdpmsCustomService;
	@Autowired
	private TymesCustomService tymesCustomService;
	@Autowired
	private UmppCustomService umppCustomService;
	@Autowired
	private CustomConstants customConstants;
	
	@Override
	public void onEvent(FlowableEvent event) {
		if (event.getType() == FlowableEngineEventType.TASK_CREATED) {
			customConstants = SpringUtil.getBean(CustomConstants.class);
			logger.info("...进入 任务创建 监听器   业务系统：【{}】", customConstants.businessSystem);
			FlowableEntityEvent entityEvent = (FlowableEntityEvent) event;
			if(entityEvent.getEntity() instanceof TaskEntity) {
				TaskEntity task = (TaskEntity) entityEvent.getEntity();
				logger.info("任务ID：{}  key:{} 表单key:{}",task.getId(),task.getTaskDefinitionKey(),task.getFormKey());			
				//业务的特殊定制
				if("mes".equals(customConstants.businessSystem.trim())) {//mes系统
					tymesCustomService = SpringUtil.getBean(TymesCustomService.class);
					tymesCustomService.taskAssign(task);
				}else if("umpp".equals(customConstants.businessSystem.trim())){  //中车系统
					umppCustomService=SpringUtil.getBean(UmppCustomService.class);
					umppCustomService.taskAssign(task);
				}else {    //pdpms系统的人员指定
					pdpmsCustomService = SpringUtil.getBean(PdpmsCustomService.class);
					pdpmsCustomService.taskAssign(task);
				}
			}
		}
	}

	@Override
	public boolean isFailOnException() {
		logger.info("...isFailOnException");
		return false;
	}

	@Override
	public boolean isFireOnTransactionLifecycleEvent() {
		logger.info("...isFireOnTransactionLifecycleEvent");
		return false;
	}

	@Override
	public String getOnTransaction() {
		logger.info("...getOnTransaction");
		return null;
	}

	
}
