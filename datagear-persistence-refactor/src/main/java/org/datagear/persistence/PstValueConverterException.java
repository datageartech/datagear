/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

/**
 * {@linkplain PstValueConverter}异常。
 * 
 * @author datagear@163.com
 *
 */
public class PstValueConverterException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public PstValueConverterException()
	{
		super();
	}

	public PstValueConverterException(String message)
	{
		super(message);
	}

	public PstValueConverterException(Throwable cause)
	{
		super(cause);
	}

	public PstValueConverterException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public PstValueConverterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
