/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.constraint;

/**
 * 抽象值{@linkplain Constraint}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractValueConstraint<T> implements Constraint
{
	private T value;

	public AbstractValueConstraint()
	{
	}

	public AbstractValueConstraint(T value)
	{
		super();
		this.value = value;
	}

	public T getValue()
	{
		return value;
	}

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
		AbstractValueConstraint<?> other = (AbstractValueConstraint<?>) obj;
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
