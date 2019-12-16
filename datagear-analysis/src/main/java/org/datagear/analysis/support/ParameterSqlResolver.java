/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;

import org.datagear.util.expression.Expression;
import org.datagear.util.expression.ExpressionResolver;

/**
 * 参数SQL解析器。
 * <p>
 * 它解析包含<code>...${parameter0}...${parameter1}</code>的SQL语句，并将它们替换为JDBC规范的<code>?</code>占位符。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ParameterSqlResolver
{
	private ExpressionResolver expressionResolver;

	public ParameterSqlResolver()
	{
		super();
		this.expressionResolver = new ExpressionResolver();
		this.expressionResolver.setStartIdentifier(ExpressionResolver.DEFAULT_START_IDENTIFIER_DOLLAR);
		this.expressionResolver.setEndIdentifier(ExpressionResolver.DEFAULT_END_IDENTIFIER);
	}

	protected ExpressionResolver getExpressionResolver()
	{
		return expressionResolver;
	}

	protected void setExpressionResolver(ExpressionResolver expressionResolver)
	{
		this.expressionResolver = expressionResolver;
	}

	/**
	 * 解析包含参数的SQL语句。
	 * 
	 * @param parameterSql
	 * @return
	 */
	public ParameterSql resolve(String parameterSql)
	{
		List<Expression> expressions = this.expressionResolver.resolve(parameterSql);

		if (expressions == null || expressions.isEmpty())
			return new ParameterSql(parameterSql);

		List<String> parameters = new ArrayList<String>(expressions.size());

		List<String> values = new ArrayList<String>(expressions.size());

		for (Expression expression : expressions)
		{
			parameters.add(expression.getContent());
			values.add("?");
		}

		String sql = this.expressionResolver.evaluate(parameterSql, expressions, values, "?");

		return new ParameterSql(sql, parameters);
	}

	/**
	 * 参数SQL解析结果。
	 * <p>
	 * 它的{@linkplain #getSql()}中的<code>...${parameter}...</code>已被替换为<code>?</code>。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ParameterSql
	{
		private String sql;

		private List<String> parameters;

		public ParameterSql()
		{
			super();
		}

		public ParameterSql(String sql)
		{
			super();
			this.sql = sql;
		}

		public ParameterSql(String sql, List<String> parameters)
		{
			super();
			this.sql = sql;
			this.parameters = parameters;
		}

		public String getSql()
		{
			return sql;
		}

		public void setSql(String sql)
		{
			this.sql = sql;
		}

		public boolean hasParameter()
		{
			return (this.parameters != null && !this.parameters.isEmpty());
		}

		public List<String> getParameters()
		{
			return parameters;
		}

		public void setParameters(List<String> parameters)
		{
			this.parameters = parameters;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [sql=" + sql + ", parameters=" + parameters + "]";
		}
	}
}
