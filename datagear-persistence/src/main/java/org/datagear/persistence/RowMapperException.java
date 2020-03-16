/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

/**
 * {@linkplain RowMapper}异常。
 * 
 * @author datagear@163.com
 *
 */
public class RowMapperException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public RowMapperException()
	{
		super();
	}

	public RowMapperException(String message)
	{
		super(message);
	}

	public RowMapperException(Throwable cause)
	{
		super(cause);
	}

	public RowMapperException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RowMapperException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
