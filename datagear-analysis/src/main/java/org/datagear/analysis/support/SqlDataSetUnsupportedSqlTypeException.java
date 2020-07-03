/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;
import org.datagear.util.SqlType;

/**
 * 不支持指定SQL类型异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetUnsupportedSqlTypeException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private SqlType sqlType;

	private String columnName;

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType)
	{
		super();
		this.sqlType = sqlType;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String columnName)
	{
		super();
		this.sqlType = sqlType;
		this.columnName = columnName;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String columnName, String message)
	{
		super(message);
		this.sqlType = sqlType;
		this.columnName = columnName;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String columnName, Throwable cause)
	{
		super(cause);
		this.sqlType = sqlType;
		this.columnName = columnName;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String columnName, String message, Throwable cause)
	{
		super(message, cause);
		this.sqlType = sqlType;
		this.columnName = columnName;
	}

	public SqlType getSqlType()
	{
		return sqlType;
	}

	protected void setSqlType(SqlType sqlType)
	{
		this.sqlType = sqlType;
	}

	public boolean hasColumnName()
	{
		return (this.columnName != null && !this.columnName.isEmpty());
	}

	public String getColumnName()
	{
		return columnName;
	}

	protected void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}
}
