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

import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.DtbsSourceProperty;

/**
 * 数据源防护对象。
 * 
 * @author datagear@163.com
 *
 */
public class GuardEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 数据源连接URL */
	private String url;

	/** 数据源用户名 */
	private String user = "";

	/** 数据源连接属性 */
	private List<DtbsSourceProperty> properties = Collections.emptyList();

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

	public GuardEntity(String url, String user, List<DtbsSourceProperty> properties)
	{
		super();
		this.url = url;
		this.user = user;
		this.properties = properties;
	}

	public GuardEntity(DtbsSource schema)
	{
		this(schema.getUrl(), schema.getUser(), schema.getProperties());
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

	public List<DtbsSourceProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<DtbsSourceProperty> properties)
	{
		this.properties = properties;
	}
}