/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.fmk.JsonOutputFormat;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * 抽象JSON数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonDataSet extends AbstractResolvableDataSet implements ResolvableDataSet
{
	private static final long serialVersionUID = 1L;

	public static final DataSetFmkTemplateResolver JSON_TEMPLATE_RESOLVER = new DataSetFmkTemplateResolver(
			JsonOutputFormat.INSTANCE);

	/** 使用Jackson的{@code JSONPath}配置 */
	protected static final Configuration JACKSON_JSON_PATH_CONFIGURATION = Configuration.builder()
			.jsonProvider(new JacksonJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();

	/** 数据JSON路径 */
	private String dataJsonPath = "";

	public AbstractJsonDataSet()
	{
		super();
	}

	public AbstractJsonDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractJsonDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	public String getDataJsonPath()
	{
		return dataJsonPath;
	}

	/**
	 * 设置数据JSON路径。
	 * <p>
	 * 当希望返回的是原始JSON数据的指定JSON路径值时，可以设置此项。
	 * </p>
	 * <p>
	 * 例如："stores[0].books"、"[1].stores"、"$['store']['book'][0]"、
	 * "$.store.book[*].author"、"$..book[2]"，具体参考{@code JSONPath}相关文档。
	 * </p>
	 * <p>
	 * 默认无数据路径，将直接返回原始JSON数据。
	 * </p>
	 * 
	 * @param dataJsonPath
	 */
	public void setDataJsonPath(String dataJsonPath)
	{
		this.dataJsonPath = dataJsonPath;
	}

	/**
	 * 解析结果。
	 * <p>
	 * 如果{@linkplain #getJsonReader(Map)}返回的{@linkplain TemplateResolvedSource#hasResolvedTemplate()}，
	 * 此方法将返回{@linkplain TemplateResolvedDataSetResult}。
	 * </p>
	 */
	@Override
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws DataSetException
	{
		TemplateResolvedSource<Reader> reader = null;
		try
		{
			reader = getJsonReader(query);

			ResolvedDataSetResult result = resolveResult(query, reader.getSource(), properties, resolveProperties);

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
			if (reader != null)
				IOUtil.close(reader.getSource());
		}
	}

	/**
	 * 获取JSON输入流。
	 * <p>
	 * 实现方法应该返回实例级不变的输入流。
	 * </p>
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract TemplateResolvedSource<Reader> getJsonReader(DataSetQuery query) throws Throwable;

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param jsonReader
	 *            JSON输入流
	 * @param properties
	 *            允许为{@code null}
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(DataSetQuery query,
			Reader jsonReader, List<DataSetProperty> properties, boolean resolveProperties) throws Throwable
	{
		JsonNode jsonNode = getObjectMapperNonStardand().readTree(jsonReader);

		if (!isLegalResultDataJsonNode(jsonNode))
			throw new UnsupportedJsonResultDataException("Result data must be JSON object or array");

		Object rawData = resolveRawData(query, jsonNode, getDataJsonPath());

		if (resolveProperties)
		{
			List<DataSetProperty> resolvedProperties = resolveProperties(rawData);
			mergeDataSetProperties(resolvedProperties, properties);
			properties = resolvedProperties;
		}

		return resolveResult(rawData, properties, query.getResultDataFormat());
	}

	/**
	 * 解析原始数据数据。
	 * 
	 * @param query
	 * @param jsonNode      允许为{@code null}
	 * @param dataJsonPath  允许为{@code null}
	 * @return
	 * @throws ReadJsonDataPathException
	 * @throws Throwable
	 */
	protected Object resolveRawData(DataSetQuery query, JsonNode jsonNode, String dataJsonPath)
			throws ReadJsonDataPathException, Throwable
	{
		if (jsonNode == null)
			return null;

		Object data = getObjectMapperNonStardand().treeToValue(jsonNode, Object.class);

		if (data != null && !StringUtil.isEmpty(dataJsonPath))
		{
			String stdDataJsonPath = dataJsonPath.trim();

			if (!StringUtil.isEmpty(stdDataJsonPath))
			{
				// 转换"stores[0].books"、"[1].stores"简化模式为规范的JSONPath
				if (!stdDataJsonPath.startsWith("$"))
				{
					if (stdDataJsonPath.startsWith("["))
						stdDataJsonPath = "$" + stdDataJsonPath;
					else
						stdDataJsonPath = "$." + stdDataJsonPath;
				}

				try
				{
					data = JsonPath.compile(stdDataJsonPath).read(data, JACKSON_JSON_PATH_CONFIGURATION);
				}
				catch(Throwable t)
				{
					throw new ReadJsonDataPathException(dataJsonPath, t);
				}
			}
		}

		if (data != null && hasResultFetchSize(query))
		{
			if (data instanceof Collection<?>)
			{
				Collection<?> collection = (List<?>) data;
				List<Object> dataList = new ArrayList<>();

				for (Object ele : collection)
				{
					if (isReachResultFetchSize(query, dataList.size()))
						break;

					dataList.add(ele);
				}

				data = dataList;
			}
			else if (data instanceof Object[])
			{
				Object[] array = (Object[]) data;
				Object[] dataArray = new Object[evalResultFetchSize(query, array.length)];

				for (int i = 0; i < dataArray.length; i++)
				{
					dataArray[i] = array[i];
				}

				data = dataArray;
			}
		}

		return data;
	}

	/**
	 * 是否是合法的数据集结果数据{@linkplain JsonNode}。
	 * <p>
	 * 参考{@linkplain DataSetResult#getData()}说明。
	 * </p>
	 * 
	 * @param jsonNode
	 *            允许为{@code null}
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
	 * 解析{@linkplain DataSetProperty}。
	 * 
	 * @param resultData 允许为{@code null}，JSON对象、JSON对象数组、JSON对象列表
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	protected List<DataSetProperty> resolveProperties(Object resultData) throws Throwable
	{
		if (resultData == null)
		{
			return Collections.EMPTY_LIST;
		}
		else if (resultData instanceof Map<?, ?>)
		{
			return resolveJsonObjProperties((Map<String, ?>) resultData);
		}
		else if (resultData instanceof List<?>)
		{
			List<?> list = (List<?>) resultData;

			if (list.size() == 0)
				return Collections.EMPTY_LIST;
			else
				return resolveJsonObjProperties((Map<String, ?>) list.get(0));
		}
		else if (resultData instanceof Object[])
		{
			Object[] array = (Object[]) resultData;

			if (array.length == 0)
				return Collections.EMPTY_LIST;
			else
				return resolveJsonObjProperties((Map<String, ?>) array[0]);
		}
		else
			throw new UnsupportedJsonResultDataException("Result data must be object or object array/list");
	}

	/**
	 * 解析{@linkplain DataSetProperty}。
	 * 
	 * @param jsonObj
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveJsonObjProperties(Map<String, ?> jsonObj) throws Throwable
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

	/**
	 * 将指定JSON文本作为模板解析。
	 * 
	 * @param json
	 * @param query
	 * @return
	 */
	protected String resolveJsonAsTemplate(String json, DataSetQuery query)
	{
		return resolveTextAsTemplate(JSON_TEMPLATE_RESOLVER, json, query);
	}
}
