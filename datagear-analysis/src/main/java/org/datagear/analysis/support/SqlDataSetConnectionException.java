/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * @author datagear@163.com
 *
 */
public class SqlDataSetConnectionException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public SqlDataSetConnectionException(Throwable cause)
	{
		super(cause);
	}
}
