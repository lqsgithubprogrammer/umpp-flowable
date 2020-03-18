package com.tyflow.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 定义的常量
 * @author Administrator
 *
 */
@Component
public class CustomConstants {
	@Value("${business-system}")
	public String businessSystem;
}
