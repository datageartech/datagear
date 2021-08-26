/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 属性配置。
 * 
 * @author datagear@163.com
 */
@Configuration
@PropertySource(value = "classpath:org/datagear/web/application.properties", encoding = "UTF-8")
public class ApplicationPropertiesConfig
{
	public ApplicationPropertiesConfig()
	{
	}

	@Bean
	public ApplicationProperties applicationProperties()
	{
		ApplicationPropertiesImpl bean = new ApplicationPropertiesImpl();
		return bean;
	}

	protected static class ApplicationPropertiesImpl extends ApplicationProperties
	{
		private static final long serialVersionUID = 1L;

		public ApplicationPropertiesImpl()
		{
			super();
		}
	}
}
