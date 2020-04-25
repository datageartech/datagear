/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.json.jackson;

import java.util.List;

import org.datagear.analysis.support.JsonSupport;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * {@linkplain ObjectMapper}工厂。
 * 
 * @author datagear@163.com
 *
 */
public class ObjectMapperFactory
{
	private List<JsonSerializerConfig> jsonSerializerConfigs;

	public ObjectMapperFactory()
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
	 * 获取{@linkplain ObjectMapper}对象。
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ObjectMapper getObjectMapper()
	{
		ObjectMapper objectMapper = JsonSupport.create();
		JsonSupport.setWriteJsonFeatures(objectMapper);
		JsonSupport.setReadNonStandardJsonFeatures(objectMapper);

		if (this.jsonSerializerConfigs != null && !this.jsonSerializerConfigs.isEmpty())
		{
			SimpleModule module = new SimpleModule(ObjectMapperFactory.class.getSimpleName());

			for (JsonSerializerConfig sc : this.jsonSerializerConfigs)
			{
				module.addSerializer(sc.getSerializeType(), (JsonSerializer<Object>) sc.getJsonSerializer());
			}

			objectMapper.registerModule(module);
		}

		return objectMapper;
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
	}
}
