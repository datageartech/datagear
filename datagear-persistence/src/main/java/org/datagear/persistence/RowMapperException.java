/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence;

/**
 * {@linkplain RowMapper}异常。
 * 
 * @author datagear@163.com
 *
 */
public class RowMapperException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public RowMapperException()
	{
		super();
	}

	public RowMapperException(String message)
	{
		super(message);
	}

	public RowMapperException(Throwable cause)
	{
		super(cause);
	}

	public RowMapperException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
