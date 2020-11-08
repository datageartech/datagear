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
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		return application.sources(DataGearApplication.class).banner(new DataGearBanner());
	}
}
