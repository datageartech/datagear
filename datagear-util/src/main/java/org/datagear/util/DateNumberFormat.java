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

package org.datagear.util;

/**
 * 日期、时间格式。
 * 
 * @author datagear@163.com
 *
 */
public class DateNumberFormat extends DateFormat
{
	private static final long serialVersionUID = 1L;

	/**
	 * 默认数值格式：#.##########
	 */
	public static final String DEFAULT_NUMBER_FORMAT = "#.##########";

	/** 数值格式 */
	private String numberFormat = DEFAULT_NUMBER_FORMAT;

	public DateNumberFormat()
	{
		super();
	}

	public String getNumberFormat()
	{
		return numberFormat;
	}

	public void setNumberFormat(String numberFormat)
	{
		this.numberFormat = numberFormat;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((numberFormat == null) ? 0 : numberFormat.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DateNumberFormat other = (DateNumberFormat) obj;
		if (numberFormat == null)
		{
			if (other.numberFormat != null)
				return false;
		}
		else if (!numberFormat.equals(other.numberFormat))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [dateFormat=" + getDateFormat() + ", timeFormat=" + getTimeFormat()
				+ ", timestampFormat=" + getTimestampFormat() + ", numberFormat=" + numberFormat + "]";
	}
}
