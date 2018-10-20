/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.webembd;

/**
 * {@linkplain AppConfigFactory}异常。
 * 
 * @author datagear@163.com
 *
 */
public class AppConfigFactoryException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public AppConfigFactoryException()
	{
		super();
	}

	public AppConfigFactoryException(String message)
	{
		super(message);
	}

	public AppConfigFactoryException(Throwable cause)
	{
		super(cause);
	}

	public AppConfigFactoryException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
