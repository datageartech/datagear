/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence;

/**
 * 持久化异常。
 * 
 * @author datagear@163.com
 *
 */
public class PersistenceException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public PersistenceException()
	{
		super();
	}

	public PersistenceException(String message)
	{
		super(message);
	}

	public PersistenceException(Throwable cause)
	{
		super(cause);
	}

	public PersistenceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
