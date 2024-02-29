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

package org.datagear.dataexchange;

/**
 * 不支持指定SQL类型异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedSqlTypeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedSqlTypeException()
	{
		super();
	}

	public UnsupportedSqlTypeException(int sqlType)
	{
		super("Sql type [" + sqlType + "] is not supported");
	}

	public UnsupportedSqlTypeException(String message)
	{
		super(message);
	}

	public UnsupportedSqlTypeException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedSqlTypeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
