/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.meta.Column;
import org.datagear.meta.Table;

/**
 * SQL表达式参数值变量表达式异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValueVariableExpressionException extends SqlParamValueExpressionException
{
	private static final long serialVersionUID = 1L;

	public SqlParamValueVariableExpressionException(Table table, Column column, Object value, String expression)
	{
		super(table, column, value, expression);
	}

	public SqlParamValueVariableExpressionException(Table table, Column column, Object value, String expression,
			String message)
	{
		super(table, column, value, expression, message);
	}

	public SqlParamValueVariableExpressionException(Table table, Column column, Object value, String expression,
			Throwable cause)
	{
		super(table, column, value, expression, cause);
	}

	public SqlParamValueVariableExpressionException(Table table, Column column, Object value, String expression,
			String message, Throwable cause)
	{
		super(table, column, value, expression, message, cause);
	}
}
