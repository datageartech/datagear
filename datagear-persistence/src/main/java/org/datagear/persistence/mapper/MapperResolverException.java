/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

import org.datagear.persistence.PersistenceException;

/**
 * {@linkplain MapperResolver}异常。
 * 
 * @author datagear@163.com
 *
 */
public class MapperResolverException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public MapperResolverException()
	{
		super();
	}

	public MapperResolverException(String message)
	{
		super(message);
	}

	public MapperResolverException(Throwable cause)
	{
		super(cause);
	}

	public MapperResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
