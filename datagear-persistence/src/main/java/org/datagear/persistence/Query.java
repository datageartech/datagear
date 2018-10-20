/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 查询。
 *
 * @author datagear@163.com
 * @createDate 2013-2-4
 *
 */
public class Query implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 针对keyword，是否使用“NOT LIKE”而非“LIKE” */
	private boolean notLike = false;

	/** 查询关键字 */
	private String keyword;

	/** 查询条件 */
	private String condition;

	/** 排序方式 */
	private Order[] orders;

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
		return getClass().getSimpleName() + " [notLike=" + notLike + ", keyword=" + keyword + ", condition=" + condition
				+ ", orders=" + Arrays.toString(orders) + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
		result = prime * result + (notLike ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(orders);
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
		Query other = (Query) obj;
		if (condition == null)
		{
			if (other.condition != null)
				return false;
		}
		else if (!condition.equals(other.condition))
			return false;
		if (keyword == null)
		{
			if (other.keyword != null)
				return false;
		}
		else if (!keyword.equals(other.keyword))
			return false;
		if (notLike != other.notLike)
			return false;
		if (!Arrays.equals(orders, other.orders))
			return false;
		return true;
	}
}
