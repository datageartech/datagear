/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

/**
 * {@linkplain DriverEntityManager}异常。
 * 
 * @author datagear@163.com
 *
 */
public class DriverEntityManagerException extends ConnectionSourceException
{
	private static final long serialVersionUID = 1L;

	public DriverEntityManagerException()
	{
		super();
	}

	public DriverEntityManagerException(String message)
	{
		super(message);
	}

	public DriverEntityManagerException(Throwable cause)
	{
		super(cause);
	}

	public DriverEntityManagerException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
