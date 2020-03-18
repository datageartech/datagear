/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

/**
 * {@linkplain SqlParamValueMapper}异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValueMapperException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public SqlParamValueMapperException()
	{
		super();
	}

	public SqlParamValueMapperException(String message)
	{
		super(message);
	}

	public SqlParamValueMapperException(Throwable cause)
	{
		super(cause);
	}

	public SqlParamValueMapperException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
