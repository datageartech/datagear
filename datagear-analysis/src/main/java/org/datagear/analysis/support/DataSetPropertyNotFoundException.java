/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;

/**
 * 指定名称的{@linkplain DataSetProperty}未找到异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetPropertyNotFoundException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private String name;

	public DataSetPropertyNotFoundException(String name)
	{
		super("Property [" + name + "] not found");
		this.name = name;
	}

	public DataSetPropertyNotFoundException(String name, String message)
	{
		super(message);
		this.name = name;
	}

	public DataSetPropertyNotFoundException(String name, Throwable cause)
	{
		super(cause);
		this.name = name;
	}

	public DataSetPropertyNotFoundException(String name, String message, Throwable cause)
	{
		super(message, cause);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	protected void setName(String name)
	{
		this.name = name;
	}
}
