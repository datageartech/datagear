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

import java.io.IOException;
import java.io.Reader;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Json支持类。
 * <p>
 * 基于Jackson的{@linkplain ObjectMapper}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class JsonSupport
{
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final ObjectMapper OBJECT_MAPPER_NON_STARDAND = new ObjectMapper();
	static
	{
		setWriteJsonFeatures(OBJECT_MAPPER);
		setWriteJsonFeatures(OBJECT_MAPPER_NON_STARDAND);
		setReadNonStandardJsonFeatures(OBJECT_MAPPER_NON_STARDAND);
	}

	public JsonSupport()
	{
	}

	/**
	 * 生成JSON。
	 * 
	 * @param value
	 * @return
	 * @throws JsonProcessingException
	 */
	public static String generate(Object value) throws JsonProcessingException
	{
		return OBJECT_MAPPER.writeValueAsString(value);
	}

	/**
	 * 生成JSON，如果出现异常，则返回{@code defaultJson}。
	 * 
	 * @param value
	 * @param defaultJson
	 * @return
	 */
	public static String generate(Object value, String defaultJson)
	{
		try
		{
			return generate(value);
		}
		catch (JsonProcessingException e)
		{
			return defaultJson;
		}
	}

	/**
	 * 解析标准JSON。
	 * 
	 * @param <T>
	 * @param json
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static <T> T parse(String json, Class<T> type) throws IOException
	{
		return OBJECT_MAPPER.readValue(json, type);
	}

	/**
	 * 解析标准JSON。
	 * 
	 * @param <T>
	 * @param json
	 * @param type
	 * @param defaultValue
	 * @return
	 */
	public static <T> T parse(String json, Class<T> type, T defaultValue)
	{
		try
		{
			return OBJECT_MAPPER.readValue(json, type);
		}
		catch (IOException e)
		{
			return defaultValue;
		}
	}

	/**
	 * 解析非标准JSON。
	 * 
	 * @param <T>
	 * @param json
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static <T> T parseNonStardand(String json, Class<T> type) throws IOException
	{
		return OBJECT_MAPPER_NON_STARDAND.readValue(json, type);
	}

	/**
	 * 解析非标准JSON。
	 * 
	 * @param <T>
	 * @param json
	 * @param type
	 * @param defaultValue
	 * @return
	 */
	public static <T> T parseNonStardand(String json, Class<T> type, T defaultValue)
	{
		try
		{
			return OBJECT_MAPPER_NON_STARDAND.readValue(json, type);
		}
		catch (IOException e)
		{
			return defaultValue;
		}
	}

	/**
	 * 解析非标准JSON。
	 * 
	 * @param <T>
	 * @param reader
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static <T> T parseNonStardand(Reader reader, Class<T> type) throws IOException
	{
		return OBJECT_MAPPER_NON_STARDAND.readValue(reader, type);
	}

	public static ObjectMapper getObjectMapper()
	{
		return OBJECT_MAPPER;
	}

	public static ObjectMapper getObjectMapperNonStardand()
	{
		return OBJECT_MAPPER_NON_STARDAND;
	}

	/**
	 * 创建{@linkplain ObjectMapper}实例。
	 * 
	 * @return
	 */
	public static ObjectMapper create()
	{
		return new ObjectMapper();
	}

	/**
	 * 设置写JSON特性。
	 * 
	 * @param objectMapper
	 */
	@SuppressWarnings("deprecation")
	public static void setWriteJsonFeatures(ObjectMapper objectMapper)
	{
		objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
		// XXX jackson默认输出JSON是会加双引号的，这里保留仅用于标识此项设置是必须的
		objectMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
	}

	/**
	 * 设置读非标准JSON特性。
	 * <p>
	 * 允许单引号、无引号字段名。
	 * </p>
	 * 
	 * @param objectMapper
	 */
	public static void setReadNonStandardJsonFeatures(ObjectMapper objectMapper)
	{
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	/**
	 * 禁用{@linkplain JsonGenerator.Feature.AUTO_CLOSE_TARGET}特性。
	 * 
	 * @param objectMapper
	 */
	public static void disableAutoCloseTargetFeature(ObjectMapper objectMapper)
	{
		objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
	}
}
