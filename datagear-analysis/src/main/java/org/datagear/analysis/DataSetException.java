/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

/**
 * 数据集异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DataSetException()
	{
		super();
	}

	public DataSetException(String message)
	{
		super(message);
	}

	public DataSetException(Throwable cause)
	{
		super(cause);
	}

	public DataSetException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
