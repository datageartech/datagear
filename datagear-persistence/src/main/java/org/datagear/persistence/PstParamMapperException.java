/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

/**
 * {@linkplain PstParamMapper}异常。
 * 
 * @author datagear@163.com
 *
 */
public class PstParamMapperException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public PstParamMapperException()
	{
		super();
	}

	public PstParamMapperException(String message)
	{
		super(message);
	}

	public PstParamMapperException(Throwable cause)
	{
		super(cause);
	}

	public PstParamMapperException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
