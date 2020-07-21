/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.datagear.web.freemarker.CustomFreeMarkerView;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.util.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.datagear.web.util.EnumThemeChangeInterceptor;
import org.datagear.web.util.SubContextPathRequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web配置。
 * <p>
 * 依赖配置：{@linkplain PropertiesConfiguration}、{@linkplain DataSourceConfiguration}、{@linkplain CoreConfiguration}。
 * </p>
 * <p>
 * 注：依赖配置需要手动加载。
 * </p>
 * 
 * @author datagear@163.com
 */
@Configuration
@ComponentScan("org.datagear.web.controller")
public class WebConfiguration
{
	@Autowired
	private CoreConfiguration coreConfiguration;

	@Autowired
	private Environment environment;

	public WebConfiguration()
	{
		super();
	}

	public WebConfiguration(CoreConfiguration coreConfiguration, Environment environment)
	{
		super();
		this.coreConfiguration = coreConfiguration;
		this.environment = environment;
	}

	public CoreConfiguration getCoreConfiguration()
	{
		return coreConfiguration;
	}

	public void setCoreConfiguration(CoreConfiguration coreConfiguration)
	{
		this.coreConfiguration = coreConfiguration;
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Bean
	public ObjectMapper objectMapper()
	{
		return this.coreConfiguration.objectMapperFactory().getObjectMapper();
	}

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter()
	{
		MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter(this.objectMapper());
		return bean;
	}

	@Bean
	public EnumThemeChangeInterceptor themeChangeInterceptor()
	{
		List<String> themes = new ArrayList<>();
		themes.add("lightness");
		themes.add("dark");
		themes.add("green");

		EnumThemeChangeInterceptor bean = new EnumThemeChangeInterceptor(themes);

		return bean;
	}

	@Bean
	public SubContextPathRequestMappingHandlerMapping handlerMapping()
	{
		SubContextPathRequestMappingHandlerMapping bean = new SubContextPathRequestMappingHandlerMapping();
		bean.setAlwaysUseFullPath(true);
		bean.setSubContextPath(this.environment.getProperty("subContextPath"));
		bean.setInterceptors(this.themeChangeInterceptor());

		return bean;
	}

	@Bean
	public RequestMappingHandlerAdapter handlerAdapter()
	{
		RequestMappingHandlerAdapter bean = new RequestMappingHandlerAdapter();

		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(this.mappingJackson2HttpMessageConverter());

		ConfigurableWebBindingInitializer webBindingInitializer = new ConfigurableWebBindingInitializer();
		webBindingInitializer.setConversionService(this.coreConfiguration.conversionService().getObject());

		bean.setMessageConverters(messageConverters);
		bean.setWebBindingInitializer(webBindingInitializer);

		return bean;
	}

	@Bean
	public WriteJsonTemplateDirectiveModel writeJsonTemplateDirectiveModel()
	{
		WriteJsonTemplateDirectiveModel bean = new WriteJsonTemplateDirectiveModel(
				this.coreConfiguration.objectMapperFactory());
		return bean;
	}

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer()
	{
		FreeMarkerConfigurer bean = new FreeMarkerConfigurer();

		Properties settings = new Properties();
		settings.setProperty("datetime_format", "yyyy-MM-dd HH:mm:ss");
		settings.setProperty("date_format", "yyyy-MM-dd");
		settings.setProperty("number_format", "#.##");

		Map<String, Object> variables = new HashMap<>();
		variables.put("writeJson", this.writeJsonTemplateDirectiveModel());

		bean.setTemplateLoaderPath("classpath:org/datagear/web/webapp/view/freemarker/");
		bean.setDefaultEncoding("UTF-8");
		bean.setFreemarkerSettings(settings);
		bean.setFreemarkerVariables(variables);

		return bean;
	}

	@Bean
	public FreeMarkerViewResolver freeMarkerViewResolver()
	{
		FreeMarkerViewResolver bean = new FreeMarkerViewResolver();
		bean.setViewClass(CustomFreeMarkerView.class);
		bean.setContentType("text/html;charset=UTF-8");
		bean.setExposeRequestAttributes(true);
		bean.setAllowRequestOverride(true);
		bean.setCache(true);
		bean.setPrefix("");
		bean.setSuffix(".ftl");

		return bean;
	}

	@Bean
	public DeliverContentTypeExceptionHandlerExceptionResolver exceptionResolver()
	{
		DeliverContentTypeExceptionHandlerExceptionResolver bean = new DeliverContentTypeExceptionHandlerExceptionResolver();
		return bean;
	}

	@Bean
	public ResourceBundleThemeSource themeSource()
	{
		ResourceBundleThemeSource bean = new ResourceBundleThemeSource();
		bean.setBasenamePrefix("org.datagear.web.theme.");

		return bean;
	}

	@Bean
	public CookieThemeResolver themeResolver()
	{
		CookieThemeResolver bean = new CookieThemeResolver();
		bean.setDefaultThemeName(this.themeChangeInterceptor().getThemes().get(0));
		bean.setCookieName("THEME");
		bean.setCookieMaxAge(60 * 60 * 24 * 365);

		return bean;
	}

	@Bean
	public AcceptHeaderLocaleResolver localeResolver()
	{
		AcceptHeaderLocaleResolver bean = new AcceptHeaderLocaleResolver();
		return bean;
	}

	@Bean
	public MultipartResolver multipartResolver()
	{
		CommonsMultipartResolver bean = new CommonsMultipartResolver();
		return bean;
	}
}
