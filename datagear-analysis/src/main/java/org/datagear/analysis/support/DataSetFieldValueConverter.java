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

package org.datagear.analysis.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetField.DataType;
import org.datagear.util.StringUtil;

/**
 * {@linkplain DataSetField}值转换器。
 * <p>
 * 它支持将对象转换为{@linkplain DataSetField.DataType}类型的值。
 * </p>
 * <p>
 * 此类不是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetFieldValueConverter extends DataValueConverter<DataSetField>
{
	private DataFormat dataFormat;

	/**
	 * 是否忽略{@linkplain BigInteger}至{@linkplain DataType#INTEGER}的转换。
	 */
	private boolean ignoreBigIntegerToInteger = true;

	/**
	 * 对于映射表，是否执行严格模式：true 不保留无对应字段的值；false 保留无对应字段的值
	 */
	private boolean strictForMap = true;

	private SimpleDateFormat _dateFormat = null;
	private SimpleDateFormat _timeFormat = null;
	private SimpleDateFormat _timestampFormat = null;
	private DecimalFormat _numberFormat = null;

	public DataSetFieldValueConverter()
	{
		super();
		setDataFormat(new DataFormat());
	}

	public DataSetFieldValueConverter(DataFormat dataFormat)
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

	public boolean isStrictForMap()
	{
		return strictForMap;
	}

	public void setStrictForMap(boolean strictForMap)
	{
		this.strictForMap = strictForMap;
	}

	/**
	 * 转换字段值映射表，返回一个经转换的新映射表。
	 * <p>
	 * 转换规则受{@linkplain #isStrictForMap()}影响。
	 * </p>
	 * 
	 * @param fieldValues
	 *            允许为{@code null}
	 * @param targets
	 *            允许为{@code null}
	 * @return
	 */
	public Map<String, Object> convert(Map<String, ?> fieldValues, Collection<DataSetField> targets)
	{
		if (fieldValues == null)
			return null;

		Map<String, Object> re = (isStrictForMap() ? new HashMap<>() : new HashMap<>(fieldValues));

		if (targets != null)
		{
			for (DataSetField target : targets)
			{
				String name = target.getName();
				Object value = fieldValues.get(name);
				value = convert(value, target);

				re.put(name, value);
			}
		}

		return re;
	}

	@Override
	public Object convert(Object value, DataSetField target) throws DataValueConvertionException
	{
		if (value == null && target != null)
			value = target.getDefaultValue();

		Object re = super.convert(value, target);

		// 数组与非数组互转
		if (re != null && target != null)
		{
			boolean likeArray = DataType.isLikeArray(re);

			if(likeArray && !target.isArray())
			{
				re = DataType.getLikeArrayFirstEle(re);
			}
			else if (!likeArray && target.isArray())
			{
				re = DataType.wrapToLikeArray(re);
			}
		}

		return re;
	}

	@Override
	protected Object convertValue(Object value, DataSetField target) throws DataValueConvertionException
	{
		if (value == null || target == null || DataType.UNKNOWN.equals(target.getType()))
			return value;

		try
		{
			if (value instanceof String)
				return convertStringValue((String) value, target);
			else if (value instanceof Boolean)
				return convertBooleanValue((Boolean) value, target);
			else if (value instanceof Number)
				return convertNumberValue((Number) value, target);
			else if (value instanceof Time)
				return convertTimeValue((Time) value, target);
			else if (value instanceof Timestamp)
				return convertTimestampValue((Timestamp) value, target);
			else if (value instanceof java.util.Date)
				return convertDateValue((java.util.Date) value, target);
			else if (value instanceof Map<?, ?>)
				return convertMapValue((Map<?, ?>) value, target);
			else
				return convertExt(value, target);
		}
		catch (DataValueConvertionException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			return convertExt(value, target);
		}
	}

	protected Object convertStringValue(String value, DataSetField target) throws Throwable
	{
		if (value == null || target == null)
			return value;

		String type = target.getType();

		if (DataType.STRING.equals(type))
			return value;

		if (value.isEmpty())
			return null;

		if (DataType.BOOLEAN.equals(type))
			return StringUtil.toBoolean(value);
		else if (DataType.NUMBER.equals(type))
			return this._numberFormat.parse(value);
		else if (DataType.INTEGER.equals(type))
		{
			Long number = this._numberFormat.parse(value).longValue();
			return narrowIfIntegerRange(number);
		}
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
		else if (DataType.isObjectType(type))
		{
			// 必须采用严格模式，避免与允许的String类型逻辑冲突
			Object obj = convertJsonToObjStrictly(value, target);
			return obj;
		}
		else
			return convertExt(value, target);
	}

	protected Object convertBooleanValue(Boolean value, DataSetField target) throws Throwable
	{
		if (value == null || target == null)
			return value;

		String type = target.getType();

		if (DataType.BOOLEAN.equals(type))
			return value;

		if (DataType.STRING.equals(type))
			return value.toString();
		else if (DataType.NUMBER.equals(type) || DataType.INTEGER.equals(type))
			return (Boolean.TRUE.equals(value) ? 1 : 0);
		else
			return convertExt(value, target);
	}

	protected Object convertNumberValue(Number value, DataSetField target) throws Throwable
	{
		if (value == null || target == null)
			return value;

		String type = target.getType();

		if (DataType.NUMBER.equals(type))
			return value;

		if (DataType.STRING.equals(type))
			return this._numberFormat.format(value);
		else if (DataType.BOOLEAN.equals(type))
			return (value.intValue() > 0);
		else if (DataType.INTEGER.equals(type))
		{
			if (this.ignoreBigIntegerToInteger && (value instanceof BigInteger))
				return value;
			else if (value instanceof Float || value instanceof Double || value instanceof BigDecimal
					|| value instanceof BigInteger)
				return value.longValue();
			else
				return value;
		}
		else if (DataType.DATE.equals(type))
			return new Date(value.longValue());
		else if (DataType.TIME.equals(type))
			return new Time(value.longValue());
		else if (DataType.TIMESTAMP.equals(type))
			return new Timestamp(value.longValue());
		else
			return convertExt(value, target);
	}

	protected Object convertDateValue(java.util.Date value, DataSetField target) throws Throwable
	{
		if (value == null || target == null)
			return value;

		String type = target.getType();

		if (DataType.STRING.equals(type))
			return this._dateFormat.format(value);
		else if (DataType.NUMBER.equals(type))
			return value.getTime();
		else if (DataType.INTEGER.equals(type))
			return value.getTime();
		else if (DataType.DATE.equals(type))
			return new Date(value.getTime());
		else if (DataType.TIME.equals(type))
			return new Time(value.getTime());
		else if (DataType.TIMESTAMP.equals(type))
			return new Timestamp(value.getTime());
		else
			return convertExt(value, target);
	}

	protected Object convertTimeValue(Time value, DataSetField target) throws Throwable
	{
		if (value == null || target == null)
			return value;

		String type = target.getType();

		if (DataType.TIME.equals(type))
			return value;

		if (DataType.STRING.equals(type))
			return this._timeFormat.format(value);
		else if (DataType.NUMBER.equals(type))
			return value.getTime();
		else if (DataType.INTEGER.equals(type))
			return value.getTime();
		else if (DataType.DATE.equals(type))
			return new Date(value.getTime());
		else if (DataType.TIMESTAMP.equals(type))
			return new Timestamp(value.getTime());
		else
			return convertExt(value, target);
	}

	protected Object convertTimestampValue(Timestamp value, DataSetField target) throws Throwable
	{
		if (value == null || target == null)
			return value;

		String type = target.getType();

		if (DataType.TIMESTAMP.equals(type))
			return value;

		if (DataType.STRING.equals(type))
			return this._timestampFormat.format(value);
		else if (DataType.NUMBER.equals(type))
			return value.getTime();
		else if (DataType.INTEGER.equals(type))
			return value.getTime();
		else if (DataType.DATE.equals(type))
			return new Date(value.getTime());
		else if (DataType.TIME.equals(type))
			return new Time(value.getTime());
		else
			return convertExt(value, target);
	}

	@SuppressWarnings("unchecked")
	protected Object convertMapValue(Map<?, ?> value, DataSetField target) throws Throwable
	{
		if (value == null || target == null)
			return value;

		String type = target.getType();

		if (DataType.isObjectType(type))
		{
			return convert((Map<String, ?>) value, target.getFields());
		}
		else if (DataType.STRING.equals(type))
		{
			return convertObjToJsonString(value, target);
		}
		else
			return convertExt(value, target);
	}
}
