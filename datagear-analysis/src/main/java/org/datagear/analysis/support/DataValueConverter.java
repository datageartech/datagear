/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataNameType;
import org.datagear.analysis.DataType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;

/**
 * 数据值转换器。
 * 
 * @author datagear@163.com
 *
 */
public class DataValueConverter
{
	private ConversionService conversionService;

	public DataValueConverter()
	{
		super();
	}

	public DataValueConverter(ConversionService conversionService)
	{
		super();
		this.conversionService = conversionService;
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	/**
	 * 转换数据值映射表。
	 * 
	 * @param paramValues
	 *            允许为{@code null}
	 * @param dataNameTypes
	 * @return 转换结果映射表，如果{@code paramValues}为{@code null}，则返回{@code null}
	 */
	public Map<String, Object> convert(Map<String, ?> paramValues, List<? extends DataNameType> dataNameTypes)
			throws DataValueConvertionException
	{
		if (paramValues == null)
			return null;

		Map<String, Object> re = new HashMap<>(paramValues);

		for (DataNameType param : dataNameTypes)
		{
			String name = param.getName();

			if (!paramValues.containsKey(name))
				continue;

			Object value = paramValues.get(name);
			value = convert(value, param.getType());

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
	public Object convert(Object value, DataType type) throws DataValueConvertionException
	{
		if (value == null)
			return null;

		if (DataType.isString(type))
			return convertToType(value, String.class, type);
		else if (DataType.isBoolean(type))
			return convertToType(value, Boolean.class, type);
		else if (DataType.isInteger(type))
			return convertToType(value, Long.class, type);
		else if (DataType.isDecimal(type))
			return convertToType(value, Double.class, type);
		else if (DataType.isDate(type))
			return convertToType(value, java.sql.Date.class, type);
		else if (DataType.isTime(type))
			return convertToType(value, java.sql.Time.class, type);
		else if (DataType.isTimestamp(type))
			return convertToType(value, java.sql.Timestamp.class, type);
		else
			throw new UnsupportedOperationException();
	}

	protected Object convertToType(Object src, Class<?> type, DataType dataType) throws DataValueConvertionException
	{
		try
		{
			return this.conversionService.convert(src, type);
		}
		catch (ConversionException e)
		{
			throw new DataValueConvertionException(src, dataType, e);
		}
	}
}
