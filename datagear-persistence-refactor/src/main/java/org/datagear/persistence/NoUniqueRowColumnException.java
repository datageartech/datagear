/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

/**
 * 找不到能确定唯一行的列异常。
 * 
 * @author datagear@163.com
 *
 */
public class NoUniqueRowColumnException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public NoUniqueRowColumnException()
	{
		super();
	}

	public NoUniqueRowColumnException(String message)
	{
		super(message);
	}

	public NoUniqueRowColumnException(Throwable cause)
	{
		super(cause);
	}

	public NoUniqueRowColumnException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
