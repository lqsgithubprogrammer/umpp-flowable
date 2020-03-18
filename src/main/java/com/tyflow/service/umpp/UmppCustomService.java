package com.tyflow.service.umpp;

import org.flowable.task.service.impl.persistence.entity.TaskEntity;

public interface UmppCustomService {
	/**
	 * 指定任务执行人
	 * @param taskId
	 * @param taskDefKey
	 */
	void taskAssign(TaskEntity taskEntity);
}
