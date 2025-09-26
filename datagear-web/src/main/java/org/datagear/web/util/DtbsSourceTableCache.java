/*
 * Copyright 2018-present datagear.tech
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

import org.datagear.management.domain.DtbsSource;
import org.datagear.meta.Table;
import org.datagear.util.cache.CacheAware;
import org.datagear.util.cache.CommonCacheKey;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * 数据库表{@linkplain Table}缓存。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceTableCache implements CacheAware
{
	private Cache cache;

	public DtbsSourceTableCache()
	{
		super();
	}

	public DtbsSourceTableCache(Cache cache)
	{
		super();
		this.cache = cache;
	}

	@Override
	public Cache getCache()
	{
		return cache;
	}

	@Override
	public void setCache(Cache cache)
	{
		this.cache = cache;
	}

	/**
	 * 获取{@linkplain Table}。
	 * 
	 * @param dtbsSourceId
	 * @param tableName
	 * @return 返回{@code null}表示没有缓存
	 */
	public Table get(String dtbsSourceId, String tableName)
	{
		DtbsSourceTableCacheKey key = toCacheKey(dtbsSourceId, tableName);

		ValueWrapper vw = this.cache.get(key);
		return (vw == null ? null : (Table) vw.get());
	}

	/**
	 * 将{@linkplain Table}添加至缓存。
	 * 
	 * @param dtbsSourceId
	 * @param table
	 */
	public void put(String dtbsSourceId, Table table)
	{
		DtbsSourceTableCacheKey key = toCacheKey(dtbsSourceId, table.getName());
		this.cache.put(key, table);
	}

	/**
	 * 删除指定名称{@linkplain Table}缓存。
	 * 
	 * @param dtbsSourceId
	 * @param tableName
	 */
	public void invalidate(String dtbsSourceId, String tableName)
	{
		DtbsSourceTableCacheKey key = toCacheKey(dtbsSourceId, tableName);
		this.cache.evict(key);
	}

	/**
	 * 清除指定{@linkplain DtbsSource} ID的所有{@linkplain Table}缓存。
	 * 
	 * @param dtbsSourceId
	 */
	public void invalidate(String dtbsSourceId)
	{
		this.cache.evict(dtbsSourceId);
	}

	protected DtbsSourceTableCacheKey toCacheKey(String dtbsSourceId, String tableName)
	{
		return new DtbsSourceTableCacheKey(dtbsSourceId, tableName);
	}

	protected static class DtbsSourceTableCacheKey implements CommonCacheKey, Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String dtbsSourceId;

		private final String tableName;

		public DtbsSourceTableCacheKey(String dtbsSourceId, String tableName)
		{
			super();
			this.dtbsSourceId = dtbsSourceId;
			this.tableName = tableName;
		}

		public String getDtbsSourceId()
		{
			return dtbsSourceId;
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
			result = prime * result + ((dtbsSourceId == null) ? 0 : dtbsSourceId.hashCode());
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
			DtbsSourceTableCacheKey other = (DtbsSourceTableCacheKey) obj;
			if (dtbsSourceId == null)
			{
				if (other.dtbsSourceId != null)
					return false;
			}
			else if (!dtbsSourceId.equals(other.dtbsSourceId))
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
			return getClass().getSimpleName() + " [dtbsSourceId=" + dtbsSourceId + ", tableName=" + tableName + "]";
		}
	}
}
