/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.util.dialect;

/**
 * @author datagear@163.com
 *
 */
public class MbSqlDialectException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public MbSqlDialectException()
	{
		super();
	}

	public MbSqlDialectException(String message)
	{
		super(message);
	}

	public MbSqlDialectException(Throwable cause)
	{
		super(cause);
	}

	public MbSqlDialectException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
