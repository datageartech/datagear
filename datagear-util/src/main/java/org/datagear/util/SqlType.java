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

package org.datagear.util;

/**
 * SQL类型。
 * 
 * @author datagear@163.com
 *
 */
public class SqlType
{
	/** JDBC类型 */
	private int type;

	/** SQL类型名称 */
	private String typeName;

	public SqlType()
	{
		super();
	}

	public SqlType(int type)
	{
		super();
		this.type = type;
	}

	public SqlType(int type, String typeName)
	{
		super();
		this.type = type;
		this.typeName = typeName;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public boolean hasTypeName()
	{
		return (this.typeName != null && !this.typeName.isEmpty());
	}

	public String getTypeName()
	{
		return typeName;
	}

	public void setTypeName(String typeName)
	{
		this.typeName = typeName;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [type=" + type + ", typeName=" + typeName + "]";
	}
}
