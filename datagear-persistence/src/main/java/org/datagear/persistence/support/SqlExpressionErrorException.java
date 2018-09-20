/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support;

import java.sql.SQLException;

import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.support.ExpressionResolver.Expression;

/**
 * SQL {@linkplain Expression}执行错误。
 * 
 * @author datagear@163.com
 *
 */
public class SqlExpressionErrorException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	private Expression expression;

	public SqlExpressionErrorException(Expression expression, SQLException cause)
	{
		super(expression.getContent(), cause);
		this.expression = expression;
	}

	public Expression getExpression()
	{
		return expression;
	}
}
