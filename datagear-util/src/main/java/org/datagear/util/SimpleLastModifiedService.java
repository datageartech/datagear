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

package org.datagear.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 简单基于{@linkplain ConcurrentMap}的{@linkplain LastModifiedService}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleLastModifiedService implements LastModifiedService
{
	private ConcurrentMap<String, Long> lastModifieds = new ConcurrentHashMap<>();

	public SimpleLastModifiedService()
	{
		super();
	}

	@Override
	public long getLastModified(String name)
	{
		Long v = this.lastModifieds.get(name);
		return (v == null ? LAST_MODIFIED_UNSET : v.longValue());
	}

	@Override
	public void setLastModified(String name, long lastModified)
	{
		this.lastModifieds.put(name, lastModified);
	}

	@Override
	public long setLastModifiedNow(String name)
	{
		long v = System.currentTimeMillis();
		setLastModified(name, v);

		return v;
	}

	@Override
	public boolean isModified(String name, long lastModified)
	{
		long v = getLastModified(name);
		return (v != lastModified);
	}
}
