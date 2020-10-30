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
@PropertySource(value = {
		// 基础配置
		"classpath:org/datagear/web/datagear.properties",
		// 预留扩展配置，Web应用内的自定义配置文件
		"/WEB-INF/datagear.properties",
		// 预留扩展配置，Web应用内的自定义配置文件
		"/WEB-INF/config/datagear.properties",
		// 预留扩展配置，程序运行目录的自定义配置文件
		"file:datagear.properties",
		// 预留扩展配置，程序运行目录的自定义配置文件
		"file:config/datagear.properties",
		// 预留扩展配置，应用数据目录的自定义配置文件
		"file:${user.home}/.datagear/datagear.properties",
		// 版本号配置
		"classpath:org/datagear/web/datagear-version.properties" },
		// 上面的扩展配置都是可选的，所以这里要设为true
		ignoreResourceNotFound = true, encoding = "UTF-8")
public class PropertiesConfiguration
{
	public PropertiesConfiguration()
	{
	}
}
