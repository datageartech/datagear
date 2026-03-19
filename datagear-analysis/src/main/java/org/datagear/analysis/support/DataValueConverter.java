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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.datagear.analysis.NameTypeAware;
import org.datagear.analysis.NameTypeInputAware;
import org.datagear.util.StringUtil;

/**
 * 数据值转换器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataValueConverter<T extends NameTypeAware>
{
	/** 正则表达式：小数 */
	public static final Pattern PATTERN_DECIMAL_NUMBER = Pattern.compile("^[^\\.]+\\.[^\\.]+$");

	/** 正则表达式：整数 */
	public static final Pattern PATTERN_INTEGER = Pattern.compile("^-?[1-9]\\d*$");

	/**
	 * 转换数据值。
	 * <p>
	 * 如果{@code nameTypeAware}是{@linkplain NameTypeInputAware}实例且{@linkplain NameTypeInputAware#isMultiple()}为{@code true}，
	 * 而{@code value}既不是数组，也不是集合，那么返回值将是包含其转换结果一个元素的{@code Object[]}数组。
	 * </p>
	 * 
	 * @param <T>
	 * @param value
	 *            待转换的数据值、数据值数组、数据值集合，允许{@code null}
	 * @param target
	 *            允许{@code null}
	 * @return 转换结果对象，当{@code value}是数组时，返回{@code Object[]}；当{@code value}是{@linkplain Collection}时，返回{@linkplain List}。
	 * @throws DataValueConvertionException
	 */
	public Object convert(Object value, T target) throws DataValueConvertionException
	{
		if (value == null || target == null)
			return value;

		if (value instanceof Object[])
		{
			Object[] src = (Object[]) value;
			return convertArray(src, target);
		}
		else if (value instanceof Collection<?>)
		{
			@SuppressWarnings("unchecked")
			Collection<Object> src = (Collection<Object>) value;
			return convertCollection(src, target);
		}
		else
			return convertValue(value, target);
	}

	protected Object convertArray(Object[] values, T target) throws DataValueConvertionException
	{
		if (values == null)
			return null;

		Object[] to = new Object[values.length];

		for (int i = 0; i < values.length; i++)
			to[i] = convertValue(values[i], target);

		return to;
	}

	protected Object convertCollection(Collection<?> values, T target) throws DataValueConvertionException
	{
		if (values == null)
			return null;

		List<Object> to = new ArrayList<>(values.size());

		for (Object ele : values)
			to.add(convertValue(ele, target));

		return to;
	}

	/**
	 * 转换数据值。
	 * 
	 * @param value
	 *            要转换的数据值，不会是数组/集合，不会为{@code null}
	 * @param target
	 *            不会为{@code null}
	 * @return
	 * @throws DataValueConvertionException
	 */
	protected abstract Object convertValue(Object value, T target) throws DataValueConvertionException;

	protected Number convertToInteger(Object value, T target)
	{
		Number re = convertToNumber(value, target);

		if (re == null)
			return null;

		if (re instanceof Integer)
			return re;

		if (re instanceof Long)
			return re;

		return narrowIfIntegerRange(re.longValue());
	}

	protected Number convertToNumber(Object value, T target)
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
					return narrowIfIntegerRange(re);
				}
			}
			catch (NumberFormatException e)
			{
				return (Number) convertExt(value, target);
			}
		}

		return (Number) convertExt(value, target);
	}

	protected Number narrowIfIntegerRange(Long value)
	{
		if (value == null)
			return null;

		long lv = value.longValue();

		if (lv <= Integer.MAX_VALUE && lv >= Integer.MIN_VALUE)
			return value.intValue();
		else
			return value;
	}

	protected String convertToString(Object value, T target)
	{
		if (value == null)
			return null;

		if (value instanceof String)
			return (String) value;

		return value.toString();
	}

	protected Boolean convertToBoolean(Object value, T target)
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

		return (Boolean) convertExt(value, target);
	}

	/**
	 * 将符合JSON规范的字符串转换为对象。
	 * 
	 * @param value
	 * @param target
	 * @return
	 */
	protected Object convertJsonToObj(String value, T target)
	{
		if (StringUtil.isEmpty(value))
			return null;
		
		try
		{
			Object re = JsonSupport.parseNonStardand(value, Object.class);
			return re;
		}
		catch(Exception e)
		{
			return convertExt(value, target);
		}
	}

	/**
	 * 将符合JSON对象/数组规范的字符串转换为对象。
	 * 
	 * @param value
	 *            仅允许<code>"{ ... }"</code>、{@code "[ ... ]"}、
	 *            {@code null}、{@code ""}的JSON格式
	 * @param target
	 * @return
	 */
	protected Object convertJsonToObjStrictly(String value, T target)
	{
		if (StringUtil.isEmpty(value))
			return null;

		try
		{
			Object re = JsonSupport.parseNonStardand(value, Object.class);

			if (isStrictJsonObject(re))
				return re;

			return convertExt(value, target);
		}
		catch (Exception e)
		{
			return convertExt(value, target);
		}
	}

	/**
	 * 是否严格JSON对象（{@code Map<?, ?>}、{@code List<?>}、{@code Object[]}）。
	 * 
	 * @param o
	 * @return
	 */
	protected boolean isStrictJsonObject(Object o)
	{
		if (o == null)
			return false;

		if (o instanceof Map<?, ?>)
			return true;

		if (o instanceof List<?>)
			return true;

		if (o instanceof Object[])
			return true;

		return false;
	}

	/**
	 * 将对象转为JSON字符串。
	 * 
	 * @param obj
	 * @param target
	 * @return
	 */
	protected Object convertObjToJsonString(Object value, T target)
	{
		if (value == null)
			return null;

		try
		{
			String re = JsonSupport.generate(value);
			return re;
		}
		catch (Exception e)
		{
			return convertExt(value, target);
		}
	}

	protected Object convertExt(Object value, T target) throws DataValueConvertionException
	{
		throw new DataValueConvertionException(value, target.getType(), "Convert ["
				+ StringUtil.truncate(value, 20, "...") + "] to type [" + target.getType() + "] is not supported");
	}

	/**
	 * 将字符串转换为日期。
	 * <p>
	 * 如果{@code str}不匹配{@code format}，但又匹配整数的话，将按照毫秒数转换处理。
	 * </p>
	 * 
	 * @param str
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	protected java.util.Date convertToDateWithInteger(String str, SimpleDateFormat format) throws ParseException
	{
		if (StringUtil.isEmpty(str))
			return null;
		
		// 这里应优先parse，因为符合format的str也可能匹配数值格式
		try
		{
			return format.parse(str);
		}
		catch(ParseException e)
		{
			// 是整数
			if (isIntegerString(str))
			{
				try
				{
					long time = Long.valueOf(str);
					return new java.util.Date(time);
				}
				catch (NumberFormatException e1)
				{
					throw e;
				}
			}
			else
				throw e;
		}
	}

	protected boolean isDecimalNumberString(String str)
	{
		return PATTERN_DECIMAL_NUMBER.matcher(str).matches();
	}

	protected boolean isIntegerString(String str)
	{
		return PATTERN_INTEGER.matcher(str).matches();
	}
}
