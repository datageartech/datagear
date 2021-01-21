/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 不支持指定SQL数据类型的值。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedSqlValueException extends Exception
{
	private static final long serialVersionUID = 1L;

	public UnsupportedSqlValueException()
	{
		super();
	}

	public UnsupportedSqlValueException(int sqlType, Object sqlValue)
	{
		super("Value [" + sqlValue + "] for sql type [" + sqlType + "] is not supported");
	}

	public UnsupportedSqlValueException(String message)
	{
		super(message);
	}

	public UnsupportedSqlValueException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedSqlValueException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
