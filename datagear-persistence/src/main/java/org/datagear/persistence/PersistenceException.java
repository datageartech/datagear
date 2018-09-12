/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence;

/**
 * 持久化异常。
 * <p>
 * 此异常作为整个persistence模块的顶级异常类，所有persistence模块的异常类都继承自此类。
 * </p>
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
