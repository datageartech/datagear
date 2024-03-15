/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.webapp;

import org.datagear.web.util.ApplicationBanner;
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
		return application.sources(DataGearApplication.class).banner(new ApplicationBanner());
	}
}
