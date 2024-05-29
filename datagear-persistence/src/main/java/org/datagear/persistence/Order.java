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

package org.datagear.persistence;

/**
 * 排序。
 * 
 * @author datagear@163.com
 *
 */
public class Order extends org.datagear.util.query.Order
{
	private static final long serialVersionUID = 1L;

	public Order()
	{
		super();
	}

	public Order(String name)
	{
		super(name);
	}

	public Order(String name, String type)
	{
		super(name, type);
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
