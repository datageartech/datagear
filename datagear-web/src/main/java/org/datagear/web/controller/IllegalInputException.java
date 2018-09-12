/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

/**
 * 输入非法异常。
 * 
 * @author datagear@163.com
 *
 */
public class IllegalInputException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public IllegalInputException()
	{
		super();
	}

	public IllegalInputException(String message)
	{
		super(message);
	}

	public IllegalInputException(Throwable cause)
	{
		super(cause);
	}

	public IllegalInputException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
