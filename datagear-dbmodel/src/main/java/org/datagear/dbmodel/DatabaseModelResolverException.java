/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbmodel;

/**
 * {@link DatabaseModelResolver}异常。
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseModelResolverException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DatabaseModelResolverException()
	{
		super();
	}

	public DatabaseModelResolverException(String message)
	{
		super(message);
	}

	public DatabaseModelResolverException(Throwable cause)
	{
		super(cause);
	}

	public DatabaseModelResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
