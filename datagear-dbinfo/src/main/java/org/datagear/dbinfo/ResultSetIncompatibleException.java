/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

/**
 * 结果集不兼容异常。
 * 
 * @author datagear@163.com
 *
 */
public class ResultSetIncompatibleException extends DatabaseInfoResolverException
{
	private static final long serialVersionUID = 1L;

	public ResultSetIncompatibleException()
	{
		super();
	}

	public ResultSetIncompatibleException(String message)
	{
		super(message);
	}

	public ResultSetIncompatibleException(Throwable cause)
	{
		super(cause);
	}

	public ResultSetIncompatibleException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
