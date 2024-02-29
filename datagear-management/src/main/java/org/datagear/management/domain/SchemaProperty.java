/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.management.domain;

import java.io.Serializable;

/**
 * 数据库模式属性。
 * <p>
 * 数据库JDBC连接属性。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SchemaProperty implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/**属性名*/
	private String name;
	
	/**属性值*/
	private String value;

	public SchemaProperty()
	{
		super();
	}

	public SchemaProperty(String name, String value)
	{
		super();
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
