/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.meta.Column;
import org.datagear.meta.Table;

/**
 * SQL表达式参数值变量表达式语法错误异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValueVariableExpressionSyntaxException extends SqlParamValueVariableExpressionException
{
	private static final long serialVersionUID = 1L;

	public SqlParamValueVariableExpressionSyntaxException(Table table, Column column, Object value, String expression)
	{
		super(table, column, value, expression);
	}

	public SqlParamValueVariableExpressionSyntaxException(Table table, Column column, Object value, String expression,
			String message)
	{
		super(table, column, value, expression, message);
	}

	public SqlParamValueVariableExpressionSyntaxException(Table table, Column column, Object value, String expression,
			Throwable cause)
	{
		super(table, column, value, expression, cause);
	}

	public SqlParamValueVariableExpressionSyntaxException(Table table, Column column, Object value, String expression,
			String message, Throwable cause)
	{
		super(table, column, value, expression, message, cause);
	}
}
