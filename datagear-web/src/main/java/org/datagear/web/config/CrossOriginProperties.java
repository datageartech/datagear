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

package org.datagear.web.config;

import java.io.Serializable;
import java.util.List;

import org.springframework.web.cors.CorsConfiguration;

/**
 * 跨域请求配置项。
 * 
 * @author datagear@163.com
 *
 */
public class CrossOriginProperties implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<String> paths = null;

	private CorsConfiguration config = null;

	public CrossOriginProperties()
	{
		super();
	}

	public List<String> getPaths()
	{
		return paths;
	}

	public void setPaths(List<String> paths)
	{
		this.paths = paths;
	}

	public CorsConfiguration getConfig()
	{
		return config;
	}

	public void setConfig(CorsConfiguration config)
	{
		this.config = config;
	}
}
