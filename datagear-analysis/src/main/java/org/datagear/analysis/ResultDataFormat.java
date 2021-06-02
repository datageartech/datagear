/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import org.datagear.util.DateFormat;

/**
 * 数据集结果数据格式。
 * 
 * @author datagear@163.com
 *
 */
public class ResultDataFormat extends DateFormat
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 格式化类型：数值，表示格式化为数值
	 */
	public static final String TYPE_NUMBER = "NUMBER";

	/**
	 * 格式化类型：字符串，表示格式化为字符串
	 */
	public static final String TYPE_STRING = "STRING";

	/**
	 * 格式化类型：无，表示不格式化，保持原类型
	 */
	public static final String TYPE_NONE = "NONE";

	/** 日期格式化类型 */
	private String dateType = TYPE_STRING;

	/** 时间格式化类型 */
	private String timeType = TYPE_STRING;

	/** 时间戳格式化类型 */
	private String timestampType = TYPE_STRING;
	
	public ResultDataFormat()
	{
		super();
	}

	public String getDateType()
	{
		return dateType;
	}

	public void setDateType(String dateType)
	{
		this.dateType = dateType;
	}

	/**
	 * 获取当{@linkplain #getDateType()}为{@linkplain #TYPE_STRING}时的日期格式。
	 * 
	 * @return
	 */
	@Override
	public String getDateFormat()
	{
		return super.getDateFormat();
	}

	public String getTimeType()
	{
		return timeType;
	}

	public void setTimeType(String timeType)
	{
		this.timeType = timeType;
	}

	/**
	 * 获取当{@linkplain #getTimeType()}为{@linkplain #TYPE_STRING}时的时间格式。
	 * 
	 * @return
	 */
	@Override
	public String getTimeFormat()
	{
		return super.getTimeFormat();
	}

	public String getTimestampType()
	{
		return timestampType;
	}

	public void setTimestampType(String timestampType)
	{
		this.timestampType = timestampType;
	}

	/**
	 * 获取当{@linkplain #getTimestampType()}为{@linkplain #TYPE_STRING}时的时间戳格式。
	 * 
	 * @return
	 */
	@Override
	public String getTimestampFormat()
	{
		return super.getTimestampFormat();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dateType == null) ? 0 : dateType.hashCode());
		result = prime * result + ((timeType == null) ? 0 : timeType.hashCode());
		result = prime * result + ((timestampType == null) ? 0 : timestampType.hashCode());
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
		ResultDataFormat other = (ResultDataFormat) obj;
		if (dateType == null)
		{
			if (other.dateType != null)
				return false;
		}
		else if (!dateType.equals(other.dateType))
			return false;
		if (timeType == null)
		{
			if (other.timeType != null)
				return false;
		}
		else if (!timeType.equals(other.timeType))
			return false;
		if (timestampType == null)
		{
			if (other.timestampType != null)
				return false;
		}
		else if (!timestampType.equals(other.timestampType))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [dateType=" + dateType
				+ ", dateFormat=" + getDateFormat() + ", timeType=" + timeType + ", timeFormat=" + getTimeFormat()
				+ ", timestampType=" + timestampType + ", timestampFormat=" + getTimestampFormat() + "]";
	}
}
