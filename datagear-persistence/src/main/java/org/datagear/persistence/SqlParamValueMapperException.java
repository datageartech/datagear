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

package org.datagear.persistence;

import org.datagear.meta.Column;
import org.datagear.meta.Table;

/**
 * {@linkplain SqlParamValueMapper}异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValueMapperException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	private transient Table table;

	private transient Column column;

	private transient Object value;

	public SqlParamValueMapperException(Table table, Column column, Object value)
	{
		super();
		this.table = table;
		this.column = column;
		this.value = value;
	}

	public SqlParamValueMapperException(Table table, Column column, Object value, String message)
	{
		super(message);
		this.table = table;
		this.column = column;
		this.value = value;
	}

	public SqlParamValueMapperException(Table table, Column column, Object value, Throwable cause)
	{
		super(cause);
		this.table = table;
		this.column = column;
		this.value = value;
	}

	public SqlParamValueMapperException(Table table, Column column, Object value, String message, Throwable cause)
	{
		super(message, cause);
		this.table = table;
		this.column = column;
		this.value = value;
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

	public Object getValue()
	{
		return value;
	}

	protected void setValue(Object value)
	{
		this.value = value;
	}
}
