/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.meta.resolver;

/**
 * 数据库元信息解析异常。
 * 
 * @author datagear@163.com
 *
 */
public class DBMetaResolverException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DBMetaResolverException()
	{
		super();
	}

	public DBMetaResolverException(String message)
	{
		super(message);
	}

	public DBMetaResolverException(Throwable cause)
	{
		super(cause);
	}

	public DBMetaResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
