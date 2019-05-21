/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据导入异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataImportException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DataImportException()
	{
		super();
	}

	public DataImportException(String message)
	{
		super(message);
	}

	public DataImportException(Throwable cause)
	{
		super(cause);
	}

	public DataImportException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
