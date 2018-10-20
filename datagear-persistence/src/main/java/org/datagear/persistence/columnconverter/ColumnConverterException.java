/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.columnconverter;

import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.features.ColumnConverter;

/**
 * {@linkplain ColumnConverter}转换异常。
 * 
 * @author datagear@163.com
 *
 */
public class ColumnConverterException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public ColumnConverterException()
	{
		super();
	}

	public ColumnConverterException(String message)
	{
		super(message);
	}

	public ColumnConverterException(Throwable cause)
	{
		super(cause);
	}

	public ColumnConverterException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
