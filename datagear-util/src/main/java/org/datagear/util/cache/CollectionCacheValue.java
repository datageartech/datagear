/*
 * Copyright 2018-2024 datagear.tech
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 集合缓存值。
 * 
 * @author datagear@163.com
 *
 */
public class CollectionCacheValue<T extends Serializable> implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<T> values = new ArrayList<>();

	public CollectionCacheValue()
	{
		super();
	}

	public synchronized List<T> getValues()
	{
		return values;
	}

	/**
	 * 查找。
	 * 
	 * @param predicate
	 * @return {@code null}表示未找到
	 */
	public synchronized T find(Predicate<T> predicate)
	{
		int idx = doFindIndex(predicate);
		return (idx < 0 ? null : this.values.get(idx));
	}

	/**
	 * 查找索引。
	 * 
	 * @param predicate
	 * @return {@code -1}表示未找到
	 */
	public synchronized int findIndex(Predicate<T> predicate)
	{
		return doFindIndex(predicate);
	}

	/**
	 * 添加。
	 * <p>
	 * 注意：执行此操作后应执行存入缓存操作。
	 * </p>
	 * 
	 * @param value
	 * @param removePredicate
	 *            允许{@code null}
	 * @param maxSize
	 *            保留最大数，超过则删除旧添加的，{@code -1}表示不删除
	 */
	public synchronized void add(T value, Predicate<T> removePredicate, int maxSize)
	{
		if (removePredicate != null)
			doRemove(removePredicate);

		this.values.add(value);

		if (maxSize > -1)
		{
			while (this.values.size() > maxSize)
				this.values.remove(0);
		}
	}

	/**
	 * 移除一个匹配项。
	 * <p>
	 * 注意：执行此操作后应执行存入缓存操作。
	 * </p>
	 * 
	 * @param removePredicate
	 * @return 可能{@code null}，移除项
	 */
	public synchronized T remove(Predicate<T> removePredicate)
	{
		return doRemove(removePredicate);
	}

	/**
	 * 清空。
	 * <p>
	 * 注意：执行此操作后应执行存入缓存操作。
	 * </p>
	 */
	public synchronized void clear()
	{
		this.values.clear();
	}

	protected T doRemove(Predicate<T> removePredicate)
	{
		int idx = doFindIndex(removePredicate);

		if (idx >= 0)
			return this.values.remove(idx);
		else
			return null;
	}

	protected int doFindIndex(Predicate<T> predicate)
	{
		for (int i = 0, len = this.values.size(); i < len; i++)
		{
			if (predicate.test(this.values.get(i)))
				return i;
		}

		return -1;
	}
}
