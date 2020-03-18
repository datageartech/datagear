/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.util.SqlParamValue;

/**
 * 不支持{@linkplain SqlParamValue}映射异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedSqlParamValueMapperException extends SqlParamValueMapperException
{
	private static final long serialVersionUID = 1L;

	private Table table;

	private Column column;

	private transient Object value;

	public UnsupportedSqlParamValueMapperException()
	{
		super();
	}

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value)
	{
		super();
		this.table = table;
		this.column = column;
		this.value = value;
	}

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value, Throwable cause)
	{
		super(cause);
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
