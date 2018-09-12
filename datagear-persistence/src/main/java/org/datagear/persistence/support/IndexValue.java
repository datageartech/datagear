/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support;

import java.util.List;

/**
 * 封装对象索引信息。
 * 
 * @author datagear@163.com
 *
 */
public class IndexValue<T>
{
	private long index;

	private T value;

	public IndexValue()
	{
		super();
	}

	public IndexValue(long index, T value)
	{
		super();
		this.index = index;
		this.value = value;
	}

	public long getIndex()
	{
		return index;
	}

	public void setIndex(long index)
	{
		this.index = index;
	}

	public T getValue()
	{
		return value;
	}

	public void setValue(T value)
	{
		this.value = value;
	}

	public static <T> IndexValue<T> valueOf(long index, T value)
	{
		return new IndexValue<T>(index, value);
	}

	public static <T> T[] toValueArray(List<IndexValue<T>> indexValues, T[] target)
	{
		long len = indexValues.size();
		if (len > target.length)
			len = target.length;

		for (int i = 0; i < len; i++)
		{
			IndexValue<T> indexValue = indexValues.get(i);

			target[i] = indexValue.getValue();
		}

		return target;
	}

	public static long[] toIndexArray(List<? extends IndexValue<?>> indexValues)
	{
		long[] indexes = new long[indexValues.size()];

		for (int i = 0; i < indexes.length; i++)
		{
			IndexValue<?> indexValue = indexValues.get(i);

			indexes[i] = indexValue.getIndex();
		}

		return indexes;
	}
}
