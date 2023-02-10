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

import java.math.BigDecimal;
import java.math.BigInteger;
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
 * 此类不是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetPropertyValueConverter extends DataValueConverter
{
	private DataFormat dataFormat;

	/**
	 * 是否忽略{@linkplain BigInteger}至{@linkplain DataType#INTEGER}的转换。
	 */
	private boolean ignoreBigIntegerToInteger = true;

	/**
	 * 是否忽略{@linkplain BigDecimal}至{@linkplain DataType#DECIMAL}的转换。
	 */
	private boolean ignoreBigDecimalToDecimal = true;

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

	public boolean isIgnoreBigIntegerToInteger()
	{
		return ignoreBigIntegerToInteger;
	}

	public void setIgnoreBigIntegerToInteger(boolean ignoreBigIntegerToInteger)
	{
		this.ignoreBigIntegerToInteger = ignoreBigIntegerToInteger;
	}

	public boolean isIgnoreBigDecimalToDecimal()
	{
		return ignoreBigDecimalToDecimal;
	}

	public void setIgnoreBigDecimalToDecimal(boolean ignoreBigDecimalToDecimal)
	{
		this.ignoreBigDecimalToDecimal = ignoreBigDecimalToDecimal;
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
			java.util.Date date = convertToDateWithInteger(value, this._dateFormat);
			return new Date(date.getTime());
		}
		else if (DataType.TIME.equals(type))
		{
			java.util.Date date = convertToDateWithInteger(value, this._timeFormat);
			return new Time(date.getTime());
		}
		else if (DataType.TIMESTAMP.equals(type))
		{
			java.util.Date date = convertToDateWithInteger(value, this._timestampFormat);
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
		{
			if (this.ignoreBigIntegerToInteger && (value instanceof BigInteger))
				return value;
			else
				return value.longValue();
		}
		else if (DataType.DECIMAL.equals(type))
		{
			if (this.ignoreBigDecimalToDecimal && (value instanceof BigDecimal))
				return value;
			else
				return value.doubleValue();
		}
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
