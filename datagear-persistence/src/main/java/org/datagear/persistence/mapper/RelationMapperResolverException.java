/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

import org.datagear.persistence.PersistenceException;

/**
 * {@linkplain RelationMapperResolver}异常。
 * 
 * @author datagear@163.com
 *
 */
public class RelationMapperResolverException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public RelationMapperResolverException()
	{
		super();
	}

	public RelationMapperResolverException(String message)
	{
		super(message);
	}

	public RelationMapperResolverException(Throwable cause)
	{
		super(cause);
	}

	public RelationMapperResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
