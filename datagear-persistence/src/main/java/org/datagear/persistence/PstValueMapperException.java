/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

/**
 * {@linkplain PstValueMapper}异常。
 * 
 * @author datagear@163.com
 *
 */
public class PstValueMapperException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public PstValueMapperException()
	{
		super();
	}

	public PstValueMapperException(String message)
	{
		super(message);
	}

	public PstValueMapperException(Throwable cause)
	{
		super(cause);
	}

	public PstValueMapperException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
