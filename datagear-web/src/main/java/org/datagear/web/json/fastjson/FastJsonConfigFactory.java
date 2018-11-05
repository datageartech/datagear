/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.fastjson;

import java.util.HashMap;
import java.util.Map;

import org.datagear.persistence.collection.SizeOnlyCollection;
import org.datagear.persistence.collection.SizeOnlyList;
import org.datagear.persistence.collection.SizeOnlyQueue;
import org.datagear.persistence.collection.SizeOnlySet;

import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;

/**
 * fastjson配置工厂。
 * 
 * @author datagear@163.com
 *
 */
public class FastJsonConfigFactory
{
	private static final SerializerFeature[] SERIALIZER_FEATURES = new SerializerFeature[] {
			SerializerFeature.WriteMapNullValue };

	private Map<Class<?>, ObjectSerializer> objectSerializerMap = new HashMap<Class<?>, ObjectSerializer>();

	public FastJsonConfigFactory()
	{
		super();
		init();
	}

	public FastJsonConfigFactory(Map<Class<?>, ObjectSerializer> objectSerializerMap)
	{
		super();
		init();
		this.objectSerializerMap.putAll(objectSerializerMap);
	}

	/**
	 * 初始化。
	 */
	protected void init()
	{
		this.objectSerializerMap.put(java.sql.Date.class, new LocaleSqlDateSerializer());
		this.objectSerializerMap.put(java.sql.Time.class, new LocaleSqlTimeSerializer());
		this.objectSerializerMap.put(java.sql.Timestamp.class, new LocaleSqlTimestampSerializer());
		this.objectSerializerMap.put(java.util.Date.class, new LocaleDateSerializer());
		this.objectSerializerMap.put(Class.class, new ClassSerializer(true));
		// this.objectSerializerMap.put(DefaultDynamicBean.class, new
		// DynamicBeanObjectSerializer());
		// this.objectSerializerMap.put(DynamicBean.class, new
		// DynamicBeanObjectSerializer());
		this.objectSerializerMap.put(SizeOnlyList.class, new SizeOnlyCollectionSerializer());
		this.objectSerializerMap.put(SizeOnlySet.class, new SizeOnlyCollectionSerializer());
		this.objectSerializerMap.put(SizeOnlyQueue.class, new SizeOnlyCollectionSerializer());
		this.objectSerializerMap.put(SizeOnlyCollection.class, new SizeOnlyCollectionSerializer());
	}

	public Map<Class<?>, ObjectSerializer> getObjectSerializerMap()
	{
		return objectSerializerMap;
	}

	public void setObjectSerializerMap(Map<Class<?>, ObjectSerializer> objectSerializerMap)
	{
		this.objectSerializerMap.putAll(objectSerializerMap);
	}

	public void setObjectSerializerStringMap(Map<String, ObjectSerializer> objectSerializerStringMap)
	{
		Map<Class<?>, ObjectSerializer> objectSerializerMap = new HashMap<Class<?>, ObjectSerializer>();

		for (Map.Entry<String, ObjectSerializer> entry : objectSerializerStringMap.entrySet())
		{
			try
			{
				Class<?> clazz = Class.forName(entry.getKey());

				objectSerializerMap.put(clazz, entry.getValue());
			}
			catch (ClassNotFoundException e)
			{
				throw new IllegalArgumentException("No class found for [" + entry.getKey() + "]");
			}
		}

		this.objectSerializerMap.putAll(objectSerializerMap);
	}

	/**
	 * 获取{@linkplain SerializeConfig}。
	 * 
	 * @return
	 */
	public SerializeConfig getSerializeConfig()
	{
		SerializeConfig serializeConfig = new SerializeConfig();

		for (Map.Entry<Class<?>, ObjectSerializer> entry : this.objectSerializerMap.entrySet())
			serializeConfig.put(entry.getKey(), entry.getValue());

		return serializeConfig;
	}

	/**
	 * 获取{@linkplain FastJsonConfig}。
	 * 
	 * @return
	 */
	public FastJsonConfig getFastJsonConfig()
	{
		SerializeConfig serializeConfig = getSerializeConfig();
		SerializerFeature[] serializerFeatures = getSerializerFeatures();

		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.setSerializeConfig(serializeConfig);
		fastJsonConfig.setSerializerFeatures(serializerFeatures);

		return fastJsonConfig;
	}

	/**
	 * 获取{@linkplain SerializerFeature}。
	 * 
	 * @return
	 */
	public SerializerFeature[] getSerializerFeatures()
	{
		return SERIALIZER_FEATURES;
	}
}
