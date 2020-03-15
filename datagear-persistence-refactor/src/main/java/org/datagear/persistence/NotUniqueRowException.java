/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

/**
 * 非唯一行异常。
 * 
 * @author datagear@163.com
 *
 */
public class NotUniqueRowException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public NotUniqueRowException()
	{
		super();
	}

	public NotUniqueRowException(String message)
	{
		super(message);
	}

	public NotUniqueRowException(Throwable cause)
	{
		super(cause);
	}

	public NotUniqueRowException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
