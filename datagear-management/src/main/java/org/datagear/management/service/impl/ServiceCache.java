/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * 服务缓存。
 * 
 * @author datagear@163.com
 *
 */
public class ServiceCache
{
	/** 缓存 */
	private Cache cache = null;

	/** 缓存是否开启序列化特性 */
	private boolean serialized = false;

	/** 是否是共享缓存 */
	private boolean shared = false;

	/** 是否禁用 */
	private boolean disabled = false;

	public ServiceCache()
	{
		super();
	}

	/**
	 * @param cache
	 *            允许{@code null}
	 */
	public ServiceCache(Cache cache)
	{
		super();
		this.cache = cache;
	}

	/**
	 * 获取缓存。
	 * <p>
	 * 使用参考{@linkplain #isEnable()}、{@linkplain #isShared()}。
	 * </p>
	 * 
	 * @return 可能为{@code null}。
	 */
	public Cache getCache()
	{
		return cache;
	}

	public void setCache(Cache cache)
	{
		this.cache = cache;
	}

	/**
	 * 缓存是否开启序列化特性。
	 * <p>
	 * 当为{@code true}时，则不必在存入缓存前、取出缓存后，进行克隆以确保缓存不被改变。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isSerialized()
	{
		return serialized;
	}

	public void setSerialized(boolean serialized)
	{
		this.serialized = serialized;
	}

	/**
	 * 是否共享缓存。
	 * <p>
	 * 当为{@code true}时，使用{@linkplain #getCache()}应生成全局缓存KEY。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isShared()
	{
		return shared;
	}

	public void setShared(boolean shared)
	{
		this.shared = shared;
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	/**
	 * 此服务缓存是否是启用的。
	 * <p>
	 * 如果为{@code false}，即使{@linkplain #getCache()}不为{@code null}，也不应使用。
	 * </p>
	 * <p>
	 * 如果为{@code true}，则{@linkplain #getCache()}不会为{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isEnable()
	{
		if (this.disabled)
			return false;

		return (this.cache != null);
	}

	/**
	 * 获取缓存。
	 * <p>
	 * 调用此方法前应确保{@linkplain #isEnable()}为{@code true}。
	 * </p>
	 * 
	 * @param key
	 * @return
	 */
	public ValueWrapper get(Object key)
	{
		if (this.cache == null)
			return null;

		return this.cache.get(key);
	}

	/**
	 * 存入缓存。
	 * <p>
	 * 调用此方法前应确保{@linkplain #isEnable()}为{@code true}。
	 * </p>
	 * 
	 * @param key
	 * @param value
	 */
	public void put(Object key, Object value)
	{
		if (this.cache == null)
			return;

		this.cache.put(key, value);
	}

	/**
	 * 删除缓存。
	 * <p>
	 * 调用此方法前应确保{@linkplain #isEnable()}为{@code true}。
	 * </p>
	 * 
	 * @param key
	 */
	public void evictImmediately(Object key)
	{
		if (this.cache == null)
			return;

		this.cache.evictIfPresent(key);
	}

	/**
	 * 清空缓存。
	 * <p>
	 * 调用此方法前应确保{@linkplain #isEnable()}为{@code true}。
	 * </p>
	 */
	public void invalidate()
	{
		if (this.cache == null)
			return;

		this.cache.invalidate();
	}
}
