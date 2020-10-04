/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 安全配置。
 * <p>
 * 依赖配置：{@linkplain PropertiesConfiguration}、{@linkplain CoreConfiguration}。
 * </p>
 * <p>
 * 注：依赖配置需要手动加载。
 * </p>
 * 
 * @author datagear@163.com
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
	public SecurityConfiguration()
	{
	}
}
