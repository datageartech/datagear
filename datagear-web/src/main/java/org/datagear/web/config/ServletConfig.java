/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
