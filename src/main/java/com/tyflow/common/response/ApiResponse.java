package com.tyflow.common.response;

import java.io.Serializable;

public class ApiResponse implements Serializable{

	private static final long serialVersionUID = 1L;
	private Object data;
	private boolean state; 
	private String message;

	public ApiResponse() {

	}

	public static ApiResponse success(Object data) {
		ApiResponse o = new ApiResponse();
		o.setData(data);
		o.setState(true);
		return o;
	}

	public static ApiResponse success(Object data, String msg) {
		ApiResponse o = new ApiResponse();
		o.setData(data);
		o.setMessage(msg);
		o.setState(true);
		return o;
	}

	public static ApiResponse fail(String msg) {
		ApiResponse o = new ApiResponse();
		o.setMessage(msg);
		o.setState(false);
		return o;
	}

	public static ApiResponse fail(Object data, String msg) {
		ApiResponse o = new ApiResponse();
		o.setData(data);
		o.setMessage(msg);
		o.setState(false);
		return o;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
