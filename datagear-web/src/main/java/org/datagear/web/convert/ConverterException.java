/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.convert;

/**
 * 转换异常。
 * 
 * @author datagear@163.com
 *
 */
public class ConverterException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ConverterException()
	{
		super();
	}

	public ConverterException(String message)
	{
		super(message);
	}

	public ConverterException(Throwable cause)
	{
		super(cause);
	}

	public ConverterException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
