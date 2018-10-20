/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

/**
 * 非法{@linkplain PropertyPath}异常。
 * 
 * @author datagear@163.com
 *
 */
public class IllegalPropertyPathException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public IllegalPropertyPathException()
	{
		super();
	}

	public IllegalPropertyPathException(String message)
	{
		super(message);
	}

	public IllegalPropertyPathException(Throwable cause)
	{
		super(cause);
	}

	public IllegalPropertyPathException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
