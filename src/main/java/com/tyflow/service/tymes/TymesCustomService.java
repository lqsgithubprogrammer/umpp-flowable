package com.tyflow.service.tymes;

import org.flowable.task.service.impl.persistence.entity.TaskEntity;

public interface TymesCustomService {
	/**
	 * 指定任务执行人
	 * @param taskId
	 * @param taskDefKey
	 */
	void taskAssign(TaskEntity taskEntity);
}
