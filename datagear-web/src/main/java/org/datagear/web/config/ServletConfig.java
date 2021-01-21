/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import java.util.Arrays;

import org.datagear.util.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
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

	@Autowired
	public ServletConfig(CoreConfig coreConfig)
	{
		super();
		this.coreConfig = coreConfig;
	}

	public CoreConfig getCoreConfig()
	{
		return coreConfig;
	}

	public void setCoreConfig(CoreConfig coreConfig)
	{
		this.coreConfig = coreConfig;
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
}
