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

package org.datagear.web.util.accesslatch;

/**
 * 抽象{@linkplain AccessLatch}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractAccessLatch implements AccessLatch
{
	public AbstractAccessLatch()
	{
		super();
	}

	@Override
	public int remain(String identity, String resource)
	{
		return this.remain(identity, resource, System.currentTimeMillis());
	}

	@Override
	public boolean access(String identity, String resource)
	{
		return access(identity, resource, System.currentTimeMillis());
	}
}
