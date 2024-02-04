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

import org.datagear.util.DateNumberFormat;

/**
 * 数据格式。
 * 
 * @author datagear@163.com
 *
 */
public class DataFormat extends DateNumberFormat
{
	private static final long serialVersionUID = 1L;

	public static DataFormat DEFAULT = new DataFormat();

	static
	{
		DEFAULT.setDateFormat(DEFAULT_DATE_FORMAT);
		DEFAULT.setTimeFormat(DEFAULT_TIME_FORMAT);
		DEFAULT.setTimestampFormat(DEFAULT_TIMESTAMP_FORMAT);
		DEFAULT.setNumberFormat(DEFAULT_NUMBER_FORMAT);
	}

	public DataFormat()
	{
		super();
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
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
		return true;
	}
}
