/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 数据交换不支持异常。
 * <p>
 * {@linkplain GenericDataExchangeService}在不支持数据交换时将抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedExchangeException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedExchangeException()
	{
		super();
	}

	public UnsupportedExchangeException(String message)
	{
		super(message);
	}

	public UnsupportedExchangeException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedExchangeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
