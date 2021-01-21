/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

/**
 * 记录未找到异常。
 * 
 * @author datagear@163.com
 *
 */
public class RecordNotFoundException extends ControllerException
{
	private static final long serialVersionUID = 1L;

	public RecordNotFoundException()
	{
		super();
	}

	public RecordNotFoundException(String message)
	{
		super(message);
	}

	public RecordNotFoundException(Throwable cause)
	{
		super(cause);
	}

	public RecordNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
