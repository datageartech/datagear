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

package org.datagear.util.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;

/**
 * 代理{@linkplain LocalCache}。
 * 
 * @author datagear@163.com
 *
 */
public class DelegationLocalCache implements LocalCache
{
	private Cache cache;

	public DelegationLocalCache()
	{
		super();
	}

	public DelegationLocalCache(Cache cache)
	{
		super();
		this.cache = cache;
	}

	public Cache getCache()
	{
		return cache;
	}

	public void setCache(Cache cache)
	{
		this.cache = cache;
	}

	@Override
	public String getName()
	{
		return this.cache.getName();
	}

	@Override
	public Object getNativeCache()
	{
		return this.cache.getNativeCache();
	}

	@Override
	public ValueWrapper get(Object key)
	{
		return this.cache.get(key);
	}

	@Override
	public <T> T get(Object key, Class<T> type)
	{
		return this.cache.get(key, type);
	}

	@Override
	public <T> T get(Object key, Callable<T> valueLoader)
	{
		return this.cache.get(key, valueLoader);
	}

	@Override
	public void put(Object key, Object value)
	{
		this.cache.putIfAbsent(key, value);
	}

	@Override
	public void evict(Object key)
	{
		this.cache.evict(key);
	}

	@Override
	public void clear()
	{
		this.cache.clear();
	}
}
