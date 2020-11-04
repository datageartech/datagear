/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.cometd.server.CometDServlet;
import org.datagear.util.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.ServletContextAttributeExporter;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Servlet配置。
 * 
 * @author datagear@163.com
 */
@Configuration
public class ServletConfig
{
	private CoreConfig coreConfig;

	private Environment environment;

	@Autowired
	public ServletConfig(CoreConfig coreConfig, Environment environment)
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

	@Bean
	public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilterRegistrationBean()
	{
		CharacterEncodingFilter filter = new CharacterEncodingFilter(IOUtil.CHARSET_UTF_8, true);

		FilterRegistrationBean<CharacterEncodingFilter> bean = new FilterRegistrationBean<>();
		bean.setFilter(filter);
		bean.setUrlPatterns(Arrays.asList("/*"));
		bean.setName(CharacterEncodingFilter.class.getSimpleName());
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		bean.setAsyncSupported(true);

		return bean;
	}

	@Bean
	@DependsOn("bayeuxServerServletContextAttributeExporter")
	public ServletRegistrationBean<CometDServlet> cometdServletRegistrationBean()
	{
		CometDServlet cometdServlet = new CometDServlet();
		ServletRegistrationBean<CometDServlet> bean = new ServletRegistrationBean<>(cometdServlet, "/cometd/*");
		bean.setName(CometDServlet.class.getSimpleName());

		return bean;
	}

	@Bean("bayeuxServerServletContextAttributeExporter")
	public ServletContextAttributeExporter bayeuxServerServletContextAttributeExporter()
	{
		ServletContextAttributeExporter bean = new ServletContextAttributeExporter();

		Map<String, Object> attributes = new HashMap<>();
		attributes.put(org.cometd.bayeux.server.BayeuxServer.ATTRIBUTE, this.coreConfig.bayeuxServer());

		bean.setAttributes(attributes);

		return bean;
	}
}
