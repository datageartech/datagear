/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 属性配置。
 * 
 * @author datagear@163.com
 */
@Configuration
@PropertySource(value = "classpath:org/datagear/web/application.properties", encoding = "UTF-8")
public class PropertiesConfig
{
	public PropertiesConfig()
	{
	}
}
