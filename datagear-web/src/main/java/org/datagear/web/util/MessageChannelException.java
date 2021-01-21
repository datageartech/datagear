/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

/**
 * 消息通道异常。
 * 
 * @author datagear@163.com
 *
 */
public class MessageChannelException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public MessageChannelException()
	{
		super();
	}

	public MessageChannelException(String message)
	{
		super(message);
	}

	public MessageChannelException(Throwable cause)
	{
		super(cause);
	}

	public MessageChannelException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public MessageChannelException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
