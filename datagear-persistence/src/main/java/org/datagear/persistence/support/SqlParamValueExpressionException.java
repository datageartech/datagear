/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
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
