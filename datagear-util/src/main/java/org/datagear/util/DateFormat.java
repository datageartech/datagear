/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util;

import java.io.Serializable;

/**
 * 日期格式。
 * 
 * @author datagear@163.com
 *
 */
public class DateFormat implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * 默认日期格式：yyyy-MM-dd
	 */
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * 默认时间格式：hh:mm:ss
	 */
	public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

	/**
	 * 默认时间戳格式：yyyy-MM-dd hh:mm:ss
	 */
	public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** 日期格式 */
	private String dateFormat = DEFAULT_DATE_FORMAT;

	/** 时间格式 */
	private String timeFormat = DEFAULT_TIME_FORMAT;

	/** 时间戳格式 */
	private String timestampFormat = DEFAULT_TIMESTAMP_FORMAT;

	public DateFormat()
	{
		super();
	}

	public String getDateFormat()
	{
		return dateFormat;
	}

	public void setDateFormat(String dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public String getTimeFormat()
	{
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat)
	{
		this.timeFormat = timeFormat;
	}

	public String getTimestampFormat()
	{
		return timestampFormat;
	}

	public void setTimestampFormat(String timestampFormat)
	{
		this.timestampFormat = timestampFormat;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateFormat == null) ? 0 : dateFormat.hashCode());
		result = prime * result + ((timeFormat == null) ? 0 : timeFormat.hashCode());
		result = prime * result + ((timestampFormat == null) ? 0 : timestampFormat.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DateFormat other = (DateFormat) obj;
		if (dateFormat == null)
		{
			if (other.dateFormat != null)
				return false;
		}
		else if (!dateFormat.equals(other.dateFormat))
			return false;
		if (timeFormat == null)
		{
			if (other.timeFormat != null)
				return false;
		}
		else if (!timeFormat.equals(other.timeFormat))
			return false;
		if (timestampFormat == null)
		{
			if (other.timestampFormat != null)
				return false;
		}
		else if (!timestampFormat.equals(other.timestampFormat))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [dateFormat=" + dateFormat + ", timeFormat=" + timeFormat + ", timestampFormat="
				+ timestampFormat + "]";
	}
}
