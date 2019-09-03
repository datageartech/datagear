/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import javax.xml.validation.Schema;

import org.datagear.management.service.PermissionDeniedException;

/**
 * 保存指定URL的{@linkplain Schema}无权限异常。
 * 
 * @author datagear@163.com
 *
 */
public class SaveSchemaUrlPermissionDeniedException extends PermissionDeniedException
{
	private static final long serialVersionUID = 1L;

	public SaveSchemaUrlPermissionDeniedException()
	{
		super();
	}

	public SaveSchemaUrlPermissionDeniedException(String message)
	{
		super(message);
	}

	public SaveSchemaUrlPermissionDeniedException(Throwable cause)
	{
		super(cause);
	}

	public SaveSchemaUrlPermissionDeniedException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
