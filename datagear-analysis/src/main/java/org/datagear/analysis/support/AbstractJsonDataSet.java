/*
 * Copyright 2018-present datagear.tech
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.support.AbstractJsonDataSet.JsonDataSetResource;
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
public abstract class AbstractJsonDataSet<T extends JsonDataSetResource> extends AbstractResolvableResourceDataSet<T>
		implements ResolvableDataSet
{
	private static final long serialVersionUID = 1L;

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

	public AbstractJsonDataSet(String id, String name, List<DataSetField> fields)
	{
		super(id, name, fields);
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
	protected ResourceData resolveResourceData(T resource, boolean resolveFields) throws Throwable
	{
		Reader reader = null;

		try
		{
			reader = resource.getReader();

			Object data = resolveData(reader, resource.getDataJsonPath());
			List<DataSetField> fields = null;

			if (resolveFields)
				fields = resolveFields(data);

			return new ResourceData(data, fields);
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
		data = getJsonPathSupport().resolve(data, dataJsonPath);
	
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
	 * 解析{@linkplain DataSetField}。
	 * <p>
	 * 注意：此方法只能识别{@linkplain Map}、{@code Collection<Map>}、{@code Map[]}类型的数据，其他类型将返回空列表。
	 * </p>
	 * 
	 * @param data
	 *            允许{@code null}
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	protected List<DataSetField> resolveFields(Object data) throws Throwable
	{
		if (data == null)
		{
			return Collections.emptyList();
		}
		else if (data instanceof Map<?, ?>)
		{
			return resolveJsonObjFields((Map<String, ?>) data);
		}
		else if (data instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) data;

			Object ele = null;

			for (Object obj : collection)
			{
				if (obj != null)
				{
					ele = obj;
					break;
				}
			}

			return resolveFields(ele);
		}
		else if (data instanceof Object[])
		{
			Object[] array = (Object[]) data;

			Object ele = null;

			for (Object obj : array)
			{
				if (obj != null)
				{
					ele = obj;
					break;
				}
			}

			return resolveFields(ele);
		}
		else
		{
			// 对于不支持的类型应返回空列表而非抛出异常
			return Collections.emptyList();
		}
	}

	/**
	 * 解析{@linkplain DataSetField}。
	 * 
	 * @param jsonObj
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetField> resolveJsonObjFields(Map<String, ?> jsonObj) throws Throwable
	{
		List<DataSetField> fields = new ArrayList<>();

		if (jsonObj == null)
		{

		}
		else
		{
			for (Map.Entry<String, ?> entry : jsonObj.entrySet())
			{
				Object value = entry.getValue();
				String type = DataSetField.DataType.resolveDataType(value);

				DataSetField field = new DataSetField(entry.getKey(), type);

				// JSON数值只有NUMBER类型
				if (DataSetField.DataType.INTEGER.equals(field.getType())
						|| DataSetField.DataType.DECIMAL.equals(field.getType()))
					field.setType(DataSetField.DataType.NUMBER);

				fields.add(field);
			}
		}

		return fields;
	}

	protected ObjectMapper getObjectMapperNonStardand()
	{
		return JsonSupport.getObjectMapperNonStardand();
	}

	protected JsonPathSupport getJsonPathSupport()
	{
		return JsonPathSupport.INSTANCE;
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
