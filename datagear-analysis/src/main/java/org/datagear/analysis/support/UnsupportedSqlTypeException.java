/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * 不支持指定SQL类型异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedSqlTypeException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private int sqlType;

	public UnsupportedSqlTypeException(int sqlType)
	{
		super();
		this.sqlType = sqlType;
	}

	public UnsupportedSqlTypeException(int sqlType, String message)
	{
		super(message);
		this.sqlType = sqlType;
	}

	public UnsupportedSqlTypeException(int sqlType, Throwable cause)
	{
		super(cause);
		this.sqlType = sqlType;
	}

	public UnsupportedSqlTypeException(int sqlType, String message, Throwable cause)
	{
		super(message, cause);
		this.sqlType = sqlType;
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
