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

package org.datagear.connection;

import java.io.Serializable;

/**
 * 驱动库信息。
 * 
 * @author datagear@163.com
 *
 */
public class DriverLibraryInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 库名称 */
	private String name;

	/** 库字节数大小 */
	private long size;

	public DriverLibraryInfo()
	{
		super();
	}

	public DriverLibraryInfo(String name, long size)
	{
		super();
		this.name = name;
		this.size = size;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", size=" + size + "]";
	}
}
