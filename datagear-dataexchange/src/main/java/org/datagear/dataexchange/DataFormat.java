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

package org.datagear.dataexchange;

import java.io.Serializable;
import java.util.Locale;

import org.datagear.util.DateNumberFormat;

/**
 * 数据格式。
 * 
 * @author datagear@163.com
 *
 */
public class DataFormat extends DateNumberFormat implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String BINARY_FORMAT_HEX = "Hex";

	public static final String BINARY_FORMAT_BASE64 = "Base64";

	public static final String BINARY_FORMAT_NULL = "NULL";

	/** 地区 */
	private Locale locale = Locale.getDefault();

	/** 二进制格式 */
	private String binaryFormat = BINARY_FORMAT_HEX;

	public DataFormat()
	{
		super();
	}

	public DataFormat(Locale locale, String dateFormat, String timeFormat, String timestampFormat, String numberFormat,
			String binaryFormat)
	{
		super();
		this.locale = locale;
		setDateFormat(dateFormat);
		setTimeFormat(timeFormat);
		setTimestampFormat(timestampFormat);
		setNumberFormat(numberFormat);
		this.binaryFormat = binaryFormat;
	}

	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	public String getBinaryFormat()
	{
		return binaryFormat;
	}

	public void setBinaryFormat(String binaryFormat)
	{
		this.binaryFormat = binaryFormat;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [dateFormat=" + getDateFormat() + ", timeFormat=" + getTimeFormat()
				+ ", timestampFormat=" + getTimestampFormat() + ", numberFormat=" + getNumberFormat()
				+ ", binaryFormat="
				+ binaryFormat + "]";
	}
}
