/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

/**
 * 图表结果错误信息。
 * 
 * @author datagear@163.com
 *
 */
public class ChartResultError
{
	private Throwable throwable;

	public ChartResultError()
	{
		super();
	}

	public ChartResultError(Throwable throwable)
	{
		super();
		this.throwable = throwable;
	}

	public Throwable getThrowable()
	{
		return throwable;
	}

	public void setThrowable(Throwable throwable)
	{
		this.throwable = throwable;
	}
}
