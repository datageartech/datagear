/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence;

/**
 * 数据库方言异常。
 * 
 * @author datagear@163.com
 *
 */
public class DialectException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public DialectException()
	{
		super();
	}

	public DialectException(String message)
	{
		super(message);
	}

	public DialectException(Throwable cause)
	{
		super(cause);
	}

	public DialectException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
