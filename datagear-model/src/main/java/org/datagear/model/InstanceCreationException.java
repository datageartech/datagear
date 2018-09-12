/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model;

/**
 * 创建实例异常。
 * <p>
 * {@linkplain Model#newInstance()}会抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class InstanceCreationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public InstanceCreationException()
	{
		super();
	}

	public InstanceCreationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InstanceCreationException(String message)
	{
		super(message);
	}

	public InstanceCreationException(Throwable cause)
	{
		super(cause);
	}
}
