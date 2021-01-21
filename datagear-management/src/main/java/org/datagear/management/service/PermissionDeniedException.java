/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

/**
 * 无权执行异常。
 * 
 * @author datagear@163.com
 *
 */
public class PermissionDeniedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public PermissionDeniedException()
	{
		super("Permission denied");
	}

	public PermissionDeniedException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public PermissionDeniedException(String message)
	{
		super(message);
	}

	public PermissionDeniedException(Throwable cause)
	{
		super(cause);
	}
}
