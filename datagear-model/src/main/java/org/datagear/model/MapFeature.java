/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 映射表特性。
 * 
 * @author datagear@163.com
 *
 */
public class MapFeature<K, V> implements Feature
{
	/** 默认值 */
	private V value;

	/** 映射表值 */
	private Map<K, V> mapValues;

	public MapFeature()
	{
		super();
	}

	public MapFeature(V value)
	{
		super();
		this.value = value;
	}

	public MapFeature(Map<K, V> mapValues)
	{
		super();
		this.mapValues = mapValues;
	}

	public MapFeature(V value, Map<K, V> mapValues)
	{
		super();
		this.value = value;
		this.mapValues = mapValues;
	}

	public V getValue()
	{
		return value;
	}

	public void setValue(V value)
	{
		this.value = value;
	}

	public Map<K, V> getMapValues()
	{
		return mapValues;
	}

	public void setMapValues(Map<K, V> mapValues)
	{
		this.mapValues = mapValues;
	}

	/**
	 * 获取所有关键字。
	 * 
	 * @return
	 */
	public Set<K> getKeys()
	{
		return (this.mapValues == null ? null : this.mapValues.keySet());
	}

	/**
	 * 获取指定关键字的值。
	 * <p>
	 * 如果没有指定关键字的值，将返回{@linkplain #getValue()}值。
	 * </p>
	 * 
	 * @param key
	 * @return
	 */
	public V getValue(K key)
	{
		V value = (this.mapValues == null ? null : this.mapValues.get(key));

		if (value == null)
			value = this.value;

		return value;
	}

	/**
	 * 获取指定关键字的值。
	 * <p>
	 * 如果没有指定关键字的值，将返回{@code null}。
	 * </p>
	 * 
	 * @param key
	 * @return
	 */
	public V getOriginalValue(K key)
	{
		return (this.mapValues == null ? null : this.mapValues.get(key));
	}

	/**
	 * 设置指定关键字的值。
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public V setValue(K key, V value)
	{
		if (this.mapValues == null)
			this.mapValues = new HashMap<K, V>();

		return this.mapValues.put(key, value);
	}

	/**
	 * 是否包含指定关键字的值。
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasValue(K key)
	{
		return (this.mapValues == null ? false : (this.mapValues.get(key) != null));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((mapValues == null) ? 0 : mapValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapFeature<?, ?> other = (MapFeature<?, ?>) obj;
		if (value == null)
		{
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		if (mapValues == null)
		{
			if (other.mapValues != null)
				return false;
		}
		else if (!mapValues.equals(other.mapValues))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [value=" + value + ", mapValues=" + mapValues + "]";
	}
}
