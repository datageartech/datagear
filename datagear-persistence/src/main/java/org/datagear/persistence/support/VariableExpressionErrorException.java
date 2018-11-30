/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.persistence.support.ExpressionResolver.Expression;
import org.springframework.expression.ExpressionException;

/**
 * 变量表达式执行出错。
 * 
 * @author datagear@163.com
 *
 */
public class VariableExpressionErrorException extends ExpressionErrorException
{
	private static final long serialVersionUID = 1L;

	public VariableExpressionErrorException(Expression expression, ExpressionException cause)
	{
		super(expression, cause);
	}

	@Override
	public ExpressionException getCause()
	{
		return (ExpressionException) super.getCause();
	}
}
