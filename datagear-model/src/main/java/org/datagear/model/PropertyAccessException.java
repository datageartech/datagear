/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model;

/**
 * 属性访问异常。
 * <p>
 * {@linkplain Property#get(Object)}和{@linkplain Property#set(Object, Object)}
 * 会抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyAccessException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public PropertyAccessException()
	{
		super();
	}

	public PropertyAccessException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public PropertyAccessException(String message)
	{
		super(message);
	}

	public PropertyAccessException(Throwable cause)
	{
		super(cause);
	}
}
