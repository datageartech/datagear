/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.util.IOUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * 抽象JSON数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonDataSet extends AbstractResolvableDataSet implements ResolvableDataSet
{
	public AbstractJsonDataSet()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public AbstractJsonDataSet(String id, String name)
	{
		super(id, name, Collections.EMPTY_LIST);
	}

	public AbstractJsonDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	/**
	 * 解析结果。
	 * <p>
	 * 如果{@linkplain #getJsonReader(Map)}返回的{@linkplain TemplateResolvedSource#hasResolvedTemplate()}，
	 * 此方法将返回{@linkplain TemplateResolvedDataSetResult}。
	 * </p>
	 */
	@Override
	protected ResolvedDataSetResult resolveResult(Map<String, ?> paramValues, List<DataSetProperty> properties)
			throws DataSetException
	{
		TemplateResolvedSource<Reader> reader = null;
		try
		{
			reader = getJsonReader(paramValues);

			ResolvedDataSetResult result = resolveResult(reader.getSource(), properties);

			if (reader.hasResolvedTemplate())
				result = new TemplateResolvedDataSetResult(result.getResult(), result.getProperties(),
						reader.getResolvedTemplate());

			return result;
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}
		finally
		{
			IOUtil.close(reader.getSource());
		}
	}

	/**
	 * 获取JSON输入流。
	 * <p>
	 * 实现方法应该返回实例级不变的输入流。
	 * </p>
	 * 
	 * @param paramValues
	 * @return
	 * @throws Throwable
	 */
	protected abstract TemplateResolvedSource<Reader> getJsonReader(Map<String, ?> paramValues) throws Throwable;

	/**
	 * 解析结果。
	 * 
	 * @param jsonReader
	 *            JSON输入流
	 * @param properties
	 *            允许为{@code null}，此时会自动解析
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(Reader jsonReader, List<DataSetProperty> properties) throws Throwable
	{
		boolean resolveProperties = (properties == null || properties.isEmpty());

		JsonNode jsonNode = getObjectMapperNonStardand().readTree(jsonReader);

		if (jsonNode == null || !isLegalResultDataJsonNode(jsonNode))
			throw new UnsupportedJsonResultDataException("Result data must be object or object array/list");

		Object data = getObjectMapperNonStardand().treeToValue(jsonNode, Object.class);

		if (resolveProperties)
			properties = resolveDataSetProperties(data);

		if (!resolveProperties)
			data = convertJsonResultData(data, properties, createDataSetPropertyValueConverter());

		DataSetResult result = new DataSetResult(data);

		return new ResolvedDataSetResult(result, properties);
	}

	protected Object convertJsonResultData(Object resultData, List<DataSetProperty> properties,
			DataSetPropertyValueConverter converter) throws Throwable
	{
		Object re = null;

		// JSON对象
		if (resultData == null)
		{
			re = null;
		}
		else if (resultData instanceof Map<?, ?>)
		{
			Map<String, Object> reMap = new HashMap<>();

			@SuppressWarnings("unchecked")
			Map<String, Object> source = (Map<String, Object>) resultData;

			for (Map.Entry<String, Object> entry : source.entrySet())
			{
				String name = entry.getKey();
				Object value = entry.getValue();

				DataSetProperty property = getDataNameTypeByName(properties, name);

				value = convertToPropertyDataType(converter, value, property);

				reMap.put(name, value);
			}

			re = reMap;
		}
		else if (resultData instanceof List<?>)
		{
			List<?> list = (List<?>) resultData;

			List<Object> reList = new ArrayList<>(list.size());

			for (Object ele : list)
				reList.add(convertJsonResultData(ele, properties, converter));

			re = reList;
		}
		else if (resultData instanceof Object[])
		{
			Object[] array = (Object[]) resultData;

			Object[] reArray = new Object[array.length];

			for (int i = 0; i < array.length; i++)
				reArray[i] = convertJsonResultData(array[i], properties, converter);

			re = reArray;
		}
		else
			throw new UnsupportedJsonResultDataException("Result data must be object or object array/list");

		return re;
	}

	/**
	 * 是否是合法的数据集结果数据{@linkplain JsonNode}。
	 * <p>
	 * 参考{@linkplain DataSetResult#getData()}说明。
	 * </p>
	 * 
	 * @param jsonNode
	 * @return
	 */
	protected boolean isLegalResultDataJsonNode(JsonNode jsonNode) throws Throwable
	{
		if (jsonNode == null || jsonNode.isNull())
			return true;

		if (jsonNode instanceof ValueNode)
			return false;

		if (jsonNode instanceof ArrayNode)
		{
			ArrayNode arrayNode = (ArrayNode) jsonNode;

			for (int i = 0; i < arrayNode.size(); i++)
			{
				JsonNode eleNode = arrayNode.get(i);

				if (eleNode == null || eleNode.isNull())
					continue;

				if (!(eleNode instanceof ObjectNode))
					return false;
			}
		}

		return true;
	}

	/**
	 * 解析JSON对象的{@linkplain DataSetProperty}。
	 * 
	 * @param resultData
	 *            JSON对象、JSON对象数组、JSON对象列表
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	protected List<DataSetProperty> resolveDataSetProperties(Object resultData) throws Throwable
	{
		if (resultData == null)
		{
			return Collections.EMPTY_LIST;
		}
		else if (resultData instanceof Map<?, ?>)
		{
			return resolveJsonObjDataSetProperties((Map<String, ?>) resultData);
		}
		else if (resultData instanceof List<?>)
		{
			List<?> list = (List<?>) resultData;

			if (list.size() == 0)
				return Collections.EMPTY_LIST;
			else
				return resolveJsonObjDataSetProperties((Map<String, ?>) list.get(0));
		}
		else if (resultData instanceof Object[])
		{
			Object[] array = (Object[]) resultData;

			if (array.length == 0)
				return Collections.EMPTY_LIST;
			else
				return resolveJsonObjDataSetProperties((Map<String, ?>) array[0]);
		}
		else
			throw new UnsupportedJsonResultDataException("Result data must be object or object array/list");
	}

	/**
	 * 解析JSON对象的{@linkplain DataSetProperty}。
	 * 
	 * @param jsonObj
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveJsonObjDataSetProperties(Map<String, ?> jsonObj) throws Throwable
	{
		List<DataSetProperty> properties = new ArrayList<>();

		if (jsonObj == null)
		{

		}
		else
		{
			for (Map.Entry<String, ?> entry : jsonObj.entrySet())
			{
				Object value = entry.getValue();
				String type = DataSetProperty.DataType.resolveDataType(value);

				DataSetProperty property = new DataSetProperty(entry.getKey(), type);

				// JSON数值只有NUMBER类型
				if (DataSetProperty.DataType.INTEGER.equals(property.getType())
						|| DataSetProperty.DataType.DECIMAL.equals(property.getType()))
					property.setType(DataSetProperty.DataType.NUMBER);

				properties.add(property);
			}
		}

		return properties;
	}

	protected ObjectMapper getObjectMapperNonStardand()
	{
		return JsonSupport.getObjectMapperNonStardand();
	}
}
