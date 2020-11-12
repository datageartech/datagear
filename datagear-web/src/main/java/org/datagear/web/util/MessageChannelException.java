/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
