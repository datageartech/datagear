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

package org.datagear.web.json.jackson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.support.JsonSupport;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
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
	private final List<JsonSerializerConfig> jsonSerializerConfigs = new ArrayList<JsonSerializerConfig>();

	private final List<JsonDeserializerConfig> jsonDeserializerConfigs = new ArrayList<JsonDeserializerConfig>();

	public ObjectMapperBuilder()
	{
	}

	public ObjectMapperBuilder(ObjectMapperBuilder builder)
	{
		this.jsonSerializerConfigs.addAll(builder.getJsonSerializerConfigs());
		this.jsonDeserializerConfigs.addAll(builder.getJsonDeserializerConfigs());
	}

	public List<JsonSerializerConfig> getJsonSerializerConfigs()
	{
		return jsonSerializerConfigs;
	}

	public List<JsonDeserializerConfig> getJsonDeserializerConfigs()
	{
		return jsonDeserializerConfigs;
	}

	public void addJsonSerializer(Class<?> type, JsonSerializer<?> serializer)
	{
		this.jsonSerializerConfigs.add(JsonSerializerConfig.valueOf(type, serializer));
	}

	public void addJsonDeserializer(Class<?> type, JsonDeserializer<?> deserializer)
	{
		this.jsonDeserializerConfigs.add(JsonDeserializerConfig.valueOf(type, deserializer));
	}

	/**
	 * 构建新{@linkplain ObjectMapper}对象。
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ObjectMapper build()
	{
		ObjectMapper objectMapper = JsonSupport.create();
		JsonSupport.setWriteJsonFeatures(objectMapper);
		JsonSupport.setReadNonStandardJsonFeatures(objectMapper);
		
		//必须忽略没有setter的属性，因为请求提交的JSON可能会包含这样的属性
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String moduleName = ObjectMapperBuilder.class.getSimpleName();
		SimpleModule module = new SimpleModule(moduleName);

		for (JsonSerializerConfig sc : this.jsonSerializerConfigs)
			module.addSerializer((Class<Object>) sc.getType(), (JsonSerializer<Object>) sc.getSerializer());

		for (JsonDeserializerConfig dc : this.jsonDeserializerConfigs)
			module.addDeserializer((Class<Object>) dc.getType(), (JsonDeserializer<Object>) dc.getDeserializer());

		objectMapper.registerModule(module);

		return objectMapper;
	}
	
	/**
	 * 构建新{@linkplain ObjectMapper}对象。
	 * <p>
	 * 对于字符串属性值，将进行HTML转义。
	 * <p>
	 * 
	 * @return
	 */
	public ObjectMapper buildForEscapeHtml()
	{
		ObjectMapperBuilder builder = new ObjectMapperBuilder(this);

		builder.addJsonSerializer(String.class, new EscapeHtmlStringSerializer());
		
		return builder.build();
	}

	/**
	 * 构建新{@linkplain ObjectMapper}对象。
	 * <p>
	 * 对于{@linkplain Long}、{@linkplain BigInteger}、{@linkplain BigDecimal}将序列化为字符串而非数值。
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
		ObjectMapperBuilder builder = new ObjectMapperBuilder(this);
		
		builder.addJsonSerializer(Long.class, new LongToStringSerializer());
		builder.addJsonSerializer(BigInteger.class, new BigIntegerToStringSerializer());
		builder.addJsonSerializer(BigDecimal.class, new BigDecimalToStringSerializer());

		return builder.build();
	}
	
	/**
	 * 构建新{@linkplain ObjectMapper}对象。
	 * <p>
	 * 具备{@linkplain #buildForEscapeHtml()}、{@linkplain #buildForBigNumberToString()}特性。
	 * <p>
	 * 
	 * @return
	 */
	public ObjectMapper buildForEscapeHtmlAndBigNumberToString()
	{
		ObjectMapperBuilder builder = new ObjectMapperBuilder(this);

		builder.addJsonSerializer(String.class, new EscapeHtmlStringSerializer());
		builder.addJsonSerializer(Long.class, new LongToStringSerializer());
		builder.addJsonSerializer(BigInteger.class, new BigIntegerToStringSerializer());
		builder.addJsonSerializer(BigDecimal.class, new BigDecimalToStringSerializer());

		return builder.build();
	}

	protected static class JsonSerializerConfig
	{
		private Class<?> type;
		private JsonSerializer<?> serializer;

		public JsonSerializerConfig()
		{
			super();
		}

		public JsonSerializerConfig(Class<?> type, JsonSerializer<?> serializer)
		{
			super();
			this.type = type;
			this.serializer = serializer;
		}

		public Class<?> getType()
		{
			return type;
		}

		public void setType(Class<?> type)
		{
			this.type = type;
		}

		public JsonSerializer<?> getSerializer()
		{
			return serializer;
		}

		public void setSerializer(JsonSerializer<?> serializer)
		{
			this.serializer = serializer;
		}

		public static JsonSerializerConfig valueOf(Class<?> type, JsonSerializer<?> serializer)
		{
			return new JsonSerializerConfig(type, serializer);
		}
	}

	protected static class JsonDeserializerConfig
	{
		private Class<?> type;
		private JsonDeserializer<?> deserializer;

		public JsonDeserializerConfig()
		{
			super();
		}

		public JsonDeserializerConfig(Class<?> type, JsonDeserializer<?> deserializer)
		{
			super();
			this.type = type;
			this.deserializer = deserializer;
		}

		public Class<?> getType()
		{
			return type;
		}

		public void setType(Class<?> type)
		{
			this.type = type;
		}

		public JsonDeserializer<?> getDeserializer()
		{
			return deserializer;
		}

		public void setDeserializer(JsonDeserializer<?> deserializer)
		{
			this.deserializer = deserializer;
		}

		public static JsonDeserializerConfig valueOf(Class<?> type, JsonDeserializer<?> deserializer)
		{
			return new JsonDeserializerConfig(type, deserializer);
		}
	}
}
