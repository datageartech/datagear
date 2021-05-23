/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.io.Serializable;

import org.datagear.analysis.support.DataFormat;

/**
 * 数据集结果数据格式。
 * 
 * @author datagear@163.com
 *
 */
public class ResultDataFormat implements Serializable
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
	private String dateType = TYPE_NONE;

	/** 日期格式 */
	private String dateFormat = DataFormat.DEFAULT_DATE_FORMAT;
	
	/** 时间格式化类型 */
	private String timeType = TYPE_NONE;

	/** 时间格式 */
	private String timeFormat = DataFormat.DEFAULT_TIME_FORMAT;

	/** 时间戳格式化类型 */
	private String timestampType = TYPE_NONE;

	/** 时间戳格式 */
	private String timestampFormat = DataFormat.DEFAULT_TIMESTAMP_FORMAT;

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
	public String getDateFormat()
	{
		return dateFormat;
	}

	public void setDateFormat(String dateFormat)
	{
		this.dateFormat = dateFormat;
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
	public String getTimeFormat()
	{
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat)
	{
		this.timeFormat = timeFormat;
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
	public String getTimestampFormat()
	{
		return timestampFormat;
	}

	public void setTimestampFormat(String timestampFormat)
	{
		this.timestampFormat = timestampFormat;
	}
}
