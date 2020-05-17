/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.datagear.analysis.DataNameType;

/**
 * 数据值转换器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataValueConverter
{
	public static final Pattern PATTERN_DECIMAL_NUMBER = Pattern.compile("^[^\\.]+\\.[^\\.]+$");
	
	/**
	 * 转换数据值映射表。
	 * 
	 * @param nameValues
	 * @param dataNameTypes
	 * @return
	 * @throws DataValueConvertionException
	 */
	public Map<String, Object> convert(Map<String, ?> nameValues, Collection<? extends DataNameType> dataNameTypes)
			throws DataValueConvertionException
	{
		if (nameValues == null)
			return null;

		Map<String, Object> re = new HashMap<>(nameValues);

		for (DataNameType dnt : dataNameTypes)
		{
			String name = dnt.getName();

			if (!nameValues.containsKey(name))
				continue;

			Object value = nameValues.get(name);
			value = convert(value, dnt.getType());

			re.put(name, value);
		}

		return re;
	}

	/**
	 * 转换数据值。
	 * 
	 * @param value
	 * @param type
	 * @return
	 * @throws DataValueConvertionException
	 */
	public abstract Object convert(Object value, String type) throws DataValueConvertionException;

	protected Object convertExt(Object value, String type) throws DataValueConvertionException
	{
		throw new DataValueConvertionException(value, type,
				"Convert [" + value + "] to type [" + type + "] is not supported");
	}

	protected Number convertToNumber(Object value, String numberType)
	{
		if (value == null)
			return null;

		if (value instanceof Number)
			return (Number) value;

		if (value instanceof String)
		{
			String str = (String) value;

			if (str.isEmpty())
				return null;

			try
			{
				if (isDecimalNumberString(str))
					return Double.valueOf(str);
				else
				{
					Long re = Long.valueOf(str);

					if (re <= Integer.MAX_VALUE && re >= Integer.MIN_VALUE)
						return re.intValue();
					else
						return re.longValue();
				}
			}
			catch(NumberFormatException e)
			{
				throw new DataValueConvertionException(value, numberType, e);
			}
		}

		return (Number) convertExt(value, numberType);
	}

	protected String convertToString(Object value, String stringType)
	{
		if (value == null)
			return null;

		if (value instanceof String)
			return (String) value;

		return value.toString();
	}

	protected Boolean convertToBoolean(Object value, String booleanType)
	{
		if (value == null)
			return null;

		if (value instanceof Boolean)
			return (Boolean) value;

		if (value instanceof String)
		{
			String str = (String) value;

			if (str.isEmpty())
				return null;
			else
				return str.equalsIgnoreCase("true") || str.equals("1");
		}

		return (Boolean) convertExt(value, booleanType);
	}

	protected boolean isDecimalNumberString(String str)
	{
		return PATTERN_DECIMAL_NUMBER.matcher(str).matches();
	}
}
