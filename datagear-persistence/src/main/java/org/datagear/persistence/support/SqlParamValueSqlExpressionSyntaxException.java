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
 * SQL表达式参数值SQL表达式语法出错异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValueSqlExpressionSyntaxException extends SqlParamValueSqlExpressionException
{
	private static final long serialVersionUID = 1L;

	public SqlParamValueSqlExpressionSyntaxException(Table table, Column column, Object value, String expression)
	{
		super(table, column, value, expression);
	}

	public SqlParamValueSqlExpressionSyntaxException(Table table, Column column, Object value, String expression,
			String message)
	{
		super(table, column, value, expression, message);
	}

	public SqlParamValueSqlExpressionSyntaxException(Table table, Column column, Object value, String expression,
			Throwable cause)
	{
		super(table, column, value, expression, cause);
	}

	public SqlParamValueSqlExpressionSyntaxException(Table table, Column column, Object value, String expression,
			String message, Throwable cause)
	{
		super(table, column, value, expression, message, cause);
	}
}
