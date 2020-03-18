package com.tyflow.service.pdpms;

import org.flowable.task.service.impl.persistence.entity.TaskEntity;

/**
 * pdpms系统定制服务
 * @author xwq
 *
 */
public interface PdpmsCustomService {
	
	/**
	 * 指定任务执行人
	 * @param taskId
	 * @param taskDefKey
	 */
	void taskAssign(TaskEntity taskEntity);
	
}
