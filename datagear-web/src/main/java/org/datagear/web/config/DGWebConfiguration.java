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
//@Configuration(proxyBeanMethods = false)
//@EnableWebMvc
//@ComponentScan(basePackageClasses = MainController.class)
public class DGWebConfiguration extends WebMvcConfigurationSupport
{
	private DGCoreConfiguration dGCoreConfiguration;

	private Environment environment;

	@Autowired
	public DGWebConfiguration(DGCoreConfiguration dGCoreConfiguration, Environment environment)
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
	protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer(
			FormattingConversionService mvcConversionService, Validator mvcValidator)
	{
		ConfigurableWebBindingInitializer bean = super.getConfigurableWebBindingInitializer(mvcConversionService,
				mvcValidator);

		// XXX 父类方法不会注册应用自定义的FormattingConversionService，所以这里重新设置

		bean.setConversionService(this.dGCoreConfiguration.conversionService().getObject());

		return bean;
	}
}
