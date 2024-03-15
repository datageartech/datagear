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

	private String name;

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
		this.name = id;
		this.dataExchange = dataExchange;
	}

	public SubDataExchange(String id, String name, DataExchange dataExchange)
	{
		super();
		this.id = id;
		this.name = name;
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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
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
		return getClass().getSimpleName() + " [id=" + id + ", name=" + name + "]";
	}
}
