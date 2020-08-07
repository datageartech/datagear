/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.io.StringReader;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * JSON {@linkplain DataSet}支持类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataSetSupport extends JsonSupport
{
	public JsonDataSetSupport()
	{
		super();
	}

	/**
	 * 解析JSON数据。
	 * 
	 * @param jsonValue
	 * @return
	 * @throws DataSetException
	 */
	public Object resolveValue(String jsonValue) throws DataSetException
	{
		StringReader reader = new StringReader(jsonValue);
		return resolveValue(reader);
	}

	/**
	 * 解析JSON数据。
	 * 
	 * @param jsonReader
	 * @return
	 * @throws DataSetException
	 */
	public Object resolveValue(Reader jsonReader) throws DataSetException
	{
		try
		{
			return parseNonStardand(jsonReader, Object.class);
		}
		catch(Throwable t)
		{
			throw new DataSetException(t);
		}
	}

	/**
	 * 解析数据集结果数据。
	 * 
	 * @param jsonValue
	 * @return
	 * @throws DataSetException
	 * @throws UnsupportedResultDataException
	 */
	public Object resolveResultData(String jsonValue) throws DataSetException, UnsupportedResultDataException
	{
		StringReader reader = new StringReader(jsonValue);
		return resolveResultData(reader);
	}

	/**
	 * 解析数据集结果数据。
	 * 
	 * @param reader
	 * @return
	 * @throws DataSetException
	 * @throws UnsupportedResultDataException
	 */
	public Object resolveResultData(Reader reader) throws DataSetException, UnsupportedResultDataException
	{
		JsonNode jsonNode = null;

		try
		{
			jsonNode = getObjectMapperNonStardand().readTree(reader);
		}
		catch (Throwable t)
		{
			throw new DataSetException(t);
		}

		if (!isLegalResultDataJsonNode(jsonNode))
			throw new UnsupportedResultDataException("Result data must be object or object array");

		if (jsonNode == null)
			return null;

		Object data = null;

		try
		{
			data = getObjectMapperNonStardand().treeToValue(jsonNode, Object.class);
		}
		catch (Throwable t)
		{
			throw new DataSetException(t);
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
	 * @return
	 */
	public boolean isLegalResultDataJsonNode(JsonNode jsonNode)
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
}
