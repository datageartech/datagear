/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 数据交换异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataExchangeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DataExchangeException()
	{
		super();
	}

	public DataExchangeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DataExchangeException(String message)
	{
		super(message);
	}

	public DataExchangeException(Throwable cause)
	{
		super(cause);
	}
}
