/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
