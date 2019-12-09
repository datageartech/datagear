/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 抽象代理映射表。
 * 
 * @author datagear@163.com
 *
 * @param <K>
 * @param <V>
 */
public class AbstractDelegatedMap<K, V> implements Map<K, V>
{
	private Map<K, V> delegatedMap;

	public AbstractDelegatedMap()
	{
		super();
		this.delegatedMap = new HashMap<K, V>();
	}

	public AbstractDelegatedMap(Map<K, V> delegatedMap)
	{
		super();
		this.delegatedMap = delegatedMap;
	}

	public Map<K, V> getDelegatedMap()
	{
		return delegatedMap;
	}

	public void setDelegatedMap(Map<K, V> delegatedMap)
	{
		this.delegatedMap = delegatedMap;
	}

	@Override
	public int size()
	{
		return this.delegatedMap.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.delegatedMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.delegatedMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.delegatedMap.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return this.delegatedMap.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		return this.delegatedMap.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		return this.delegatedMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		this.delegatedMap.putAll(m);
	}

	@Override
	public void clear()
	{
		this.delegatedMap.clear();
	}

	@Override
	public Set<K> keySet()
	{
		return this.delegatedMap.keySet();
	}

	@Override
	public Collection<V> values()
	{
		return this.delegatedMap.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return this.delegatedMap.entrySet();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegatedMap == null) ? 0 : delegatedMap.hashCode());
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
		AbstractDelegatedMap<?, ?> other = (AbstractDelegatedMap<?, ?>) obj;
		if (delegatedMap == null)
		{
			if (other.delegatedMap != null)
				return false;
		}
		else if (!delegatedMap.equals(other.delegatedMap))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return this.delegatedMap.toString();
	}
}
