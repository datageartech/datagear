/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.persistence.PersistenceException;

/**
 * 不是唯一记录异常。
 * 
 * @author datagear@163.com
 *
 */
public class NotUniqueRecordException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public NotUniqueRecordException()
	{
		super();
	}

	public NotUniqueRecordException(String message)
	{
		super(message);
	}

	public NotUniqueRecordException(Throwable cause)
	{
		super(cause);
	}

	public NotUniqueRecordException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
