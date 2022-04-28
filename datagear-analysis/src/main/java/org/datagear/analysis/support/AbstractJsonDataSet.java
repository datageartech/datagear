/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.TemplateResolvedResource.ResoureData;
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
	 * 如果{@linkplain #getJsonResource(DataSetQuery)}返回的{@linkplain JsonTemplateResolvedResource#hasResolvedTemplate()}，
	 * 此方法将返回{@linkplain TemplateResolvedDataSetResult}。
	 * </p>
	 */
	@Override
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws DataSetException
	{
		JsonTemplateResolvedResource resource = null;
		try
		{
			resource = getJsonResource(query);
			JsonResourceData jsonResourceData = resolveJsonResourceData(resource);

			ResolvedDataSetResult result = resolveResult(query, jsonResourceData, properties, resolveProperties);

			if (resource.hasResolvedTemplate())
				result = new TemplateResolvedDataSetResult(result.getResult(), result.getProperties(),
						resource.getResolvedTemplate());

			return result;
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t, (resource == null ? null : resource.getResolvedTemplate()));
		}
	}

	/**
	 * 获取{@linkplain JsonTemplateResolvedResource}。
	 * <p>
	 * 返回的{@linkplain JsonTemplateResolvedResource#getDataJsonPath()}应是{@linkplain #getDataJsonPath()}。
	 * </p>
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract JsonTemplateResolvedResource getJsonResource(DataSetQuery query) throws Throwable;

	/**
	 * 解析JSON数据。
	 * 
	 * @param resource
	 * @return
	 * @throws Throwable
	 */
	protected JsonResourceData resolveJsonResourceData(JsonTemplateResolvedResource resource) throws Throwable
	{
		Reader reader = null;

		try
		{
			reader = resource.getResource();

			Object data = resolveData(reader, resource.getDataJsonPath());
			List<DataSetProperty> properties = resolveProperties(data);

			return new JsonResourceData(data, properties);
		}
		finally
		{
			IOUtil.close(reader);
		}
	}

	/**
	 * 解析数据。
	 * 
	 * @param jsonReader
	 * @param dataJsonPath
	 * @return
	 * @throws ReadJsonDataPathException
	 * @throws Throwable
	 */
	protected Object resolveData(Reader jsonReader, String dataJsonPath)
			throws ReadJsonDataPathException, Throwable
	{
		JsonNode jsonNode = getObjectMapperNonStardand().readTree(jsonReader);
	
		if (!isLegalDataJsonNode(jsonNode))
			throw new UnsupportedJsonResultDataException("Result data must be JSON object or array");
	
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
	
		return data;
	}

	/**
	 * 是否是合法的数据{@linkplain JsonNode}。
	 * <p>
	 * 参考{@linkplain DataSetResult#getData()}说明。
	 * </p>
	 * 
	 * @param jsonNode 允许为{@code null}
	 * @return
	 */
	protected boolean isLegalDataJsonNode(JsonNode jsonNode) throws Throwable
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
	 * @param data 允许为{@code null}，JSON对象、JSON对象数组、JSON对象列表
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	protected List<DataSetProperty> resolveProperties(Object data) throws Throwable
	{
		if (data == null)
		{
			return Collections.EMPTY_LIST;
		}
		else if (data instanceof Map<?, ?>)
		{
			return resolveJsonObjProperties((Map<String, ?>) data);
		}
		else if (data instanceof List<?>)
		{
			List<?> list = (List<?>) data;

			if (list.size() == 0)
				return Collections.EMPTY_LIST;
			else
				return resolveJsonObjProperties((Map<String, ?>) list.get(0));
		}
		else if (data instanceof Object[])
		{
			Object[] array = (Object[]) data;

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

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param jsonResourceData
	 * @param properties        允许为{@code null}
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(DataSetQuery query,
			JsonResourceData jsonResourceData, List<DataSetProperty> properties, boolean resolveProperties)
			throws Throwable
	{
		List<DataSetProperty> resProperties = jsonResourceData.getProperties();
		Object resData = jsonResourceData.getData();

		if (resolveProperties)
			properties = mergeDataSetProperties(resProperties, properties);

		return resolveResult(resData, properties, query.getResultFetchSize(), query.getResultDataFormat());
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

	protected static abstract class JsonTemplateResolvedResource extends TemplateResolvedResource<Reader>
	{
		private static final long serialVersionUID = 1L;

		private final String dataJsonPath;

		public JsonTemplateResolvedResource(String resolvedTemplate, String dataJsonPath)
		{
			super(resolvedTemplate);
			this.dataJsonPath = dataJsonPath;
		}

		public String getDataJsonPath()
		{
			return dataJsonPath;
		}

		@Override
		public boolean isIdempotent()
		{
			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((dataJsonPath == null) ? 0 : dataJsonPath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			JsonTemplateResolvedResource other = (JsonTemplateResolvedResource) obj;
			if (dataJsonPath == null)
			{
				if (other.dataJsonPath != null)
					return false;
			}
			else if (!dataJsonPath.equals(other.dataJsonPath))
				return false;
			return true;
		}
	}

	protected static class JsonResourceData extends ResoureData<Object>
	{
		private static final long serialVersionUID = 1L;

		private final List<DataSetProperty> properties;

		public JsonResourceData(Object data, List<DataSetProperty> properties)
		{
			super(data);
			this.properties = (properties == null ? Collections.emptyList()
					: Collections.unmodifiableList(properties));
		}

		/**
		 * 获取属性列表。
		 * <p>
		 * 返回值及其内容不应被修改，因为可能会缓存。
		 * </p>
		 * 
		 * @return
		 */
		public List<DataSetProperty> getProperties()
		{
			return properties;
		}
	}
}
