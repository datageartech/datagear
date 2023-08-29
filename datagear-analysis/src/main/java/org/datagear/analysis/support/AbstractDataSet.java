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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.NameAwareUtil;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.ResultDataFormat;
import org.datagear.analysis.support.DataSetPropertyExpEvaluator.ValueSetter;

/**
 * 抽象{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataSet extends AbstractIdentifiable implements DataSet, Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;

	private boolean mutableModel = false;

	private List<DataSetProperty> properties = Collections.emptyList();

	private List<DataSetParam> params = Collections.emptyList();

	/** 数据格式 */
	private DataFormat dataFormat = new DataFormat();

	public AbstractDataSet()
	{
		super();
	}

	public AbstractDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id);
		this.name = name;
		this.properties = properties;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean isMutableModel()
	{
		return mutableModel;
	}

	public void setMutableModel(boolean mutableModel)
	{
		this.mutableModel = mutableModel;
	}

	@Override
	public List<DataSetProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<DataSetProperty> properties)
	{
		this.properties = properties;
	}

	@Override
	public DataSetProperty getProperty(String name)
	{
		return NameAwareUtil.find(this.properties, name);
	}

	public boolean hasParam()
	{
		return (this.params != null && !this.params.isEmpty());
	}

	@Override
	public List<DataSetParam> getParams()
	{
		return params;
	}

	public void setParams(List<DataSetParam> params)
	{
		this.params = params;
	}

	@Override
	public DataSetParam getParam(String name)
	{
		return NameAwareUtil.find(this.params, name);
	}

	/**
	 * 获取数据格式。
	 * 
	 * @return
	 */
	public DataFormat getDataFormat()
	{
		return dataFormat;
	}

	/**
	 * 设置数据格式。
	 * <p>
	 * 当数据集属性{@linkplain #getProperties()}的{@linkplain DataSetProperty#getType()}与底层数据源（数据库、CSV、JSON等）不匹配时，
	 * 可设置此数据格式，用于支持类型转换。
	 * </p>
	 * 
	 * @param dataFormat
	 */
	public void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;
	}

	/**
	 * 校验{@linkplain DataSetQuery#getParamValues()}是否有缺失的必填项。
	 * 
	 * @param query
	 * @throws DataSetParamValueRequiredException
	 */
	protected void checkRequiredParamValues(DataSetQuery query) throws DataSetParamValueRequiredException
	{
		if (!hasParam())
			return;

		List<DataSetParam> params = getParams();
		Map<String, ?> paramValues = query.getParamValues();
		
		for (DataSetParam param : params)
		{
			if (param.isRequired() && !paramValues.containsKey(param.getName()))
				throw new DataSetParamValueRequiredException(
						"Parameter [" + param.getName() + "] 's value is required");
		}
	}
	
	/**
	 * 解析结果数据。
	 * 
	 * @param rawData    {@code Collection<Map<String, ?>>}、{@code Map<String, ?>[]}、{@code Map<String, ?>}、{@code null}
	 * @param properties
	 * @param fetchSize  获取条数，小于{@code 0}表示全部
	 * @param format     允许为{@code null}
	 * @return {@code List<Map<String, ?>>}、{@code Map<String, ?>[]}、{@code Map<String, ?>}、{@code null}
	 * @throws Throwable
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object resolveResultData(Object rawData, List<DataSetProperty> properties,
			int fetchSize, ResultDataFormat format) throws Throwable
	{
		Object data = null;

		if (rawData == null)
		{

		}
		else if (rawData instanceof Collection<?>)
		{
			Collection<Map<String, ?>> rawCollection = (Collection<Map<String, ?>>) rawData;

			data = convertRawDataToResult(rawCollection, properties, fetchSize, format);
		}
		else if (rawData instanceof Map<?, ?>[])
		{
			Map<?, ?>[] rawArray = (Map<?, ?>[]) rawData;
			List<Map<String, ?>> rawCollection = (List) Arrays.asList(rawArray);
			List<Map<String, Object>> dataList = convertRawDataToResult(rawCollection, properties, fetchSize, format);

			data = dataList.toArray(new Map<?, ?>[dataList.size()]);
		}
		else if (rawData instanceof Map<?, ?>)
		{
			Map<?, ?> rawMap = (Map<?, ?>) rawData;
			List<Map<String, ?>> rawCollection = (List) Arrays.asList(rawMap);
			List<Map<String, Object>> dataList = convertRawDataToResult(rawCollection, properties, fetchSize, format);

			data = dataList.get(0);
		}
		else
			throw new UnsupportedOperationException(
					"Unsupported raw data type : " + rawData.getClass().getSimpleName());

		return data;
	}

	/**
	 * 解析结果。
	 * 
	 * @param rawData    允许为{@code null}
	 * @param properties
	 * @param fetchSize  获取条数，小于{@code 0}表示全部
	 * @param format     允许为{@code null}
	 * @return
	 * @throws Throwable
	 * @see {@link #resolveResultData(Object, List, ResultDataFormat)}
	 */
	protected ResolvedDataSetResult resolveResult(Object rawData, List<DataSetProperty> properties,
			int fetchSize, ResultDataFormat format) throws Throwable
	{
		Object data = resolveResultData(rawData, properties, fetchSize, format);
		return new ResolvedDataSetResult(new DataSetResult(data), properties);
	}

	/**
	 * 转换原始数据。
	 * 
	 * @param rawData
	 * @param properties
	 * @param fetchSize  获取条数，小于{@code 0}表示全部
	 * @param format     允许为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, Object>> convertRawDataToResult(Collection<? extends Map<String, ?>> rawData,
			List<DataSetProperty> properties, int fetchSize, ResultDataFormat format) throws Throwable
	{
		DataSetPropertyValueConverter converter = createDataSetPropertyValueConverter();
		List<Object> defaultValues = getDefaultValues(properties, converter);

		int dataSize = (fetchSize >= 0 ? fetchSize : rawData.size());
		List<Map<String, Object>> data = new ArrayList<>(dataSize);

		int plen = properties.size();

		for (Map<String, ?> rowRaw : rawData)
		{
			if (data.size() >= dataSize)
				break;

			// 易变模型应保留所有原始数据
			Map<String, Object> row = (isMutableModel() ? new HashMap<>(rowRaw) : new HashMap<>());

			for (int i = 0; i < plen; i++)
			{
				DataSetProperty property = properties.get(i);

				String name = property.getName();
				Object value = rowRaw.get(name);
				value = convertToPropertyDataType(converter, value, property);
				
				//无论是否计算属性，这里都应设置默认值
				if(value == null)
					value = defaultValues.get(i);

				row.put(name, value);
			}

			data.add(row);
		}
		
		// 计算表达式
		evalResultData(data, properties, defaultValues, converter);
		
		// 格式化，应是最后步骤
		formatResultData(data, properties, format);
		
		return data;
	}
	
	protected void evalResultData(List<Map<String, Object>> data, List<DataSetProperty> properties,
			List<Object> defaultValues, DataSetPropertyValueConverter converter)
	{
		DataSetPropertyExpEvaluator evaluator = getDataSetPropertyExpEvaluator();
		
		evaluator.eval(properties, data, new ValueSetter<Map<String, Object>>()
		{
			@Override
			public void set(DataSetProperty property, int propertyIndex, Map<String, Object> data, Object value)
			{
				value = convertToPropertyDataType(converter, value, property);
				
				if (value == null)
					value = defaultValues.get(propertyIndex);
				
				data.put(property.getName(), value);
			}
		});
	}
	
	protected void formatResultData(List<Map<String, Object>> data, List<DataSetProperty> properties, ResultDataFormat format)
	{
		if(format == null)
			return;
		
		ResultDataFormatter formatter = new ResultDataFormatter(format);
		int plen = properties.size();

		for (Map<String, Object> row : data)
		{
			for (int i = 0; i < plen; i++)
			{
				DataSetProperty property = properties.get(i);
				String name = property.getName();
				Object value = row.get(name);
				Object fv = formatter.format(value);
				
				if(fv != value)
					row.put(name, fv);
			}
		}
	}

	protected DataSetPropertyExpEvaluator getDataSetPropertyExpEvaluator()
	{
		return DataSetPropertyExpEvaluator.DEFAULT;
	}

	protected List<Object> getDefaultValues(List<DataSetProperty> properties,
			DataSetPropertyValueConverter converter)
	{
		List<Object> defaultValues = new ArrayList<Object>(properties.size());

		for (DataSetProperty p : properties)
		{
			Object defaultValue = p.getDefaultValue();
			defaultValue = convertToPropertyDataType(converter, defaultValue, p);
			defaultValues.add(defaultValue);
		}

		return defaultValues;
	}

	/**
	 * 给定数目是否已到达{@linkplain DataSetQuery#getResultFetchSize()}。
	 * 
	 * @param query
	 *            允许为{@code null}
	 * @param count
	 * @return
	 */
	protected boolean isReachResultFetchSize(DataSetQuery query, int count)
	{
		if (query == null)
			return false;

		int maxCount = query.getResultFetchSize();

		if (maxCount < 0)
			return false;

		return count >= maxCount;
	}

	/**
	 * 查找与名称数组对应的{@linkplain DataSetProperty}列表。
	 * <p>
	 * 如果{@code names}某元素没有对应的{@linkplain DataSetProperty}，返回列表对应元素位置将为{@code null}。
	 * </p>
	 * 
	 * @param dataSetProperties
	 * @param names
	 * @return
	 */
	protected List<DataSetProperty> findDataSetProperties(List<DataSetProperty> dataSetProperties, String[] names)
	{
		return findDataSetProperties(dataSetProperties, Arrays.asList(names));
	}

	/**
	 * 查找与名称数组对应的{@linkplain DataSetProperty}列表。
	 * <p>
	 * 如果{@code names}某元素没有对应的{@linkplain DataSetProperty}，返回列表对应元素位置将为{@code null}。
	 * </p>
	 * 
	 * @param dataSetProperties
	 * @param names
	 * @return
	 */
	protected List<DataSetProperty> findDataSetProperties(List<DataSetProperty> dataSetProperties, List<String> names)
	{
		return NameAwareUtil.finds(dataSetProperties, names);
	}

	/**
	 * 将源对象转换为指定{@linkplain DataSetProperty.DataType}类型的对象。
	 * <p>
	 * 如果{@code property}为{@code null}，则什么也不做直接返回。
	 * </p>
	 * 
	 * @param converter
	 * @param source
	 *            允许为{@code null}
	 * @param property
	 *            允许为{@code null}
	 * @return
	 */
	protected Object convertToPropertyDataType(DataSetPropertyValueConverter converter, Object source,
			DataSetProperty property)
	{
		if (property == null)
			return source;

		if (source == null)
			return null;
		
		String propertyType = property.getType();
		
		if (propertyType == null || DataSetProperty.DataType.UNKNOWN.equals(propertyType))
			return source;

		return converter.convert(source, property);
	}

	/**
	 * 创建一个{@linkplain DataSetPropertyValueConverter}实例。
	 * <p>
	 * 由于{@linkplain DataSetPropertyValueConverter}不是线程安全的，所以每次使用时要手动创建。
	 * </p>
	 * 
	 * @return
	 */
	protected DataSetPropertyValueConverter createDataSetPropertyValueConverter()
	{
		DataFormat dataFormat = getDataFormat();
		if (dataFormat == null)
			dataFormat = new DataFormat();

		DataSetPropertyValueConverter converter = new DataSetPropertyValueConverter(dataFormat);

		// 这里应设为true，可避免精度丢失，同时可保留BigDecimal的原始小数位数
		converter.setIgnoreBigIntegerToInteger(true);
		converter.setIgnoreBigDecimalToDecimal(true);

		return converter;
	}

	/**
	 * 解析{@linkplain DataSetProperty.DataType}类型。
	 * 
	 * @param value
	 * @return
	 */
	protected String resolvePropertyDataType(Object value)
	{
		return DataSetProperty.DataType.resolveDataType(value);
	}
	
	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> listRowsToMapRows(List<List<Object>> data, List<DataSetProperty> properties)
	{
		if (data == null)
			return Collections.EMPTY_LIST;

		int plen = properties.size();

		List<Map<String, Object>> maps = new ArrayList<>(data.size());

		for (List<Object> row : data)
		{
			Map<String, Object> map = new HashMap<>();

			for (int i = 0; i < Math.min(plen, row.size()); i++)
			{
				String name = properties.get(i).getName();
				map.put(name, row.get(i));
			}

			maps.add(map);
		}

		return maps;
	}

	/**
	 * 解析模板：普通文本。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	protected String resolveTemplatePlain(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolvePlain(text, toTemplateContext(query));
	}

	/**
	 * 解析模板：CSV。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	protected String resolveTemplateCsv(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolveCsv(text, toTemplateContext(query));
	}

	/**
	 * 解析模板：JSON。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	protected String resolveTemplateJson(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolveJson(text, toTemplateContext(query));
	}

	/**
	 * 解析模板：SQL。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	protected String resolveTemplateSql(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolveSql(text, toTemplateContext(query));
	}

	/**
	 * 解析模板：XML。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	protected String resolveTemplateXml(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolveXml(text, toTemplateContext(query));
	}

	/**
	 * 将{@linkplain DataSetQuery}转换为{@linkplain TemplateContext}。
	 * 
	 * @param query
	 * @return
	 */
	protected TemplateContext toTemplateContext(DataSetQuery query)
	{
		Map<String, ?> values = query.getParamValues();
		return new TemplateContext(values);
	}

	protected static class EvaludatedPropertiesInfo
	{
		private final List<DataSetProperty> properties;
		private final List<Object> defaultValues;

		public EvaludatedPropertiesInfo(List<DataSetProperty> properties, List<Object> defaultValues)
		{
			super();
			this.properties = properties;
			this.defaultValues = defaultValues;
		}

		public List<DataSetProperty> getProperties()
		{
			return properties;
		}

		public List<Object> getDefaultValues()
		{
			return defaultValues;
		}
	}
}
