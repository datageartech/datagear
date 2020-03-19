/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
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
