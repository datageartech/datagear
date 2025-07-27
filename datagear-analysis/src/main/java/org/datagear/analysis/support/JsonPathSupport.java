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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.datagear.util.StringUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * JsonPath支持类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonPathSupport
{
	public static final JsonPathSupport INSTANCE = new JsonPathSupport();

	private Configuration configuration = Configuration.builder()
			.jsonProvider(new JacksonJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();

	public JsonPathSupport()
	{
		super();
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
	}

	/**
	 * 解析JSON路径结果。
	 * 
	 * @param data
	 *            允许{@code null}
	 * @param jsonPath
	 *            允许{@code null}，支持省略{@code '$'}的简化模式，
	 *            比如：{@linkplain "$[0].a"}可简写为{@linkplain "[0].a"}、{@linkplain "$.a.b"}可简写为{@linkplain "a.b"}
	 * @return
	 * @throws ReadJsonDataPathException
	 */
	public Object resolve(Object data, String jsonPath) throws ReadJsonDataPathException
	{
		if (data == null || StringUtil.isEmpty(jsonPath))
			return data;

		String stdDataJsonPath = jsonPath.trim();

		if (!StringUtil.isEmpty(stdDataJsonPath))
		{
			Configuration configuration = getConfiguration();

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
				data = JsonPath.compile(stdDataJsonPath).read(data, configuration);
			}
			catch (Throwable t)
			{
				throw new ReadJsonDataPathException(jsonPath, t);
			}
		}

		return data;
	}

	/**
	 * 解析JSON路径结果映射表。
	 * 
	 * @param data
	 *            允许{@code null}
	 * @param jsonPaths
	 *            允许{@code null}，JSON路径映射表，关键字作为返回映射表的关键字，值则是JSON路径
	 * @return
	 * @throws ReadJsonDataPathException
	 */
	public Map<String, Object> resolveMap(Object data, Map<String, String> jsonPaths) throws ReadJsonDataPathException
	{
		if (data == null || jsonPaths == null || jsonPaths.isEmpty())
			return Collections.emptyMap();

		Map<String, Object> re = new HashMap<>();

		for (Map.Entry<String, String> entry : jsonPaths.entrySet())
		{
			String name = entry.getKey();
			String path = entry.getValue();
			Object value = resolve(data, path);
			re.put(name, value);
		}

		return re;
	}

	/**
	 * 解析JSON路径结果映射表。
	 * 
	 * @param data
	 *            允许{@code null}
	 * @param jsonPathsConfig
	 *            允许{@code null}，{@linkplain #resolveMap(Object, Map)}中的{@code jsonPaths}的JSON格式
	 * @return
	 * @throws ReadJsonDataPathException
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public Map<String, Object> resolveMap(Object data, String jsonPathsJson)
			throws ReadJsonDataPathException, JsonMappingException, JsonProcessingException
	{
		if (StringUtil.isEmpty(jsonPathsJson))
			return Collections.emptyMap();

		@SuppressWarnings("unchecked")
		Map<String, String> jsonPaths = getObjectMapperNonStardand().readValue(jsonPathsJson, Map.class);

		if (jsonPaths == null || jsonPaths.isEmpty())
			return Collections.emptyMap();

		return resolveMap(data, jsonPaths);
	}

	protected ObjectMapper getObjectMapperNonStardand()
	{
		return JsonSupport.getObjectMapperNonStardand();
	}
}
