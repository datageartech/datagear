/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
