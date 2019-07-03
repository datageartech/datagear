/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.Set;

/**
 * 子数据交换。
 * 
 * @author datagear@163.com
 *
 */
public class SubDataExchange
{
	private String id;

	private DataExchange dataExchange;

	private Set<SubDataExchange> dependents;

	public SubDataExchange()
	{
		super();
	}

	public SubDataExchange(String id, DataExchange dataExchange)
	{
		super();
		this.id = id;
		this.dataExchange = dataExchange;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public DataExchange getDataExchange()
	{
		return dataExchange;
	}

	public void setDataExchange(DataExchange dataExchange)
	{
		this.dataExchange = dataExchange;
	}

	public boolean hasDependent()
	{
		return (this.dependents != null && this.dependents.size() > 0);
	}

	public Set<SubDataExchange> getDependents()
	{
		return dependents;
	}

	@SuppressWarnings("unchecked")
	public void setDependents(Set<? extends SubDataExchange> dependents)
	{
		this.dependents = (Set<SubDataExchange>) dependents;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + id + "]";
	}
}
