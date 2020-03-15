/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

	/** 查询关键字 */
	private String keyword;

	/** 查询条件 */
	private String condition;

	/** 排序方式 */
	private Order[] orders;

	/** 查询参数 */
	private Map<String, Object> params = new HashMap<String, Object>();

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

	public Map<String, Object> getParams()
	{
		return params;
	}

	public void setParam(String name, Object value)
	{
		this.params.put(name, value);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [keyword=" + keyword + ", condition=" + condition + ", orders="
				+ Arrays.toString(orders)
				+ ", params=" + params + ", notLike=" + notLike + "]";
	}
}
