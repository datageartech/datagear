/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.validation.Validator;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Web配置。
 * 
 * @author datagear@163.com
 */
// @Configuration(proxyBeanMethods = false)
// @EnableWebMvc
// @ComponentScan(basePackageClasses = MainController.class)
public class WebConfig extends WebMvcConfigurationSupport
{
	private CoreConfig coreConfig;

	private Environment environment;

	@Autowired
	public WebConfig(CoreConfig coreConfig, Environment environment)
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
	protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer(
			FormattingConversionService mvcConversionService, Validator mvcValidator)
	{
		ConfigurableWebBindingInitializer bean = super.getConfigurableWebBindingInitializer(mvcConversionService,
				mvcValidator);

		// XXX 父类方法不会注册应用自定义的FormattingConversionService，所以这里重新设置

		bean.setConversionService(this.coreConfig.conversionService());

		return bean;
	}
}
