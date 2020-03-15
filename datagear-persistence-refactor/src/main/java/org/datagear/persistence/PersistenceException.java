/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

/**
 * 持久化异常。
 * 
 * @author datagear@163.com
 *
 */
public class PersistenceException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public PersistenceException()
	{
		super();
	}

	public PersistenceException(String message)
	{
		super(message);
	}

	public PersistenceException(Throwable cause)
	{
		super(cause);
	}

	public PersistenceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
