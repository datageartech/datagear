/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
