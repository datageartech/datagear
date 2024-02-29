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

package org.datagear.persistence.support;

import org.datagear.meta.Column;
import org.datagear.meta.Table;

/**
 * SQL表达式参数值SQL表达式异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValueSqlExpressionException extends SqlParamValueExpressionException
{
	private static final long serialVersionUID = 1L;

	public SqlParamValueSqlExpressionException(Table table, Column column, Object value, String expression)
	{
		super(table, column, value, expression);
	}

	public SqlParamValueSqlExpressionException(Table table, Column column, Object value, String expression,
			String message)
	{
		super(table, column, value, expression, message);
	}

	public SqlParamValueSqlExpressionException(Table table, Column column, Object value, String expression,
			Throwable cause)
	{
		super(table, column, value, expression, cause);
	}

	public SqlParamValueSqlExpressionException(Table table, Column column, Object value, String expression,
			String message, Throwable cause)
	{
		super(table, column, value, expression, message, cause);
	}
}
