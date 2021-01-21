/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.connection;

/**
 * {@linkplain ConnectionSource}异常。
 * 
 * @author datagear@163.com
 *
 */
public class ConnectionSourceException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ConnectionSourceException()
	{
		super();
	}

	public ConnectionSourceException(String message)
	{
		super(message);
	}

	public ConnectionSourceException(Throwable cause)
	{
		super(cause);
	}

	public ConnectionSourceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
