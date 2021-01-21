/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
