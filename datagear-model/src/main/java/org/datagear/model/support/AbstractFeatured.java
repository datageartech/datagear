/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.util.HashMap;
import java.util.Map;

import org.datagear.model.Featured;

/**
 * 抽象特性对象。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractFeatured implements Featured
{
	/** 特性映射表 */
	private Map<Object, Object> features = new HashMap<Object, Object>();

	public AbstractFeatured()
	{
		super();
	}

	@Override
	public Map<?, ?> getFeatures()
	{
		return features;
	}

	/**
	 * 设置特性映射表。
	 * 
	 * @param features
	 */
	@SuppressWarnings("unchecked")
	public void setFeatures(Map<?, ?> features)
	{
		Map<Object, Object> map = (Map<Object, Object>) features;

		for (Map.Entry<Object, Object> entry : map.entrySet())
			this.features.put(toFeatureKey(entry.getKey()), entry.getValue());
	}

	@Override
	public boolean hasFeature()
	{
		return !this.features.isEmpty();
	}

	@Override
	public boolean hasFeature(Object key)
	{
		return this.features.containsKey(toFeatureKey(key));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFeature(Object key)
	{
		return (T) this.features.get(toFeatureKey(key));
	}

	@Override
	public void setFeature(Object key)
	{
		this.features.put(toFeatureKey(key), true);
	}

	@Override
	public void setFeature(Object key, Object value)
	{
		this.features.put(toFeatureKey(key), value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeFeature(Object key)
	{
		return (T) this.features.remove(toFeatureKey(key));
	}

	protected Object toFeatureKey(Object key)
	{
		if (key instanceof Class<?>)
			return ((Class<?>) key).getSimpleName();

		return key;

	}
}
