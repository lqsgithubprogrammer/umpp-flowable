package com.tyflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.tyflow.config.properties.DataSourceConfigProperty;


@Component
@ConfigurationProperties(prefix="spring.datasource")
public class DataSourceConfig extends DataSourceConfigProperty {
	@Override
	public String toString() {
		return super.toString();
	}
}
