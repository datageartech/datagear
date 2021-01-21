/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

/**
 * {@linkplain HtmlChartPlugin}加载异常。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginLoadException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public HtmlChartPluginLoadException()
	{
		super();
	}

	public HtmlChartPluginLoadException(String message)
	{
		super(message);
	}

	public HtmlChartPluginLoadException(Throwable cause)
	{
		super(cause);
	}

	public HtmlChartPluginLoadException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
