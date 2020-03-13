/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta.resolver;

/**
 * 数据库元信息解析异常。
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseMetaResolverException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DatabaseMetaResolverException()
	{
		super();
	}

	public DatabaseMetaResolverException(String message)
	{
		super(message);
	}

	public DatabaseMetaResolverException(Throwable cause)
	{
		super(cause);
	}

	public DatabaseMetaResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DatabaseMetaResolverException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
