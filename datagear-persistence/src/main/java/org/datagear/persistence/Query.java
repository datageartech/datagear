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

import java.util.Arrays;

import org.datagear.util.StringUtil;
import org.datagear.util.query.KeywordQuery;

/**
 * 查询。
 *
 * @author datagear@163.com
 *
 */
public class Query extends KeywordQuery
{
	private static final long serialVersionUID = 1L;

	/** 查询条件 */
	private String condition;

	/** 排序方式 */
	private Order[] orders;

	/** 针对keyword，是否使用“NOT LIKE”而非“LIKE” */
	private boolean notLike = false;

	public Query()
	{
		super();
	}

	public Query(String keyword)
	{
		super(keyword);
	}

	public Query(String keyword, String condition)
	{
		super(keyword);
		this.condition = condition;
	}

	public boolean hasKeyword()
	{
		return !StringUtil.isEmpty(getKeyword());
	}

	public boolean isNotLike()
	{
		return notLike;
	}

	public void setNotLike(boolean notLike)
	{
		this.notLike = notLike;
	}

	public boolean hasCondition()
	{
		return (this.condition != null && !this.condition.isEmpty());
	}

	public String getCondition()
	{
		return condition;
	}

	public void setCondition(String condition)
	{
		this.condition = condition;
	}

	public boolean hasOrder()
	{
		return (this.orders != null && this.orders.length > 0);
	}

	public Order[] getOrders()
	{
		return orders;
	}

	public void setOrders(Order... orders)
	{
		this.orders = orders;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + (notLike ? 1231 : 1237);
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
		if (condition == null)
		{
			if (other.condition != null)
				return false;
		}
		else if (!condition.equals(other.condition))
			return false;
		if (notLike != other.notLike)
			return false;
		if (!Arrays.equals(orders, other.orders))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [keyword=" + getKeyword() + ", condition=" + condition + ", orders="
				+ Arrays.toString(orders) + ", notLike=" + notLike + "]";
	}
}
