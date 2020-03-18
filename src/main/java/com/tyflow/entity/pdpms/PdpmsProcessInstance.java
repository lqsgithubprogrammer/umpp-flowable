package com.tyflow.entity.pdpms;

import java.sql.Date;

public class PdpmsProcessInstance {
	
	private String id;
	private String planId;
	private String creator;
	private String procInsId;
	private Date startTime;
	private Date endTime;
	private String initData;
	private int state;
	private int type;
	
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPlanId() {
		return planId;
	}
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getProcInsId() {
		return procInsId;
	}
	public void setProcInsId(String procInsId) {
		this.procInsId = procInsId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getInitData() {
		return initData;
	}
	public void setInitData(String initData) {
		this.initData = initData;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	
}
