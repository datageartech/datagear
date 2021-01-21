/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence;

import java.io.Serializable;

/**
 * 排序。
 * 
 * @author datagear@163.com
 *
 */
public class Order implements Serializable
{
	public static final String ASC = "ASC";

	public static final String DESC = "DESC";

	private static final long serialVersionUID = 1L;

	/** 排序名 */
	private String name;

	/** 排序类型 */
	private String type;

	public Order()
	{
		super();
	}

	public Order(String name, String type)
	{
		super();
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * 是否升序。
	 * 
	 * @return
	 */
	public boolean isAsc()
	{
		return !isDesc();
	}

	/**
	 * 是否降序。
	 * 
	 * @return
	 */
	public boolean isDesc()
	{
		return DESC.equalsIgnoreCase(this.type);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Order other = (Order) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * 构建{@linkplain Order}。
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public static Order valueOf(String name, String type)
	{
		return new Order(name, type);
	}

	/**
	 * 转换为数组。
	 * 
	 * @param order
	 * @return
	 */
	public static Order[] asArray(Order order)
	{
		return new Order[] { order };
	}
}
