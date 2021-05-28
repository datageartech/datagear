/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

import java.io.Serializable;
import java.util.Locale;

import org.datagear.util.DateFormat;

/**
 * 数据格式。
 * 
 * @author datagear@163.com
 *
 */
public class DataFormat extends DateFormat implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_NUMBER_FORMAT = "#.######";

	public static final String BINARY_FORMAT_HEX = "Hex";

	public static final String BINARY_FORMAT_BASE64 = "Base64";

	public static final String BINARY_FORMAT_NULL = "NULL";

	/** 地区 */
	private Locale locale = Locale.getDefault();

	/** 数值格式 */
	private String numberFormat = DEFAULT_NUMBER_FORMAT;

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
		this.numberFormat = numberFormat;
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

	public String getNumberFormat()
	{
		return numberFormat;
	}

	public void setNumberFormat(String numberFormat)
	{
		this.numberFormat = numberFormat;
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
				+ ", timestampFormat=" + getTimestampFormat() + ", numberFormat=" + numberFormat + ", binaryFormat="
				+ binaryFormat + "]";
	}
}
