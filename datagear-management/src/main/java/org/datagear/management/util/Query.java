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

package org.datagear.management.util;

import java.util.Arrays;

import org.datagear.util.query.KeywordQuery;
import org.datagear.util.query.Order;
import org.datagear.util.query.OrdersAware;

/**
 * 查询。
 *
 * @author datagear@163.com
 *
 */
public class Query extends KeywordQuery implements OrdersAware
{
	private static final long serialVersionUID = 1L;

	/** 排序方式 */
	private Order[] orders;

	public Query()
	{
		super();
	}

	public Query(String keyword)
	{
		super(keyword);
	}

	@Override
	public Order[] getOrders()
	{
		return orders;
	}

	@Override
	public void setOrders(Order[] orders)
	{
		this.orders = orders;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(orders);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Query other = (Query) obj;
		if (!Arrays.equals(orders, other.orders))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [keyword=" + getKeyword() + ", orders=" + Arrays.toString(orders) + "]";
	}
}
