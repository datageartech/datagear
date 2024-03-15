/*
 * Copyright 2018-present datagear.tech
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

/**
 * SQL表达式参数值映射异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValueExpressionException extends SqlParamValueMapperException
{
	private static final long serialVersionUID = 1L;

	private String expression;

	public SqlParamValueExpressionException(Table table, Column column, Object value, String expression)
	{
		super(table, column, value);
		this.expression = expression;
	}

	public SqlParamValueExpressionException(Table table, Column column, Object value, String expression, String message)
	{
		super(table, column, value, message);
		this.expression = expression;
	}

	public SqlParamValueExpressionException(Table table, Column column, Object value, String expression,
			Throwable cause)
	{
		super(table, column, value, cause);
		this.expression = expression;
	}

	public SqlParamValueExpressionException(Table table, Column column, Object value, String expression, String message,
			Throwable cause)
	{
		super(table, column, value, message, cause);
		this.expression = expression;
	}

	public String getExpression()
	{
		return expression;
	}

	protected void setExpression(String expression)
	{
		this.expression = expression;
	}
}
