/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support.expression;

import org.datagear.util.expression.Expression;

/**
 * SQL表达式出错异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlExpressionErrorException extends ExpressionErrorException
{
	private static final long serialVersionUID = 1L;

	public SqlExpressionErrorException(Expression expression)
	{
		super(expression);
	}

	public SqlExpressionErrorException(Expression expression, Throwable cause)
	{
		super(expression, cause);
	}
}
