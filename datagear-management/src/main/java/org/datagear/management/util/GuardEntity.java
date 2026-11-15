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

package org.datagear.management.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 数据源防护对象。
 * 
 * @author datagear@163.com
 *
 */
public class GuardEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** URL */
	private String url;

	/** 用户名 */
	private String user = "";

	/** 属性 */
	private List<GuardProperty> properties = Collections.emptyList();

	public GuardEntity()
	{
		super();
	}

	public GuardEntity(String url)
	{
		super();
		this.url = url;
	}

	public GuardEntity(String url, String user)
	{
		super();
		this.url = url;
		this.user = user;
	}

	public GuardEntity(String url, String user, List<GuardProperty> properties)
	{
		super();
		this.url = url;
		this.user = user;
		this.properties = properties;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public List<GuardProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<GuardProperty> properties)
	{
		this.properties = properties;
	}
}