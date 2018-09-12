/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

/**
 * 记录未找到或者没有操作权限异常。
 * 
 * @author datagear@163.com
 *
 */
public class RecordNotFoundOrPermissionDeniedException extends ControllerException
{
	private static final long serialVersionUID = 1L;

	public RecordNotFoundOrPermissionDeniedException()
	{
		super();
	}

	public RecordNotFoundOrPermissionDeniedException(String message)
	{
		super(message);
	}

	public RecordNotFoundOrPermissionDeniedException(Throwable cause)
	{
		super(cause);
	}

	public RecordNotFoundOrPermissionDeniedException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
