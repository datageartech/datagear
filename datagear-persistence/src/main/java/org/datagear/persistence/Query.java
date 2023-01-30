/*
 * Copyright 2018-2023 datagear.tech
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

import java.io.Serializable;
import java.util.Arrays;

/**
 * 查询。
 *
 * @author datagear@163.com
 *
 */
public class Query implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 查询关键字 */
	private String keyword;

	/** 查询条件 */
	private String condition;

	/** 排序方式 */
	private Order[] orders;

	/** 针对keyword，是否使用“NOT LIKE”而非“LIKE” */
	private boolean notLike = false;

	public Query()
	{
	}

	public Query(String keyword)
	{
		this.keyword = keyword;
	}

	public Query(String keyword, String condition)
	{
		super();
		this.keyword = keyword;
		this.condition = condition;
	}

	public boolean hasKeyword()
	{
		return (this.keyword != null && !this.keyword.isEmpty());
	}

	public String getKeyword()
	{
		return keyword;
	}

	public void setKeyword(String keyword)
	{
		this.keyword = keyword;
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
	public String toString()
	{
		return getClass().getSimpleName() + " [keyword=" + keyword + ", condition=" + condition + ", orders="
				+ Arrays.toString(orders) + ", notLike=" + notLike + "]";
	}
}
