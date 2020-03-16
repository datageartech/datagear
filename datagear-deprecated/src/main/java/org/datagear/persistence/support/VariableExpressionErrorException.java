/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.util.expression.Expression;

/**
 * 变量表达式出错异常。
 * 
 * @author datagear@163.com
 *
 */
public class VariableExpressionErrorException extends ExpressionErrorException
{
	private static final long serialVersionUID = 1L;

	public VariableExpressionErrorException(Expression expression)
	{
		super(expression);
	}

	public VariableExpressionErrorException(Expression expression, Throwable cause)
	{
		super(expression, cause);
	}
}
