/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.config;

import org.datagear.web.util.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.datagear.web.util.SubContextPathRequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Web配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
public class DGWebMvcRegistrations implements WebMvcRegistrations
{
	private DGCoreConfiguration dGCoreConfiguration;

	private Environment environment;

	@Autowired
	public DGWebMvcRegistrations(DGCoreConfiguration dGCoreConfiguration, Environment environment)
	{
		super();
		this.dGCoreConfiguration = dGCoreConfiguration;
		this.environment = environment;
	}

	public DGCoreConfiguration getCoreConfiguration()
	{
		return dGCoreConfiguration;
	}

	public void setCoreConfiguration(DGCoreConfiguration dGCoreConfiguration)
	{
		this.dGCoreConfiguration = dGCoreConfiguration;
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping()
	{
		SubContextPathRequestMappingHandlerMapping bean = new SubContextPathRequestMappingHandlerMapping();
		bean.setAlwaysUseFullPath(true);
		bean.setSubContextPath(this.environment.getProperty("subContextPath"));

		return bean;
	}

	@Override
	public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver()
	{
		return new DeliverContentTypeExceptionHandlerExceptionResolver();
	}
}
