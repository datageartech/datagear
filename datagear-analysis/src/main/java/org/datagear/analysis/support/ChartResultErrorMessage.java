/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import org.datagear.analysis.ChartResultError;

/**
 * 图表结果错误消息。
 * 
 * @author datagear@163.com
 *
 */
public class ChartResultErrorMessage
{
	/** 错误类型 */
	private String type = "";

	/** 错误消息 */
	private String message = "";

	public ChartResultErrorMessage()
	{
		super();
	}

	public ChartResultErrorMessage(String type, String message)
	{
		super();
		this.type = type;
		this.message = message;
	}

	public ChartResultErrorMessage(ChartResultError error)
	{
		this(error, false);
	}

	public ChartResultErrorMessage(ChartResultError error, boolean rootCauseMessage)
	{
		Throwable throwable = error.getThrowable();

		if (throwable != null)
		{
			this.type = throwable.getClass().getSimpleName();

			if (rootCauseMessage)
			{
				Throwable cause = throwable;

				while (cause.getCause() != null)
					cause = cause.getCause();

				this.message = cause.getMessage();
			}
			else
			{
				this.message = throwable.getMessage();
			}
		}
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
