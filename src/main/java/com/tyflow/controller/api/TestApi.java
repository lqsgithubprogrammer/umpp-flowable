package com.tyflow.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@RestController
public class TestApi {

	@PostMapping("/api/post")
	public String testPost(String param) {
		return "this is Post . params-> " + param;
	}
	
	@PutMapping("/api/put")
	public void testPut(String param) {
		System.out.println("this is put. params->" + param);
	}
	
	@GetMapping("/api/testgrid")
	public JSONObject testgrid() {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for(int i = 0; i < 10; i++) {
			JSONObject o = new JSONObject();
			o.put("id", i);
			o.put("name", "name-"+i);
			o.put("key", "key-"+i);
			array.add(o);
		}
		json.put("data", array);
		return json;
	}
	
	@GetMapping("/api/zc/{id}")
	public JSONObject zc(@PathVariable String id) {
		JSONObject o = new JSONObject();
		o.put("id", id);
		o.put("name", "name-"+id);
		return o;
	}
	
}
