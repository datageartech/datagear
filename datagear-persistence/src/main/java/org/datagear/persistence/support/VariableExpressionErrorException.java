/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.persistence.support.ExpressionResolver.Expression;

/**
 * 变量表达式执行出错。
 * 
 * @author datagear@163.com
 *
 */
public class VariableExpressionErrorException extends ExpressionErrorException
{
	private static final long serialVersionUID = 1L;

	public VariableExpressionErrorException(Expression expression, Exception cause)
	{
		super(expression, cause);
	}
}
