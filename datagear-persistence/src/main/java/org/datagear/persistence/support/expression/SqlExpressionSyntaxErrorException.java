/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support.expression;

import org.datagear.util.expression.Expression;

/**
 * SQL表达式语法出错异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlExpressionSyntaxErrorException extends SqlExpressionErrorException
{
	private static final long serialVersionUID = 1L;

	public SqlExpressionSyntaxErrorException(Expression expression)
	{
		super(expression);
	}

	public SqlExpressionSyntaxErrorException(Expression expression, Throwable cause)
	{
		super(expression, cause);
	}
}
