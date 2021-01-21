/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.DataNameType;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;

/**
 * 抽象{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataSet extends AbstractIdentifiable implements DataSet
{
	/** 默认Freemarker模板解析器 */
	public static final DataSetFmkTemplateResolver FMK_TEMPLATE_RESOLVER = new DataSetFmkTemplateResolver();

	private String name;

	private List<DataSetProperty> properties;

	@SuppressWarnings("unchecked")
	private List<DataSetParam> params = Collections.EMPTY_LIST;

	/** 属性数据转换格式 */
	private DataFormat propertyDataFormat = null;

	public AbstractDataSet()
	{
		super();
		setPropertyDataFormat(new DataFormat());
	}

	public AbstractDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id);
		this.name = name;
		this.properties = properties;
		setPropertyDataFormat(new DataFormat());
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

	public DataFormat getPropertyDataFormat()
	{
		return propertyDataFormat;
	}

	/**
	 * 设置属性数据转换格式。
	 * <p>
	 * 当{@linkplain DataSetProperty#getType()}不是结果数据的原始类型，而需要进行类型转换时，需要使用数据转换格式进行转换。
	 * </p>
	 * 
	 * @param propertyDataFormat
	 */
	public void setPropertyDataFormat(DataFormat propertyDataFormat)
	{
		this.propertyDataFormat = propertyDataFormat;
	}

	@Override
	public boolean isReady(Map<String, ?> paramValues)
	{
		if (!hasParam())
			return true;

		List<DataSetParam> params = getParams();

		for (DataSetParam param : params)
		{
			if (param.isRequired() && !paramValues.containsKey(param.getName()))
				return false;
		}

		return true;
	}

	/**
	 * 将源对象转换为指定{@linkplain DataSetProperty.DataType}类型的对象。
	 * <p>
	 * 如果{@code property}为{@code null}，则什么也不做直接返回。
	 * </p>
	 * 
	 * @param converter
	 * @param source
	 * @param property
	 *            允许为{@code null}
	 * @return
	 */
	protected Object convertToPropertyDataType(DataSetPropertyValueConverter converter, Object source,
			DataSetProperty property)
	{
		if (property == null)
			return source;

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
		DataFormat dataFormat = this.propertyDataFormat;
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
		if (list != null)
		{
			for (T t : list)
			{
				if (t.getName().equals(name))
					return t;
			}
		}

		return null;
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
	 * 将指定文本作为Freemarker模板解析。
	 * <p>
	 * 注意：即使此数据集没有定义任何参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * 用户不定义数据集参数，但却定义模板内容，之后用户自行在DataSet.getResult(Map&lt;String,?&gt;)参数映射表中传递模板内容所须的参数值。
	 * </p>
	 * 
	 * @param text
	 * @param paramValues
	 * @return
	 */
	public String resolveAsFmkTemplate(String text, Map<String, ?> paramValues)
	{
		// if (!hasParam())
		// return text;

		if (text == null)
			return null;

		return FMK_TEMPLATE_RESOLVER.resolve(text, paramValues);
	}
}
