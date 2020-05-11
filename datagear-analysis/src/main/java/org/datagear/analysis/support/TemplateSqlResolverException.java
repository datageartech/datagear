/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * {@linkplain TemplateSqlResolver}异常。
 * 
 * @author datagear@163.com
 *
 */
public class TemplateSqlResolverException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public TemplateSqlResolverException()
	{
		super();
	}

	public TemplateSqlResolverException(String message)
	{
		super(message);
	}

	public TemplateSqlResolverException(Throwable cause)
	{
		super(cause);
	}

	public TemplateSqlResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
