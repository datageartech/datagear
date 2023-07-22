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

package org.datagear.dataexchange;

/**
 * 前置依赖的子数据交换失败而导致的{@linkplain SubmitFailReason}。
 * 
 * @author datagear@163.com
 *
 */
public class DependentSubmitFailReason extends SubmitFailReason
{
	/** 前置子数据交换ID */
	private String dependentId;

	public DependentSubmitFailReason()
	{
		super();
	}

	public DependentSubmitFailReason(String dependentId)
	{
		super("Dependent [" + dependentId + "] failed");
		this.dependentId = dependentId;
	}

	public String getDependentId()
	{
		return dependentId;
	}

	public void setDependentId(String dependentId)
	{
		this.dependentId = dependentId;
	}
}
