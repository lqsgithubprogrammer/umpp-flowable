package com.tyflow.controller.override;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.flowable.idm.api.User;
import org.flowable.idm.engine.impl.persistence.entity.UserEntityImpl;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.idm.service.UserService;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 重写 获取 登录用户
 * 
 * @author xwq
 *
 */
@RestController
@RequestMapping("/app")
public class RemoteAccountController {

	Logger logger = LoggerFactory.getLogger(RemoteAccountController.class);

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/rest/account")
	public UserRepresentation getAccount(HttpServletRequest request) {
		logger.info("-----获取登录用户-----");
		Assertion assertion = (Assertion) request.getSession().getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
		UserRepresentation userRepresentation = null;
		if (assertion != null) {
			Principal principal = assertion.getPrincipal();
			String loginName = principal.getName();
			logger.info("-----登录用户名为：{}-----", loginName);
			// 业务系统的loginname就是flowable的id
			org.flowable.ui.idm.model.UserInformation userInfo = userService.getUserInformation(loginName);
			if (userInfo != null) {
				userRepresentation = new UserRepresentation(userInfo.getUser());
			} else {
				User user = new UserEntityImpl();
				user.setId("admin");
				user.setFirstName("Test");
				user.setLastName("Administrator");
				userRepresentation = new UserRepresentation(user);
			}
		} else {
			User user = new UserEntityImpl();
			user.setId("admin");
			user.setFirstName("Test");
			user.setLastName("Administrator");
			userRepresentation = new UserRepresentation(user);
		}
		return userRepresentation;
	}
}
