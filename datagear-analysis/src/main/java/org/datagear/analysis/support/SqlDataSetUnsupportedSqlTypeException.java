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

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType)
	{
		super();
		this.sqlType = sqlType;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String message)
	{
		super(message);
		this.sqlType = sqlType;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, Throwable cause)
	{
		super(cause);
		this.sqlType = sqlType;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String message, Throwable cause)
	{
		super(message, cause);
		this.sqlType = sqlType;
	}

	public SqlType getSqlType()
	{
		return sqlType;
	}

	protected void setSqlType(SqlType sqlType)
	{
		this.sqlType = sqlType;
	}
}
