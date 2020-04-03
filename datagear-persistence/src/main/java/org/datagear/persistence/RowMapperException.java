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
public class RowMapperException extends PersistenceException
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
}
