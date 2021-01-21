/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
