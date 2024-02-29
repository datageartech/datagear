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

package org.datagear.persistence.support;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.RowMapperException;

/**
 * 不支持的列值映射异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedColumnValueMapperException extends RowMapperException
{
	private static final long serialVersionUID = 1L;

	private Table table;

	private Column column;

	private int sqlType;

	public UnsupportedColumnValueMapperException()
	{
		super();
	}

	public UnsupportedColumnValueMapperException(Table table, Column column, int sqlType)
	{
		super();
		this.table = table;
		this.column = column;
		this.sqlType = sqlType;
	}

	public UnsupportedColumnValueMapperException(Table table, Column column, int sqlType, Throwable cause)
	{
		super(cause);
		this.table = table;
		this.column = column;
		this.sqlType = sqlType;
	}

	public Table getTable()
	{
		return table;
	}

	protected void setTable(Table table)
	{
		this.table = table;
	}

	public Column getColumn()
	{
		return column;
	}

	protected void setColumn(Column column)
	{
		this.column = column;
	}

	public int getSqlType()
	{
		return sqlType;
	}

	protected void setSqlType(int sqlType)
	{
		this.sqlType = sqlType;
	}
}
