/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 不支持指定SQL类型异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedSqlTypeException extends Exception
{
	private static final long serialVersionUID = 1L;

	public UnsupportedSqlTypeException()
	{
		super();
	}

	public UnsupportedSqlTypeException(int sqlType)
	{
		super("Sql type [" + sqlType + "] is not supported");
	}

	public UnsupportedSqlTypeException(String message)
	{
		super(message);
	}

	public UnsupportedSqlTypeException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedSqlTypeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
