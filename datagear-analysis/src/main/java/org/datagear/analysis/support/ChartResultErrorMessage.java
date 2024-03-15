/*
 * Copyright 2018-present datagear.tech
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
public class ChartResultErrorMessage extends ErrorMessage
{
	private static final long serialVersionUID = 1L;

	public ChartResultErrorMessage()
	{
		super();
	}

	public ChartResultErrorMessage(String message)
	{
		super(message);
	}

	public ChartResultErrorMessage(String type, String message)
	{
		super(type, message);
	}

	public ChartResultErrorMessage(Throwable throwable)
	{
		super(throwable);
	}

	public ChartResultErrorMessage(Throwable throwable, boolean rootCauseMessage)
	{
		super(throwable, rootCauseMessage);
	}

	public ChartResultErrorMessage(ChartResultError error)
	{
		this(error, false);
	}

	public ChartResultErrorMessage(ChartResultError error, boolean rootCauseMessage)
	{
		super(error.getThrowable(), rootCauseMessage);
	}
}
