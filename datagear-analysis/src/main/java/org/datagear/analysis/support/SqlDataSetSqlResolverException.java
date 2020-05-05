/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * {@linkplain SqlDataSetSqlResolver}异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetSqlResolverException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public SqlDataSetSqlResolverException()
	{
		super();
	}

	public SqlDataSetSqlResolverException(String message)
	{
		super(message);
	}

	public SqlDataSetSqlResolverException(Throwable cause)
	{
		super(cause);
	}

	public SqlDataSetSqlResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
