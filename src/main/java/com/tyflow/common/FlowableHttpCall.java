package com.tyflow.common;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tyflow.common.response.FlowableResponse;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * flowable的Http调用
 * 
 * @author xwq
 *
 */
@Component
public class FlowableHttpCall {

	private static Logger logger = LoggerFactory.getLogger(FlowableHttpCall.class);

	private static String flowableRestContextPath;
	private static String userName;
	private static String password;

	private static FlowableHttpCall instance;
	private static OkHttpClient okHttpClient;

	public FlowableHttpCall() {
		okHttpClient = new OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS)
				.connectTimeout(10, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).build();
	}

	public static FlowableHttpCall getInstance() {
		if (instance != null) {
			synchronized (FlowableHttpCall.class) {
				if (instance == null) {
					instance = new FlowableHttpCall();
				}
			}
		}
		return instance;
	}

	public static FlowableResponse get(String url, Map<String, Object> params) {
		Request request = new Request.Builder().url(attachHttpGetParams(flowableRestContextPath + url, params)).get()
				.addHeader("Authorization", Credentials.basic(userName, password)).build();

		Call call = okHttpClient.newCall(request);
		try {
			Response response = call.execute();
			if (response.isSuccessful()) {
				return FlowableResponse.success(response.body().string());
			} else {
				return FlowableResponse.fail("调用失败", response.code());
			}
		} catch (IOException e) {
			logger.error("网络错误:{}", e.getMessage());
		}
		return FlowableResponse.fail("系统异常");
	}

	public static FlowableResponse post(String url, Map<String, Object> params) {
		FormBody.Builder formbody = new FormBody.Builder();
		if (null != params) {
			Iterator<Entry<String, Object>> iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Map.Entry<String, String> elem = (Map.Entry) iterator.next();
				formbody.add((String) elem.getKey(), (String) elem.getValue());
			}
		}
		RequestBody body = formbody.build();

		Request request = new Request.Builder().url(flowableRestContextPath + url).post(body)
				.addHeader("Authorization", Credentials.basic(userName, password)).build();

		Call call = okHttpClient.newCall(request);

		try {
			Response response = call.execute();
			if (response.isSuccessful()) {
				return FlowableResponse.success(response.body().string());
			} else {
				return FlowableResponse.fail("调用失败", response.code());
			}
		} catch (IOException e) {
			logger.error("网络错误:{}", e.getMessage());
		}
		return FlowableResponse.fail("系统异常");

	}

	public static FlowableResponse delete(String url, Map<String, Object> params) {
		FormBody.Builder formbody = new FormBody.Builder();
		if (null != params) {
			Iterator<Entry<String, Object>> iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Map.Entry<String, String> elem = (Map.Entry) iterator.next();
				formbody.add((String) elem.getKey(), (String) elem.getValue());
			}
		}
		RequestBody body = formbody.build();

		Request request = new Request.Builder().url(flowableRestContextPath + url).delete(body)
				.addHeader("Authorization", Credentials.basic(userName, password)).build();

		Call call = okHttpClient.newCall(request);

		try {
			Response response = call.execute();
			if (204 == response.code()) {
				return FlowableResponse.success(response.body().string());
			} else {
				return FlowableResponse.fail("调用失败", response.code());
			}
		} catch (IOException e) {
			logger.error("网络错误:{}", e.getMessage());
		}
		return FlowableResponse.fail("系统异常");
	}

	public static FlowableResponse put(String url, Map<String, Object> params) {

		FormBody.Builder formbody = new FormBody.Builder();
		if (null != params) {
			Iterator<Entry<String, Object>> iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Map.Entry<String, String> elem = (Map.Entry) iterator.next();
				formbody.add((String) elem.getKey(), (String) elem.getValue());
			}
		}
		RequestBody body = formbody.build();

		Request request = new Request.Builder().url(flowableRestContextPath + url).put(body)
				.addHeader("Authorization", Credentials.basic(userName, password)).build();

		Call call = okHttpClient.newCall(request);

		try {
			Response response = call.execute();
			if (response.isSuccessful()) {
				return FlowableResponse.success(response.body().string());
			} else {
				return FlowableResponse.fail("调用失败", response.code());
			}
		} catch (IOException e) {
			logger.error("网络错误:{}", e.getMessage());
		}
		return FlowableResponse.fail("系统异常");
	}

	public static String attachHttpGetParams(String url, Map<String, Object> params) {

		if (params == null)
			return url;
		Iterator<String> keys = params.keySet().iterator();
		Iterator<Object> values = params.values().iterator();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("?");

		for (int i = 0; i < params.size(); i++) {
			String value = null;
			try {
				value = URLEncoder.encode((String) values.next(), "utf-8");
			} catch (Exception e) {
				e.printStackTrace();
			}

			stringBuffer.append(keys.next() + "=" + value);
			if (i != params.size() - 1) {
				stringBuffer.append("&");
			}
		}

		return url + stringBuffer.toString();
	}

	@Value("${flowablerest.context-path}")
	public void setFlowableRestContextPath(String flowableRestContextPath) {
		FlowableHttpCall.flowableRestContextPath = flowableRestContextPath;
	}

	@Value("${flowable.common.app.idm-admin.user}")
	public void setUserName(String userName) {
		FlowableHttpCall.userName = userName;
	}

	@Value("${flowable.common.app.idm-admin.password}")
	public void setPassword(String password) {
		FlowableHttpCall.password = password;
	}

}
