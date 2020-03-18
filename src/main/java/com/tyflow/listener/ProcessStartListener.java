package com.tyflow.listener;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;

public class ProcessStartListener implements FlowableEventListener{

	@Override
	public void onEvent(FlowableEvent event) {
		// TODO Auto-generated method stub
		System.out.println("...触发流程启动监听器");
	}

	@Override
	public boolean isFailOnException() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFireOnTransactionLifecycleEvent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getOnTransaction() {
		// TODO Auto-generated method stub
		return null;
	}



}
