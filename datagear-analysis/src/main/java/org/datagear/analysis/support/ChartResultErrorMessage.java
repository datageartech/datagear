/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
