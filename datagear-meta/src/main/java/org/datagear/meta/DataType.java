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

package org.datagear.meta;

import java.io.Serializable;

/**
 * 数据类型。
 * 
 * @author datagear@163.com
 *
 */
public class DataType implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 类型名 */
	private String name;

	/** SQL类型，对应java.sql.Types中的值 */
	private int type;

	/** 可搜索类型 */
	private SearchableType searchableType;

	public DataType(String name, int type)
	{
		super();
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public boolean hasSearchableType()
	{
		return (this.searchableType != null);
	}

	public SearchableType getSearchableType()
	{
		return searchableType;
	}

	public void setSearchableType(SearchableType searchableType)
	{
		this.searchableType = searchableType;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + ", searchableType=" + searchableType
				+ "]";
	}
}
