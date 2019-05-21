/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 导入不支持异常。
 * <p>
 * {@linkplain GenericDataImporter}在不支持导入时将抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedImportException extends DataImportException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedImportException()
	{
		super();
	}

	public UnsupportedImportException(String message)
	{
		super(message);
	}

	public UnsupportedImportException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedImportException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
