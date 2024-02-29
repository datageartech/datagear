/*
 * Copyright 2018-2024 datagear.tech
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.support.AbstractJsonDataSet.JsonDataSetResource;
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
public abstract class AbstractJsonDataSet<T extends JsonDataSetResource> extends AbstractResolvableResourceDataSet<T>
		implements ResolvableDataSet
{
	private static final long serialVersionUID = 1L;

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

	@Override
	protected ResourceData resolveResourceData(T resource, boolean resolveProperties) throws Throwable
	{
		Reader reader = null;

		try
		{
			reader = resource.getReader();

			Object data = resolveData(reader, resource.getDataJsonPath());
			List<DataSetProperty> properties = null;

			if (resolveProperties)
				properties = resolveProperties(data);

			return new ResourceData(data, properties);
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

	protected ObjectMapper getObjectMapperNonStardand()
	{
		return JsonSupport.getObjectMapperNonStardand();
	}

	/**
	 * JSON数据集资源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static abstract class JsonDataSetResource extends DataSetResource
	{
		private static final long serialVersionUID = 1L;
		
		private String dataJsonPath;

		public JsonDataSetResource()
		{
			super();
		}

		public JsonDataSetResource(String resolvedTemplate, String dataJsonPath)
		{
			super(resolvedTemplate);
			this.dataJsonPath = dataJsonPath;
		}

		public String getDataJsonPath()
		{
			return dataJsonPath;
		}

		/**
		 * 获取JSON输入流。
		 * <p>
		 * 输入流应该在此方法内创建，而不应该在实例内创建，因为采用缓存后不会每次都调用此方法。
		 * </p>
		 * 
		 * @return
		 * @throws Throwable
		 */
		public abstract Reader getReader() throws Throwable;

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
			JsonDataSetResource other = (JsonDataSetResource) obj;
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
}
