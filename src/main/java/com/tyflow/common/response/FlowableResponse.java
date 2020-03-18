package com.tyflow.common.response;

public class FlowableResponse extends ApiResponse{
	private static final long serialVersionUID = 1L;
	private int code;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public static FlowableResponse success(Object data) {
		FlowableResponse o = new FlowableResponse();
		o.setData(data);
		o.setState(true);
		return o;
	}

	public static FlowableResponse success(Object data, String msg) {
		FlowableResponse o = new FlowableResponse();
		o.setData(data);
		o.setMessage(msg);
		o.setState(true);
		return o;
	}
	
	public static FlowableResponse fail(String msg) {
		FlowableResponse o = new FlowableResponse();
		o.setMessage(msg);
		o.setState(false);
		return o;
	}

	public static FlowableResponse fail(String msg, int code) {
		FlowableResponse o = new FlowableResponse();
		o.setMessage(msg);
		o.setState(false);
		o.setCode(code);
		return o;
	}

	public static FlowableResponse fail(Object data, String msg, int code) {
		FlowableResponse o = new FlowableResponse();
		o.setData(data);
		o.setMessage(msg);
		o.setState(false);
		o.setCode(code);
		return o;
	}

}
