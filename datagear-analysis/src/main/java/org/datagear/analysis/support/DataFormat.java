/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Serializable;

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

	/**
	 * 默认数值格式：#.##########
	 */
	public static final String DEFAULT_NUMBER_FORMAT = "#.##########";

	/** 数值格式 */
	private String numberFormat = DEFAULT_NUMBER_FORMAT;

	public DataFormat()
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
		DataFormat other = (DataFormat) obj;
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
