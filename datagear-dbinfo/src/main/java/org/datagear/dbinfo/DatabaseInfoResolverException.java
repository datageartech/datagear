/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

/**
 * {@linkplain DatabaseInfoResolver}异常。
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseInfoResolverException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DatabaseInfoResolverException()
	{
		super();
	}

	public DatabaseInfoResolverException(String message)
	{
		super(message);
	}

	public DatabaseInfoResolverException(Throwable cause)
	{
		super(cause);
	}

	public DatabaseInfoResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
