/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

/**
 * {@linkplain DbModelFactory}异常。
 * 
 * @author datagear@163.com
 *
 */
public class DbModelFactoryException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DbModelFactoryException()
	{
		super();
	}

	public DbModelFactoryException(String message)
	{
		super(message);
	}

	public DbModelFactoryException(Throwable cause)
	{
		super(cause);
	}

	public DbModelFactoryException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
