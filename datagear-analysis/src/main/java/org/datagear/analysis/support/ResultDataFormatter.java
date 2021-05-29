/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.text.SimpleDateFormat;

import org.datagear.analysis.ResultDataFormat;

/**
 * {@linkplain ResultDataFormat}支持格式化类。
 * <p>
 * 注意：此类的{@linkplain #format(Object)}方法不是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ResultDataFormatter
{
	private ResultDataFormat resultDataFormat;

	private SimpleDateFormat _dateFormat = null;
	private SimpleDateFormat _timeFormat = null;
	private SimpleDateFormat _timestampFormat = null;
	
	public ResultDataFormatter()
	{
		super();
	}

	public ResultDataFormatter(ResultDataFormat resultDataFormat)
	{
		super();
		setResultDataFormat(resultDataFormat);
	}

	public ResultDataFormat getResultDataFormat()
	{
		return resultDataFormat;
	}

	public void setResultDataFormat(ResultDataFormat resultDataFormat)
	{
		this.resultDataFormat = resultDataFormat;
		
		if(ResultDataFormat.TYPE_STRING.equals(resultDataFormat.getDateType()))
			this._dateFormat = new SimpleDateFormat(resultDataFormat.getDateFormat());
		
		if(ResultDataFormat.TYPE_STRING.equals(resultDataFormat.getTimeType()))
			this._timeFormat = new SimpleDateFormat(resultDataFormat.getTimeFormat());
		
		if(ResultDataFormat.TYPE_STRING.equals(resultDataFormat.getTimestampType()))
			this._timestampFormat = new SimpleDateFormat(resultDataFormat.getTimestampFormat());
	}
	
	/**
	 * 格式化。
	 * 
	 * @param value
	 * @return
	 */
	public Object format(Object value)
	{
		Object re = value;
		
		if(value instanceof java.sql.Timestamp)
		{
			String type = this.resultDataFormat.getTimestampType();
			
			if(ResultDataFormat.TYPE_NONE.equals(type))
			{
				
			}
			else if(ResultDataFormat.TYPE_STRING.equals(type))
			{
				re = this._timestampFormat.format((java.sql.Timestamp) value);
			}
			else if(ResultDataFormat.TYPE_NUMBER.equals(type))
			{
				re = ((java.sql.Timestamp) value).getTime();
			}
		}
		else if(value instanceof java.sql.Time)
		{
			String type = this.resultDataFormat.getTimeType();
			
			if(ResultDataFormat.TYPE_NONE.equals(type))
			{
				
			}
			else if(ResultDataFormat.TYPE_STRING.equals(type))
			{
				re = this._timeFormat.format((java.sql.Time) value);
			}
			else if(ResultDataFormat.TYPE_NUMBER.equals(type))
			{
				re = ((java.sql.Time) value).getTime();
			}
		}
		else if(value instanceof java.util.Date)
		{
			String type = this.resultDataFormat.getDateType();
			
			if(ResultDataFormat.TYPE_NONE.equals(type))
			{
				
			}
			else if(ResultDataFormat.TYPE_STRING.equals(type))
			{
				re = this._dateFormat.format((java.util.Date) value);
			}
			else if(ResultDataFormat.TYPE_NUMBER.equals(type))
			{
				re = ((java.util.Date) value).getTime();
			}
		}
		
		return re;
	}
}
