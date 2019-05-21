/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据导出异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataExportException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DataExportException()
	{
		super();
	}

	public DataExportException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DataExportException(String message)
	{
		super(message);
	}

	public DataExportException(Throwable cause)
	{
		super(cause);
	}
}
