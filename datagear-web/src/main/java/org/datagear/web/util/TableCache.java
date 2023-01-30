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

package org.datagear.web.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.datagear.management.domain.Schema;
import org.datagear.meta.Table;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * {@linkplain Table}缓存。
 * 
 * @author datagear@163.com
 *
 */
public class TableCache
{
	/** 缓存值的最大数 */
	private int maximumSize = 1000;

	/** 缓存过期分钟数 */
	private int expireAfterAccessMinutes = 60 * 72;

	private Cache<TableCacheKey, Table> _cache = null;

	public TableCache()
	{
		super();
	}

	public int getMaximumSize()
	{
		return maximumSize;
	}

	public void setMaximumSize(int maximumSize)
	{
		this.maximumSize = maximumSize;
	}

	public int getExpireAfterAccessMinutes()
	{
		return expireAfterAccessMinutes;
	}

	public void setExpireAfterAccessMinutes(int expireAfterAccessMinutes)
	{
		this.expireAfterAccessMinutes = expireAfterAccessMinutes;
	}

	/**
	 * 初始化。
	 */
	public void init()
	{
		this._cache = Caffeine.newBuilder().maximumSize(this.maximumSize)
				.expireAfterAccess(this.expireAfterAccessMinutes, TimeUnit.MINUTES).build();
	}

	/**
	 * 获取{@linkplain Table}。
	 * 
	 * @param schemaId
	 * @param tableName
	 * @return 返回{@code null}表示没有缓存
	 */
	public Table get(String schemaId, String tableName)
	{
		TableCacheKey key = new TableCacheKey(schemaId, tableName);
		return this._cache.getIfPresent(key);
	}

	/**
	 * 将{@linkplain Table}添加至缓存。
	 * 
	 * @param schemaId
	 * @param table
	 */
	public void put(String schemaId, Table table)
	{
		TableCacheKey key = new TableCacheKey(schemaId, table.getName());
		this._cache.put(key, table);
	}

	/**
	 * 清除指定名称{@linkplain Table}缓存。
	 * 
	 * @param schemaId
	 * @param tableName
	 */
	public void invalidate(String schemaId, String tableName)
	{
		TableCacheKey key = new TableCacheKey(schemaId, tableName);
		this._cache.invalidate(key);
	}

	/**
	 * 清除指定{@linkplain Schema} ID的所有{@linkplain Table}缓存。
	 * 
	 * @param schemaId
	 */
	public void invalidate(String schemaId)
	{
		ConcurrentMap<TableCacheKey, Table> map = this._cache.asMap();
		Set<TableCacheKey> keys = map.keySet();

		Set<TableCacheKey> myKeys = new HashSet<>();
		for (TableCacheKey key : keys)
		{
			if (key.getSchemaId().equals(schemaId))
				myKeys.add(key);
		}

		if (!myKeys.isEmpty())
			this._cache.invalidateAll(myKeys);
	}

	protected static class TableCacheKey implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String schemaId;
		private final String tableName;

		public TableCacheKey(String schemaId, String tableName)
		{
			super();
			this.schemaId = schemaId;
			this.tableName = tableName;
		}

		public String getSchemaId()
		{
			return schemaId;
		}

		public String getTableName()
		{
			return tableName;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schemaId == null) ? 0 : schemaId.hashCode());
			result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
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
			TableCacheKey other = (TableCacheKey) obj;
			if (schemaId == null)
			{
				if (other.schemaId != null)
					return false;
			}
			else if (!schemaId.equals(other.schemaId))
				return false;
			if (tableName == null)
			{
				if (other.tableName != null)
					return false;
			}
			else if (!tableName.equals(other.tableName))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [schemaId=" + schemaId + ", tableName=" + tableName + "]";
		}
	}
}
