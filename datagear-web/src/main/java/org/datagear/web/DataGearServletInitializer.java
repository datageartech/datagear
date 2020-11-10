/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web;

import org.datagear.web.DataGearApplication.DataGearBanner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 应用入口。
 * 
 * @author datagear@163.com
 *
 */
public class DataGearServletInitializer extends SpringBootServletInitializer
{
	/** 系统作为war包部署至Servlet容器时加载配置项标识，参考：application-war.properties */
	public static final String PROFILE_WAR = "war";

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		application.profiles(PROFILE_WAR);
		return application.sources(DataGearApplication.class).banner(new DataGearBanner());
	}
}
