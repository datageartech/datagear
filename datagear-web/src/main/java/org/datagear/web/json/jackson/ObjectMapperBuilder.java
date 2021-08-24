/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.json.jackson;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datagear.analysis.support.JsonSupport;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * {@linkplain ObjectMapper}构建器。
 * 
 * @author datagear@163.com
 *
 */
public class ObjectMapperBuilder
{
	private List<JsonSerializerConfig> jsonSerializerConfigs;

	public ObjectMapperBuilder()
	{
	}

	public List<JsonSerializerConfig> getJsonSerializerConfigs()
	{
		return jsonSerializerConfigs;
	}

	public void setJsonSerializerConfigs(List<JsonSerializerConfig> jsonSerializerConfigs)
	{
		this.jsonSerializerConfigs = jsonSerializerConfigs;
	}

	/**
	 * 构建新{@linkplain ObjectMapper}对象。
	 * 
	 * @return
	 */
	public ObjectMapper build()
	{
		ObjectMapper objectMapper = JsonSupport.create();
		JsonSupport.setWriteJsonFeatures(objectMapper);
		JsonSupport.setReadNonStandardJsonFeatures(objectMapper);

		List<JsonSerializerConfig> configs = (this.jsonSerializerConfigs != null ? this.jsonSerializerConfigs
				: Collections.emptyList());

		return build(configs);
	}

	/**
	 * 构建新{@linkplain ObjectMapper}对象。
	 * <p>
	 * 对于{@linkplain Long}、{@linkplain BigInteger}将序列化为字符串而非数值。
	 * </p>
	 * <p>
	 * 对于大整数，当序列化至JavaScript语境时，可能会出现精度丢失问题（超出其最大安全数），
	 * 比如，对于：{@code 9223372036854775807}，在JavaScript中只能识别为：{@code 9223372036854776000}。
	 * </p>
	 * <p>
	 * 此方法构建的{@linkplain ObjectMapper}会将大整数序列化为字符串，可以解决它们在JavaScript中的显示问题。
	 * </p>
	 * 
	 * @return
	 */
	public ObjectMapper buildForBigNumberToString()
	{
		ObjectMapper objectMapper = JsonSupport.create();
		JsonSupport.setWriteJsonFeatures(objectMapper);
		JsonSupport.setReadNonStandardJsonFeatures(objectMapper);

		List<JsonSerializerConfig> configs = new ArrayList<JsonSerializerConfig>();

		if (this.jsonSerializerConfigs != null)
			configs.addAll(this.jsonSerializerConfigs);
		
		configs.add(JsonSerializerConfig.valueOf(Long.class, new LongToStringSerializer()));
		configs.add(JsonSerializerConfig.valueOf(BigInteger.class, new BigIntegerToStringSerializer()));

		return build(configs);
	}

	protected ObjectMapper build(List<JsonSerializerConfig> configs)
	{
		ObjectMapper objectMapper = JsonSupport.create();
		JsonSupport.setWriteJsonFeatures(objectMapper);
		JsonSupport.setReadNonStandardJsonFeatures(objectMapper);

		String moduleName = ObjectMapperBuilder.class.getSimpleName();

		addSerializer(objectMapper, moduleName, configs);

		return objectMapper;
	}

	@SuppressWarnings("unchecked")
	protected void addSerializer(ObjectMapper objectMapper, String moduleName, List<JsonSerializerConfig> configs)
	{
		SimpleModule module = new SimpleModule(moduleName);

		for (JsonSerializerConfig sc : configs)
			module.addSerializer(sc.getSerializeType(), (JsonSerializer<Object>) sc.getJsonSerializer());

		objectMapper.registerModule(module);
	}

	public static class JsonSerializerConfig
	{
		private Class<?> serializeType;
		private JsonSerializer<?> jsonSerializer;

		public JsonSerializerConfig()
		{
			super();
		}

		public JsonSerializerConfig(Class<?> serializeType, JsonSerializer<?> jsonSerializer)
		{
			super();
			this.serializeType = serializeType;
			this.jsonSerializer = jsonSerializer;
		}

		public Class<?> getSerializeType()
		{
			return serializeType;
		}

		public void setSerializeType(Class<?> serializeType)
		{
			this.serializeType = serializeType;
		}

		public JsonSerializer<?> getJsonSerializer()
		{
			return jsonSerializer;
		}

		public void setJsonSerializer(JsonSerializer<?> jsonSerializer)
		{
			this.jsonSerializer = jsonSerializer;
		}

		public static JsonSerializerConfig valueOf(Class<?> serializeType, JsonSerializer<?> jsonSerializer)
		{
			return new JsonSerializerConfig(serializeType, jsonSerializer);
		}
	}
}
