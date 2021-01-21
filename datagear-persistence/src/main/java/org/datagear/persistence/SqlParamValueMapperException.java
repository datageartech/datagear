/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
