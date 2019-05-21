/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 导出不支持异常。
 * <p>
 * {@linkplain GenericDataExporter}在不支持导出时将抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedExportException extends DataImportException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedExportException()
	{
		super();
	}

	public UnsupportedExportException(String message)
	{
		super(message);
	}

	public UnsupportedExportException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedExportException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
