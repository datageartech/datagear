/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetProperty.DataType;

/**
 * {@linkplain DataSetProperty}值转换器。
 * <p>
 * 它支持将对象转换为{@linkplain DataSetProperty.DataType}类型的值。
 * </p>
 * <p>
 * 此类的{@linkplain #convert(java.util.Map, java.util.Collection)}、{@linkplain #convert(Object, String)}不是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetPropertyValueConverter extends DataValueConverter
{
	private DataFormat dataFormat;

	private SimpleDateFormat _dateFormat = null;
	private SimpleDateFormat _timeFormat = null;
	private SimpleDateFormat _timestampFormat = null;
	private DecimalFormat _numberFormat = null;

	public DataSetPropertyValueConverter()
	{
		super();
		setDataFormat(new DataFormat());
	}

	public DataSetPropertyValueConverter(DataFormat dataFormat)
	{
		super();
		setDataFormat(dataFormat);
	}

	public DataFormat getDataFormat()
	{
		return dataFormat;
	}

	public void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;

		this._dateFormat = new SimpleDateFormat(dataFormat.getDateFormat());
		this._timeFormat = new SimpleDateFormat(dataFormat.getTimeFormat());
		this._timestampFormat = new SimpleDateFormat(dataFormat.getTimestampFormat());
		this._numberFormat = new DecimalFormat(dataFormat.getNumberFormat());
	}

	@Override
	protected Object convertValue(Object value, String type) throws DataValueConvertionException
	{
		if (value == null)
			return null;

		if (type == null)
			return value;

		try
		{
			if (value instanceof String)
				return convertStringValue((String) value, type);
			else if (value instanceof Boolean)
				return convertBooleanValue((Boolean) value, type);
			else if (value instanceof Number)
				return convertNumberValue((Number) value, type);
			else if (value instanceof Time)
				return convertTimeValue((Time) value, type);
			else if (value instanceof Timestamp)
				return convertTimestampValue((Timestamp) value, type);
			else if (value instanceof java.util.Date)
				return convertDateValue((java.util.Date) value, type);
			else
			{
				if (DataType.UNKNOWN.equals(type))
					return value;
				else
					throw new DataValueConvertionException(value, type);
			}
		}
		catch (DataValueConvertionException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataValueConvertionException(value, type);
		}
	}

	protected Object convertStringValue(String value, String type) throws Throwable
	{
		if (DataType.STRING.equals(type) || DataType.UNKNOWN.equals(type))
			return value;

		if (value == null || value.isEmpty())
			return null;

		if (DataType.BOOLEAN.equals(type))
			return "true".equalsIgnoreCase(value) || "1".equals(value);
		else if (DataType.NUMBER.equals(type))
			return this._numberFormat.parse(value);
		else if (DataType.INTEGER.equals(type))
			return this._numberFormat.parse(value).intValue();
		else if (DataType.DECIMAL.equals(type))
			return this._numberFormat.parse(value).doubleValue();
		else if (DataType.DATE.equals(type))
		{
			java.util.Date date = this._dateFormat.parse(value);
			return new Date(date.getTime());
		}
		else if (DataType.TIME.equals(type))
		{
			java.util.Date date = this._timeFormat.parse(value);
			return new Time(date.getTime());
		}
		else if (DataType.TIMESTAMP.equals(type))
		{
			java.util.Date date = this._timestampFormat.parse(value);
			return new Timestamp(date.getTime());
		}
		else
			throw new DataValueConvertionException(value, type);
	}

	protected Object convertBooleanValue(Boolean value, String type) throws Throwable
	{
		if (DataType.BOOLEAN.equals(type) || DataType.UNKNOWN.equals(type))
			return value;

		if (value == null)
			return null;

		if (DataType.STRING.equals(type))
			return value.toString();
		else if (DataType.NUMBER.equals(type) || DataType.INTEGER.equals(type) || DataType.DECIMAL.equals(type))
			return (Boolean.TRUE.equals(value) ? 1 : 0);
		else
			throw new DataValueConvertionException(value, type);
	}

	protected Object convertNumberValue(Number value, String type) throws Throwable
	{
		if (DataType.NUMBER.equals(type) || DataType.UNKNOWN.equals(type))
			return value;

		if (value == null)
			return null;

		if (DataType.STRING.equals(type))
			return this._numberFormat.format(value);
		else if (DataType.BOOLEAN.equals(type))
			return (value.intValue() > 0);
		else if (DataType.INTEGER.equals(type))
			return value.longValue();
		else if (DataType.DECIMAL.equals(type))
			return value.doubleValue();
		else if (DataType.DATE.equals(type))
			return new Date(value.longValue());
		else if (DataType.TIME.equals(type))
			return new Time(value.longValue());
		else if (DataType.TIMESTAMP.equals(type))
			return new Timestamp(value.longValue());
		else
			throw new DataValueConvertionException(value, type);
	}

	protected Object convertDateValue(java.util.Date value, String type) throws Throwable
	{
		if (DataType.UNKNOWN.equals(type))
			return value;

		if (value == null)
			return null;

		if (DataType.STRING.equals(type))
			return this._dateFormat.format(value);
		else if (DataType.NUMBER.equals(type))
			return value.getTime();
		else if (DataType.INTEGER.equals(type))
			return value.getTime();
		else if (DataType.DECIMAL.equals(type))
			return value.getTime();
		else if (DataType.DATE.equals(type))
			return new Date(value.getTime());
		else if (DataType.TIME.equals(type))
			return new Time(value.getTime());
		else if (DataType.TIMESTAMP.equals(type))
			return new Timestamp(value.getTime());
		else
			throw new DataValueConvertionException(value, type);
	}

	protected Object convertTimeValue(Time value, String type) throws Throwable
	{
		if (DataType.TIME.equals(type) || DataType.UNKNOWN.equals(type))
			return value;

		if (value == null)
			return null;

		if (DataType.STRING.equals(type))
			return this._timeFormat.format(value);
		else if (DataType.NUMBER.equals(type))
			return value.getTime();
		else if (DataType.INTEGER.equals(type))
			return value.getTime();
		else if (DataType.DECIMAL.equals(type))
			return value.getTime();
		else if (DataType.DATE.equals(type))
			return new Date(value.getTime());
		else if (DataType.TIMESTAMP.equals(type))
			return new Timestamp(value.getTime());
		else
			throw new DataValueConvertionException(value, type);
	}

	protected Object convertTimestampValue(Timestamp value, String type) throws Throwable
	{
		if (DataType.TIMESTAMP.equals(type) || DataType.UNKNOWN.equals(type))
			return value;

		if (value == null)
			return null;

		if (DataType.STRING.equals(type))
			return this._timestampFormat.format(value);
		else if (DataType.NUMBER.equals(type))
			return value.getTime();
		else if (DataType.INTEGER.equals(type))
			return value.getTime();
		else if (DataType.DECIMAL.equals(type))
			return value.getTime();
		else if (DataType.DATE.equals(type))
			return new Date(value.getTime());
		else if (DataType.TIME.equals(type))
			return new Time(value.getTime());
		else
			throw new DataValueConvertionException(value, type);
	}
}
