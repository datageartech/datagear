/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
	 * 转换数据值映射表，返回一个经转换的新映射表。
	 * <p>
	 * 如果{@code nameValues}中有未在{@code dataNameTypes}中定义的项，那么它将原样写入返回映射表中。
	 * </p>
	 * 
	 * @param nameValues
	 *            原始名/值映射表，允许为{@code null}
	 * @param dataNameTypes
	 *            名/类型集合，允许为{@code null}
	 * @return
	 * @throws DataValueConvertionException
	 */
	public Map<String, Object> convert(Map<String, ?> nameValues, Collection<? extends DataNameType> dataNameTypes)
			throws DataValueConvertionException
	{
		if (nameValues == null)
			return null;

		Map<String, Object> re = new HashMap<>(nameValues);

		if (dataNameTypes != null)
		{
			for (DataNameType dnt : dataNameTypes)
			{
				String name = dnt.getName();

				if (!nameValues.containsKey(name))
					continue;

				Object value = nameValues.get(name);
				value = convert(value, dnt.getType());

				re.put(name, value);
			}
		}

		return re;
	}

	/**
	 * 转换数据值。
	 * 
	 * @param value
	 *            待转换的数据值、数据值数组、数据值集合。
	 * @param type
	 *            目标类型
	 * @return 转换结果对象，当{@code value}是数组时，返回{@code Object[]}；当{@code value}是{@linkplain Collection}时，返回{@linkplain List}。
	 * @throws DataValueConvertionException
	 */
	public Object convert(Object value, String type) throws DataValueConvertionException
	{
		if (value == null)
		{
			return convertValue(value, type);
		}
		else if (value instanceof Object[])
		{
			Object[] src = (Object[]) value;
			return convertArray(src, type);
		}
		else if (value instanceof Collection<?>)
		{
			@SuppressWarnings("unchecked")
			Collection<Object> src = (Collection<Object>) value;
			return convertCollection(src, type);
		}
		else
			return convertValue(value, type);
	}

	protected Object convertArray(Object[] values, String type) throws DataValueConvertionException
	{
		if (values == null)
			throw new IllegalArgumentException("[values] must not be null");

		Object[] target = new Object[values.length];

		for (int i = 0; i < values.length; i++)
		{
			target[i] = convertValue(values[i], type);
		}

		return target;
	}

	protected Object convertCollection(Collection<?> values, String type) throws DataValueConvertionException
	{
		if (values == null)
			throw new IllegalArgumentException("[values] must not be null");

		List<Object> target = new ArrayList<>(values.size());

		for (Object ele : values)
		{
			target.add(convertValue(ele, type));
		}

		return target;
	}

	/**
	 * 转换数据值。
	 * 
	 * @param value
	 *            要转换的数据值，不会是数组，可能为{@code null}
	 * @param type
	 * @return
	 * @throws DataValueConvertionException
	 */
	protected abstract Object convertValue(Object value, String type) throws DataValueConvertionException;

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
			catch (NumberFormatException e)
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

	protected Object convertExt(Object value, String type) throws DataValueConvertionException
	{
		throw new DataValueConvertionException(value, type,
				"Convert [" + value + "] to type [" + type + "] is not supported");
	}

	protected boolean isDecimalNumberString(String str)
	{
		return PATTERN_DECIMAL_NUMBER.matcher(str).matches();
	}
}
