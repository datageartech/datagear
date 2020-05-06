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

import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;

/**
 * {@linkplain DataSetParam}参数值转换器。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamValueConverter
{
	private ConversionService conversionService;

	public DataSetParamValueConverter()
	{
		super();
	}

	public DataSetParamValueConverter(ConversionService conversionService)
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
	 * 转换参数值映射表。
	 * 
	 * @param params
	 * @param paramValues
	 *            允许为{@code null}
	 * @return 转换结果映射表，如果{@code paramValues}为{@code null}，则返回{@code null}
	 */
	public Map<String, Object> convert(List<DataSetParam> params, Map<String, ?> paramValues)
			throws DataSetParamValueConverterException
	{
		if (paramValues == null)
			return null;

		Map<String, Object> re = new HashMap<>(paramValues.size());

		for (DataSetParam param : params)
		{
			String name = param.getName();

			if (!paramValues.containsKey(name))
				continue;

			Object value = paramValues.get(name);
			value = convert(param, value);

			re.put(name, value);
		}

		return re;
	}

	/**
	 * 转换参数值。
	 * 
	 * @param param
	 * @param value
	 *            允许{@code null}
	 * @return
	 */
	public Object convert(DataSetParam param, Object value) throws DataSetParamValueConverterException
	{
		if (value == null)
			return null;

		DataType dataType = param.getType();

		if (DataType.isString(dataType))
			return convertToType(param, value, String.class);
		else if (DataType.isBoolean(dataType))
			return convertToType(param, value, Boolean.class);
		else if (DataType.isInteger(dataType))
			return convertToType(param, value, Long.class);
		else if (DataType.isDecimal(dataType))
			return convertToType(param, value, Double.class);
		else if (DataType.isDate(dataType))
			return convertToType(param, value, java.sql.Date.class);
		else if (DataType.isTime(dataType))
			return convertToType(param, value, java.sql.Time.class);
		else if (DataType.isTimestamp(dataType))
			return convertToType(param, value, java.sql.Timestamp.class);
		else
			throw new UnsupportedOperationException();
	}

	protected Object convertToType(DataSetParam param, Object src, Class<?> type)
			throws DataSetParamValueConverterException
	{
		try
		{
			return this.conversionService.convert(src, type);
		}
		catch (ConversionException e)
		{
			throw new DataSetParamValueConverterException(param, src, e);
		}
	}
}
