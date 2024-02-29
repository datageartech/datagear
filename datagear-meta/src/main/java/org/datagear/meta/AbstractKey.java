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
import java.util.Arrays;

import org.datagear.util.StringUtil;

/**
 * 表键。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractKey implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 列名 */
	private String[] columnNames;

	/** 键名 */
	private String keyName;

	public AbstractKey()
	{
		super();
	}

	public AbstractKey(String[] columnNames)
	{
		super();
		this.columnNames = columnNames;
	}

	public String[] getColumnNames()
	{
		return columnNames;
	}

	public void setColumnNames(String[] columnNames)
	{
		this.columnNames = columnNames;
	}

	public boolean hasKeyName()
	{
		return (this.keyName != null && !this.keyName.isEmpty());
	}

	public String getKeyName()
	{
		return keyName;
	}

	public void setKeyName(String keyName)
	{
		this.keyName = keyName;
	}

	/**
	 * 是否包含指定列名。
	 * 
	 * @param columnName
	 * @return
	 */
	public boolean containsColumnName(String columnName)
	{
		if (this.columnNames == null)
			return false;

		for (String cn : this.columnNames)
		{
			if (StringUtil.isEquals(cn, columnName))
				return true;
		}

		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [columnNames=" + Arrays.toString(columnNames) + ", keyName=" + keyName
				+ "]";
	}
}
