/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.config;

import org.datagear.web.config.support.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

/**
 * Web配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
public class WebMvcRegistrationsConfig implements WebMvcRegistrations
{
	private CoreConfig coreConfig;

	private Environment environment;

	@Autowired
	public WebMvcRegistrationsConfig(CoreConfig coreConfig, Environment environment)
	{
		super();
		this.coreConfig = coreConfig;
		this.environment = environment;
	}

	public CoreConfig getCoreConfig()
	{
		return coreConfig;
	}

	public void setCoreConfig(CoreConfig coreConfig)
	{
		this.coreConfig = coreConfig;
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
	public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver()
	{
		return new DeliverContentTypeExceptionHandlerExceptionResolver();
	}
}
