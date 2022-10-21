/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.DataNameType;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.ResultDataFormat;

/**
 * 抽象{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataSet extends AbstractIdentifiable implements DataSet
{
	public static final DataSetFmkTemplateResolver GENERAL_TEMPLATE_RESOLVER = new DataSetFmkTemplateResolver();

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
		return getDataNameTypeByName(this.properties, name);
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
		return getDataNameTypeByName(this.params, name);
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
		ResultDataFormatter formatter = (format == null ? null : new ResultDataFormatter(format));

		int dataSize = (fetchSize >= 0 ? fetchSize : rawData.size());
		List<Map<String, Object>> data = new ArrayList<>(dataSize);

		int plen = properties.size();

		Object[] defaultValues = new Object[plen];
		Object dvPlaceholder = new Object();
		Arrays.fill(defaultValues, dvPlaceholder);

		for (Map<String, ?> rowRaw : rawData)
		{
			if (data.size() >= dataSize)
				break;

			// 易变模型应保留所有原始数据
			Map<String, Object> row = (isMutableModel() ? new HashMap<>(rowRaw) : new HashMap<>());

			for (int j = 0; j < plen; j++)
			{
				DataSetProperty property = properties.get(j);

				String name = property.getName();
				Object value = rowRaw.get(name);
				value = convertToPropertyDataType(converter, value, property);

				if (value == null)
				{
					if (defaultValues[j] == dvPlaceholder)
					{
						Object defaultValue = property.getDefaultValue();
						defaultValues[j] = convertToPropertyDataType(converter, defaultValue, property);
					}

					value = defaultValues[j];
				}

				if (formatter != null)
					value = formatter.format(value);

				row.put(name, value);
			}

			data.add(row);
		}

		return data;
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
		List<DataSetProperty> re = new ArrayList<>(names.size());

		for (int i = 0, len = names.size(); i < len; i++)
		{
			DataSetProperty dp = null;

			for (DataSetProperty dataSetProperty : dataSetProperties)
			{
				if (names.get(i).equals(dataSetProperty.getName()))
				{
					dp = dataSetProperty;
					break;
				}
			}

			re.add(dp);
		}

		return re;
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

		return convertToPropertyDataType(converter, source, property.getType());
	}

	/**
	 * 将源对象转换为指定{@linkplain DataSetProperty.DataType}类型的对象。
	 * <p>
	 * 如果{@code propertyType}为{@code null}，则什么也不做直接返回。
	 * </p>
	 * 
	 * @param converter
	 * @param source
	 * @param propertyType
	 *            允许为{@code null}
	 * @return
	 */
	protected Object convertToPropertyDataType(DataSetPropertyValueConverter converter, Object source,
			String propertyType)
	{
		if (propertyType == null || DataSetProperty.DataType.UNKNOWN.equals(propertyType))
			return source;

		return converter.convert(source, propertyType);
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

		return new DataSetPropertyValueConverter(dataFormat);
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

	/**
	 * 获取指定名称的{@linkplain DataNameType}对象，没找到则返回{@code null}。
	 * 
	 * @param <T>
	 * @param list
	 *            允许为{@code null}
	 * @param name
	 * @return
	 */
	protected <T extends DataNameType> T getDataNameTypeByName(List<T> list, String name)
	{
		int index = getDataNameTypeIndexByName(list, name);
		return (index < 0 ? null : list.get(index));
	}

	/**
	 * 获取指定名称的{@linkplain DataNameType}的索引。
	 * 
	 * @param <T>
	 * @param list
	 *            允许为{@code null}
	 * @param name
	 * @return
	 */
	protected <T extends DataNameType> int getDataNameTypeIndexByName(List<T> list, String name)
	{
		if (list == null)
			list = Collections.emptyList();

		for (int i = 0, len = list.size(); i < len; i++)
		{
			if (name.equals(list.get(i).getName()))
				return i;
		}

		return -1;
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
	 * 将指定文本作为通用模板解析（没有特定语境，比如：SQL、CSV、JSON）。
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	protected String resolveTextAsGeneralTemplate(String text, DataSetQuery query)
	{
		return resolveTextAsTemplate(GENERAL_TEMPLATE_RESOLVER, text, query);
	}

	/**
	 * 将指定文本作为模板解析。
	 * <p>
	 * 注意：即使此数据集没有定义任何参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param templateResolver
	 * @param text
	 * @param query
	 * @return
	 */
	protected String resolveTextAsTemplate(TemplateResolver templateResolver, String text, DataSetQuery query)
	{
		if (text == null)
			return null;

		Map<String, ?> values = query.getParamValues();

		return templateResolver.resolve(text, new TemplateContext(values));
	}
}
