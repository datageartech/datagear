/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.SQLException;

import org.datagear.persistence.support.ExpressionResolver.Expression;

/**
 * SQL {@linkplain Expression}执行错误。
 * 
 * @author datagear@163.com
 *
 */
public class SqlExpressionErrorException extends ExpressionErrorException
{
	private static final long serialVersionUID = 1L;

	public SqlExpressionErrorException(Expression expression, SQLException cause)
	{
		super(expression, cause);
	}

	@Override
	public SQLException getCause()
	{
		return (SQLException) super.getCause();
	}
}
