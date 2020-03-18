package com.tyflow;

import org.flowable.ui.common.rest.idm.remote.RemoteAccountResource;
import org.flowable.ui.idm.rest.app.AccountResource;
import org.flowable.ui.modeler.rest.app.EditorGroupsResource;
import org.flowable.ui.modeler.rest.app.EditorUsersResource;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

//import net.unicon.cas.client.configuration.EnableCasClient;

@SpringBootApplication(
		exclude= {
				//屏蔽springSecurity配置，避免modeler本身项目的认证
				SecurityAutoConfiguration.class,
				UserDetailsServiceAutoConfiguration.class,
				LiquibaseAutoConfiguration.class
				})
@ComponentScan(basePackages = {"com.tyflow","org.flowable.ui","org.flowable.rest.service.api"},nameGenerator = UniqueNameGenerator.class,
				excludeFilters= @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
				classes = {RemoteAccountResource.class, //把用户登录屏蔽，换成我们自定义的
						AccountResource.class,
						EditorUsersResource.class,	
						EditorGroupsResource.class
				})
)
//@EnableCasClient  //打开单点登录客户端认证
public class TyflowApplication extends SpringBootServletInitializer{

	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(TyflowApplication.class);
	}
	
	public static void main(String[] args) {
		new SpringApplicationBuilder(TyflowApplication.class).web(WebApplicationType.SERVLET).run(args);
	}


}
