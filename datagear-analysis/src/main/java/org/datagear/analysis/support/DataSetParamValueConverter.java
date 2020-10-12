/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.Collection;
import java.util.Map;

import org.datagear.analysis.DataNameType;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetParam.DataType;

/**
 * {@linkplain DataSetParam}值转换器。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamValueConverter extends DataValueConverter
{
	public DataSetParamValueConverter()
	{
		super();
	}

	/**
	 * 转换参数值映射表，返回一个经转换的新映射表。
	 * <p>
	 * 注意，此方法必须遵循如下规则：如果{@code paramValues}中有未在{@code dataSetParams}中定义的项，那么它应原样写入返回映射表中。
	 * 因为：对于支持<code>Freemarker</code>的{@linkplain DataSet}实现类（比如：{@linkplain SqlDataSet}），
	 * 存在不定义{@linkplain DataSet#getParams()}而传递参数给内部<code>Freemarker</code>模板的应用场景。
	 * </p>
	 */
	@Override
	public Map<String, Object> convert(Map<String, ?> paramValues, Collection<? extends DataNameType> dataSetParams)
			throws DataValueConvertionException
	{
		return super.convert(paramValues, dataSetParams);
	}

	@Override
	protected Object convertValue(Object value, String type) throws DataValueConvertionException
	{
		if (DataType.STRING.equals(type))
			return convertToString(value, DataType.STRING);
		else if (DataType.NUMBER.equals(type))
			return convertToNumber(value, DataType.NUMBER);
		else if (DataType.BOOLEAN.equals(type))
			return convertToBoolean(value, DataType.BOOLEAN);
		else
			return convertExt(value, type);
	}
}
