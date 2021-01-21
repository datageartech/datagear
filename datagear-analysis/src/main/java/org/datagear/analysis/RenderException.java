/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * 渲染异常。
 * 
 * @author datagear@163.com
 *
 */
public class RenderException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public RenderException()
	{
		super();
	}

	public RenderException(String message)
	{
		super(message);
	}

	public RenderException(Throwable cause)
	{
		super(cause);
	}

	public RenderException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
