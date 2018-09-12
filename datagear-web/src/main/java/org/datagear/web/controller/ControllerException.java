/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

/**
 * 控制器层异常。
 * 
 * @author datagear@163.com
 *
 */
public class ControllerException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ControllerException()
	{
		super();
	}

	public ControllerException(String message)
	{
		super(message);
	}

	public ControllerException(Throwable cause)
	{
		super(cause);
	}

	public ControllerException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
