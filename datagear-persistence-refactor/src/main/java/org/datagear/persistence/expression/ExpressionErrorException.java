/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.expression;

import org.datagear.persistence.PersistenceException;
import org.datagear.util.expression.Expression;

/**
 * {@linkplain Expression}执行出错异常。
 * 
 * @author datagear@163.com
 *
 */
public class ExpressionErrorException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	private Expression expression;

	public ExpressionErrorException(Expression expression)
	{
		super();
		this.expression = expression;
	}

	public ExpressionErrorException(Expression expression, Throwable cause)
	{
		super("Expression [" + expression.getContent() + "] error", cause);
		this.expression = expression;
	}

	public Expression getExpression()
	{
		return expression;
	}
}
