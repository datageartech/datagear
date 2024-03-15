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

import org.datagear.management.domain.Schema;
import org.datagear.meta.Table;
import org.datagear.util.StringUtil;
import org.datagear.util.cache.CollectionCacheValue;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * 数据库表{@linkplain Table}缓存。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaTableCache
{
	private Cache cache;

	/** 每个数据源最多缓存表数目 */
	private int tableCacheMaxLength = 10;

	public SchemaTableCache()
	{
		super();
	}

	public SchemaTableCache(Cache cache)
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

	public int getTableCacheMaxLength()
	{
		return tableCacheMaxLength;
	}

	public void setTableCacheMaxLength(int tableCacheMaxLength)
	{
		this.tableCacheMaxLength = tableCacheMaxLength;
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
		ValueWrapper vw = this.cache.get(schemaId);
		TableCacheValue tcv = (vw == null ? null : (TableCacheValue) vw.get());

		return (tcv == null ? null : tcv.get(tableName));
	}

	/**
	 * 将{@linkplain Table}添加至缓存。
	 * 
	 * @param schemaId
	 * @param table
	 */
	public void put(String schemaId, Table table)
	{
		ValueWrapper vw = this.cache.get(schemaId);
		TableCacheValue tcv = (vw == null ? null : (TableCacheValue) vw.get());

		if (tcv == null)
			tcv = new TableCacheValue();

		tcv.add(table, this.tableCacheMaxLength);

		this.cache.put(schemaId, tcv);
	}

	/**
	 * 删除指定名称{@linkplain Table}缓存。
	 * 
	 * @param schemaId
	 * @param tableName
	 */
	public void invalidate(String schemaId, String tableName)
	{
		ValueWrapper vw = this.cache.get(schemaId);
		TableCacheValue tcv = (vw == null ? null : (TableCacheValue) vw.get());

		if (tcv != null)
			tcv.remove(tableName);

		this.cache.put(schemaId, tcv);
	}

	/**
	 * 清除指定{@linkplain Schema} ID的所有{@linkplain Table}缓存。
	 * 
	 * @param schemaId
	 */
	public void invalidate(String schemaId)
	{
		this.cache.evict(schemaId);
	}

	public static class TableCacheValue extends CollectionCacheValue<Table>
	{
		private static final long serialVersionUID = 1L;

		public TableCacheValue()
		{
			super();
		}

		/**
		 * 获取{@linkplain Table}。
		 * 
		 * @param tableName
		 * @return 可能{@code null}
		 */
		public Table get(String tableName)
		{
			return find(t ->
			{
				return StringUtil.isEquals(tableName, t.getName());
			});
		}

		/**
		 * 添加。
		 * <p>
		 * 注意：执行此操作后应执行存入缓存操作。
		 * </p>
		 * 
		 * @param table
		 * @param maxSize
		 */
		public void add(Table table, int maxSize)
		{
			add(table, (t) ->
			{
				return StringUtil.isEquals(table.getName(), t.getName());
			}, maxSize);
		}

		/**
		 * 添加。
		 * <p>
		 * 注意：执行此操作后应执行存入缓存操作。
		 * </p>
		 * 
		 * @param tableName
		 * @return
		 */
		public Table remove(String tableName)
		{
			return remove(t ->
			{
				return StringUtil.isEquals(tableName, t.getName());
			});
		}
	}
}
