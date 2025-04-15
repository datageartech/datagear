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

package org.datagear.util;

/**
 * SQL参数值。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValue
{
	/** 参数值 */
	private Object value;

	/** JDBC类型 */
	private int type;

	public SqlParamValue()
	{
		super();
	}

	public SqlParamValue(Object value, int type)
	{
		super();
		this.value = value;
		this.type = type;
	}

	public boolean hasValue()
	{
		return (this.value != null);
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [value=" + value + ", type=" + type + "]";
	}

	public static SqlParamValue valueOf(Object value, int type)
	{
		return new SqlParamValue(value, type);
	}
}
