package com.tyflow.interceptor;

import org.flowable.idm.api.User;
import org.flowable.idm.engine.impl.persistence.entity.UserEntityImpl;
import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.idm.service.UserService;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
public class CustomHandlerInterceptor implements HandlerInterceptor {
	
	Logger logger = LoggerFactory.getLogger(CustomHandlerInterceptor.class);

	@Autowired
	private UserService userService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String servletPath = request.getServletPath();

		if (servletPath.endsWith(".css") || servletPath.endsWith(".js") || servletPath.endsWith(".jpg")
				|| servletPath.endsWith(".png")) {
			return true;
		}
		logger.debug("-------------访问路径为:{}---------------",servletPath);
		
		if (servletPath.startsWith("/app") || servletPath.startsWith("/idm")) {		
			User currentUserObject = SecurityUtils.getCurrentUserObject();
			if (currentUserObject == null) {			
				//原系统是通过security实现的，现在屏蔽了security ，所以要封装一个来绕过认证
				//从cas里获取
				Assertion assertion = (Assertion) request.getSession().getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
				if(assertion!=null) {
					Principal principal  = assertion.getPrincipal();      
		        	String loginName = principal.getName();
		        	org.flowable.ui.idm.model.UserInformation userInfo = userService.getUserInformation(loginName);
		        	
		        	if(userInfo!=null) {
		        		SecurityUtils.assumeUser(userInfo.getUser());		        	
		        	}else {
		        		//当没有 查到 时，就强行填一个
		        		SecurityUtils.assumeUser(getAdminUser() );
		        	}		        	
		        }else {
		        	//当没有开启cas时，就强行填一个        		
	        		SecurityUtils.assumeUser(getAdminUser());
		        }
			}
		}
		return true;
	}
	
	
	private User getAdminUser() {
		User user = new UserEntityImpl();
		user.setId("admin");
		user.setFirstName("Test");
		user.setLastName("Administrator");
		return user;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

}
