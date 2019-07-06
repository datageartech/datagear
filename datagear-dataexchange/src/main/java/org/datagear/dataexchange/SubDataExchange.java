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

	private Set<SubDataExchange> dependencies;

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

	public boolean hasDependency()
	{
		return (this.dependencies != null && this.dependencies.size() > 0);
	}

	public Set<SubDataExchange> getDependencies()
	{
		return dependencies;
	}

	@SuppressWarnings("unchecked")
	public void setDependencies(Set<? extends SubDataExchange> dependencies)
	{
		this.dependencies = (Set<SubDataExchange>) dependencies;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + id + "]";
	}
}
