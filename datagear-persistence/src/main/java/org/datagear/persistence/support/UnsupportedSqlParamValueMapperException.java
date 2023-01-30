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

package org.datagear.persistence.support;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.util.SqlParamValue;

/**
 * 不支持{@linkplain SqlParamValue}映射异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedSqlParamValueMapperException extends SqlParamValueMapperException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value)
	{
		super(table, column, value);
	}

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value, Throwable cause)
	{
		super(table, column, value, cause);
	}

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value, String message)
	{
		super(table, column, value, message);
	}

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value, String message,
			Throwable cause)
	{
		super(table, column, value, message, cause);
	}

}
