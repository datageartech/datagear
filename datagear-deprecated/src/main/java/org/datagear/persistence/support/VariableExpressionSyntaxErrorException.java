/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.util.expression.Expression;

/**
 * 变量表达式语法错误异常。
 * 
 * @author datagear@163.com
 *
 */
public class VariableExpressionSyntaxErrorException extends VariableExpressionErrorException
{
	private static final long serialVersionUID = 1L;

	public VariableExpressionSyntaxErrorException(Expression expression, Throwable cause)
	{
		super(expression, cause);
	}

	public VariableExpressionSyntaxErrorException(Expression expression)
	{
		super(expression);
	}
}
