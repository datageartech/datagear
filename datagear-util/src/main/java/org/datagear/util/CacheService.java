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

package org.datagear.util;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * 缓存服务。
 * 
 * @author datagear@163.com
 *
 */
public class CacheService
{
	/** 缓存 */
	private Cache cache = null;

	/** 缓存是否是编辑安全的 */
	private boolean safeForEdit = false;

	/** 是否禁用 */
	private boolean disabled = false;

	public CacheService()
	{
		super();
	}

	/**
	 * 创建。
	 * 
	 * @param cache 允许{@code null}
	 */
	public CacheService(Cache cache)
	{
		super();
		this.cache = cache;
	}

	/**
	 * 创建。
	 * 
	 * @param cache
	 *            允许{@code null}
	 * @param safeForEdit
	 *            参考{@linkplain #isSafeForEdit()}
	 */
	public CacheService(Cache cache, boolean safeForEdit)
	{
		super();
		this.cache = cache;
		this.safeForEdit = safeForEdit;
	}

	/**
	 * 获取缓存。
	 * <p>
	 * 使用参考{@linkplain #isEnabled()}。
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
	 * 缓存是否是编辑安全的。
	 * <p>
	 * 即：获取、存入缓存后，修改对象属性不会影响已缓存的对象。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isSafeForEdit()
	{
		return safeForEdit;
	}

	public void setSafeForEdit(boolean safeForEdit)
	{
		this.safeForEdit = safeForEdit;
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
	public boolean isEnabled()
	{
		return (!this.disabled && this.cache != null);
	}

	/**
	 * 获取缓存。
	 * <p>
	 * 如果{@linkplain #isSafeForEdit()}为{@code false}，调用方应确保获取对象不被修改。
	 * </p>
	 * 
	 * @param key
	 * @return 为{@code null}表示未缓存，如果{@linkplain #isEnabled()}为{@code false}，将始终返回{@code null}
	 */
	public ValueWrapper get(Object key)
	{
		if (!isEnabled())
			return null;

		return this.cache.get(key);
	}

	/**
	 * 存入缓存。
	 * <p>
	 * 如果{@linkplain #isSafeForEdit()}为{@code false}，调用方应确保存入对象不会被修改。
	 * </p>
	 * <p>
	 * 如果{@linkplain #isEnabled()}为{@code false}，将不执行任何操作。
	 * </p>
	 * 
	 * @param key
	 * @param value
	 */
	public void put(Object key, Object value)
	{
		if (!isEnabled())
			return;

		this.cache.put(key, value);
	}

	/**
	 * 删除缓存。
	 * <p>
	 * 如果{@linkplain #isEnabled()}为{@code false}，将不执行任何操作。
	 * </p>
	 * 
	 * @param key
	 */
	public void evictImmediately(Object key)
	{
		if (!isEnabled())
			return;

		this.cache.evictIfPresent(key);
	}

	/**
	 * 清空缓存。
	 * <p>
	 * 如果{@linkplain #isEnabled()}为{@code false}，将不执行任何操作。
	 * </p>
	 */
	public void invalidate()
	{
		if (!isEnabled())
			return;

		this.cache.invalidate();
	}
}
