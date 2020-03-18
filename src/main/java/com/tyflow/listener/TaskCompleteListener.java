package com.tyflow.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

public class TaskCompleteListener implements ExecutionListener{


	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) {
		System.out.println("...进入任务完成监听器");
	}


	

	
}
