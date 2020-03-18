package com.tyflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import com.tyflow.interceptor.CustomHandlerInterceptor;

import javax.annotation.Resource;

@Configuration
@EnableWebMvc
public class StaticResourceConfig implements WebMvcConfigurer {

    @Resource
    private CustomHandlerInterceptor baseInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(baseInterceptor)
                //告知拦截器：/static/admin/** 与 /static/user/** 不需要拦截 （配置的是 路径）
                .excludePathPatterns("/static/**").excludePathPatterns("/service/**");
        
    }

    /**
     * 添加静态资源文件，外部可以直接访问地址
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //需要告知系统，这是要被当成静态文件的！
        //第一个方法设置访问路径前缀，第二个方法设置资源路径
    	//把所有访问路径 都映射到 静态资源下
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        
        //所有静态资源访问 映射到 静态资源路径
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 允许跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
    	registry.addMapping("/**")  
        .allowedOrigins("*")  
        .allowCredentials(true)  
        .allowedMethods("GET", "POST", "DELETE", "PUT")  
        .maxAge(3600); 
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //registry.addViewController("/error/404").setViewName("/admin/page_error/error_404.html");
    }
}
