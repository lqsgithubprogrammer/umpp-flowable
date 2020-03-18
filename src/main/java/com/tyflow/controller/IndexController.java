package com.tyflow.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
			
	/**
	 * 跳转至modeler首页用的
	 * @return
	 */
	@RequestMapping("/")
	public void index(HttpServletRequest request,HttpServletResponse response) {
		try {		
			response.sendRedirect(request.getContextPath()+"/modeler/index.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
