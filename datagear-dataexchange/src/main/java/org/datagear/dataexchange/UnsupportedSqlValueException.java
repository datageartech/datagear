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
 * 不支持指定SQL数据类型的值。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedSqlValueException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedSqlValueException()
	{
		super();
	}

	public UnsupportedSqlValueException(int sqlType, Object sqlValue)
	{
		super("Value [" + sqlValue + "] for sql type [" + sqlType + "] unsupported");
	}

	public UnsupportedSqlValueException(String message)
	{
		super(message);
	}

	public UnsupportedSqlValueException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedSqlValueException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
