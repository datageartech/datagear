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

package org.datagear.util.dirquery;

import org.datagear.util.query.KeywordQuery;
import org.datagear.util.query.Order;

/**
 * 目录查询。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryQuery extends KeywordQuery
{
	private static final long serialVersionUID = 1L;

	/**
	 * 查询范围：本级
	 */
	public static final String QUERY_RANGE_CHILDREN = "children";

	/**
	 * 查询范围：全级
	 */
	public static final String QUERY_RANGE_DESCENDANT = "descendant";

	/** 排序方式 */
	private Order order;

	/** 查询范围 */
	private String queryRange = QUERY_RANGE_CHILDREN;

	public DirectoryQuery()
	{
		super();
	}

	public DirectoryQuery(String keyword)
	{
		super(keyword);
	}

	public DirectoryQuery(String keyword, Order order)
	{
		super(keyword);
		this.order = order;
	}

	public DirectoryQuery(String keyword, Order order, String queryRange)
	{
		super(keyword);
		this.order = order;
		this.queryRange = queryRange;
	}

	public Order getOrder()
	{
		return order;
	}

	public void setOrder(Order order)
	{
		this.order = order;
	}

	public String getQueryRange()
	{
		return queryRange;
	}

	public void setQueryRange(String queryRange)
	{
		this.queryRange = queryRange;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((order == null) ? 0 : order.hashCode());
		result = prime * result + ((queryRange == null) ? 0 : queryRange.hashCode());
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
		DirectoryQuery other = (DirectoryQuery) obj;
		if (order == null)
		{
			if (other.order != null)
				return false;
		}
		else if (!order.equals(other.order))
			return false;
		if (queryRange == null)
		{
			if (other.queryRange != null)
				return false;
		}
		else if (!queryRange.equals(other.queryRange))
			return false;
		return true;
	}

	public static Order nameAscOrder()
	{
		return new Order(ResultFileInfo.FIELD_NAME, Order.ASC);
	}

	public static Order nameDescOrder()
	{
		return new Order(ResultFileInfo.FIELD_NAME, Order.DESC);
	}

	public static Order sizeAscOrder()
	{
		return new Order(ResultFileInfo.FIELD_BYTES, Order.ASC);
	}

	public static Order sizeDescOrder()
	{
		return new Order(ResultFileInfo.FIELD_BYTES, Order.DESC);
	}

	public static Order lastModifiedAscOrder()
	{
		return new Order(ResultFileInfo.FIELD_LAST_MODIFIED, Order.ASC);
	}

	public static Order lastModifiedDescOrder()
	{
		return new Order(ResultFileInfo.FIELD_LAST_MODIFIED, Order.DESC);
	}
}
