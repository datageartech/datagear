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

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;
import org.datagear.util.SqlType;

/**
 * 不支持指定SQL类型异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetUnsupportedSqlTypeException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private SqlType sqlType;

	private String columnName;

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType)
	{
		super();
		this.sqlType = sqlType;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String columnName)
	{
		super();
		this.sqlType = sqlType;
		this.columnName = columnName;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String columnName, String message)
	{
		super(message);
		this.sqlType = sqlType;
		this.columnName = columnName;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String columnName, Throwable cause)
	{
		super(cause);
		this.sqlType = sqlType;
		this.columnName = columnName;
	}

	public SqlDataSetUnsupportedSqlTypeException(SqlType sqlType, String columnName, String message, Throwable cause)
	{
		super(message, cause);
		this.sqlType = sqlType;
		this.columnName = columnName;
	}

	public SqlType getSqlType()
	{
		return sqlType;
	}

	protected void setSqlType(SqlType sqlType)
	{
		this.sqlType = sqlType;
	}

	public boolean hasColumnName()
	{
		return (this.columnName != null && !this.columnName.isEmpty());
	}

	public String getColumnName()
	{
		return columnName;
	}

	protected void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}
}
