/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model;

/**
 * 值特性。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class ValueFeature<T> implements Feature
{
	private T value;

	public ValueFeature()
	{
		super();
	}

	public ValueFeature(T value)
	{
		super();
		this.value = value;
	}

	/**
	 * 获取值。
	 * 
	 * @return
	 */
	public T getValue()
	{
		return value;
	}

	/**
	 * 设置值。
	 * 
	 * @param value
	 */
	public void setValue(T value)
	{
		this.value = value;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ValueFeature<?> other = (ValueFeature<?>) obj;
		if (value == null)
		{
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [value=" + value + "]";
	}
}
