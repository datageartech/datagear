/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * 数据集源解析异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetSourceParseException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public DataSetSourceParseException()
	{
		super();
	}

	public DataSetSourceParseException(String message)
	{
		super(message);
	}

	public DataSetSourceParseException(Throwable cause)
	{
		super(cause);
	}

	public DataSetSourceParseException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
