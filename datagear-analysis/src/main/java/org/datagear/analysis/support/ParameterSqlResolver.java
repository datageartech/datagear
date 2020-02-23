/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.datagear.util.expression.Expression;
import org.datagear.util.expression.ExpressionResolver;

/**
 * 参数SQL解析器。
 * <p>
 * 它可以解析SQL语句中的<code>#{parameter}</code>内容，并可替换为给定映射表中与<code>parameter</code>对应的值。
 * 
 * @author datagear@163.com
 *
 */
public class ParameterSqlResolver
{
	public static final String LITERAL_PARAM_VALUE_START = ExpressionResolver.DEFAULT_START_IDENTIFIER_DOLLAR;
	public static final String LITERAL_PARAM_VALUE_END = ExpressionResolver.DEFAULT_END_IDENTIFIER;

	private ExpressionResolver expressionResolver;

	public ParameterSqlResolver()
	{
		super();
		this.expressionResolver = new ExpressionResolver();
		this.expressionResolver.setStartIdentifier(ExpressionResolver.DEFAULT_START_IDENTIFIER_SHARP);
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
	 * 解析SQL语句中的所有<code>#{parameter}</code>参数名列表，没有则返回空列表。
	 * 
	 * @param sql
	 * @return
	 */
	public List<String> resolveParams(String sql)
	{
		List<String> params = new ArrayList<String>();

		List<Expression> expressions = this.expressionResolver.resolve(sql);

		if (expressions == null || expressions.isEmpty())
			return params;

		for (Expression expression : expressions)
			params.add(expression.getContent());

		return params;
	}

	/**
	 * 计算包含<code>#{parameter}</code>参数的SQL语句。
	 * <p>
	 * 此方法将SQL语句中的<code>#{parameter}</code>替换为{@code paramValues}中与<code>parameter</code>对应的值，作为返回对象的{@linkplain ParameterSql#getSql()}。
	 * </p>
	 * <p>
	 * 具体的替换规则如下：
	 * </p>
	 * <ul>
	 * <li>如果<code>parameter</code>的值是{@linkplain #isLiteralParamValue(String)
	 * 字面参数值}，则直接使用其中的<code>"..."</code>替换SQL语句中对应内容；</li>
	 * <li>否则，替换为JDBC规范的<code>?</code>占位符，并将<code>parameter</code>加入返回对象的{@linkplain ParameterSql#getNames()}，
	 * 值加入返回对象的{@linkplain ParameterSql#getValues()}中。</li>
	 * </ul>
	 * 
	 * @param sql
	 * @param paramValues
	 * @return
	 */
	public ParameterSql evaluate(String sql, Map<String, ?> paramValues)
	{
		List<Expression> expressions = this.expressionResolver.resolve(sql);

		if (expressions == null || expressions.isEmpty())
			return new ParameterSql(sql);

		List<String> reNames = new ArrayList<String>(expressions.size());
		List<Object> reValues = new ArrayList<Object>(expressions.size());
		List<String> expValues = new ArrayList<String>(expressions.size());

		for (Expression expression : expressions)
		{
			String name = expression.getContent();
			Object value = paramValues.get(name);
			boolean isPrepared = true;

			//如果是${...}格式字符串，则直接替换字面内容
			if (value instanceof String)
			{
				String strValue = ((String) value);
				if (isLiteralParamValue(strValue))
				{
					isPrepared = false;
					strValue = strValue.substring(LITERAL_PARAM_VALUE_START.length(),
							strValue.length() - LITERAL_PARAM_VALUE_END.length());
					expValues.add(strValue);
				}
			}

			if (isPrepared)
			{
				reNames.add(name);
				reValues.add(value);
				expValues.add("?");
			}
		}

		sql = this.expressionResolver.evaluate(sql, expressions, expValues, "?");

		return new ParameterSql(sql, reNames, reValues);
	}

	/**
	 * 是否是字面参数值。
	 * <p>
	 * 字面参数值的格式为：<code>"${...}"</code>
	 * </p>
	 * 
	 * @param paramValue
	 * @return
	 */
	public boolean isLiteralParamValue(String paramValue)
	{
		return (paramValue != null && paramValue.startsWith(LITERAL_PARAM_VALUE_START)
				&& paramValue.endsWith(LITERAL_PARAM_VALUE_END));
	}

	/**
	 * 是否是字面参数值。
	 * 
	 * @param paramValue
	 * @return
	 * @see {@linkplain #isLiteralParamValue(String)}
	 */
	public boolean isLiteralParamValue(Object paramValue)
	{
		if (paramValue == null)
			return false;

		if (!(paramValue instanceof String))
			return false;

		return isLiteralParamValue((String) paramValue);
	}

	/**
	 * 参数SQL解析结果。
	 * <p>
	 * 它的{@linkplain #getSql()}中的<code>...#{parameter}...</code>已被替换。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ParameterSql
	{
		private String sql;

		private List<String> names;

		private List<Object> values;

		public ParameterSql()
		{
			super();
		}

		public ParameterSql(String sql)
		{
			super();
			this.sql = sql;
		}

		public ParameterSql(String sql, List<String> names, List<Object> values)
		{
			super();
			this.sql = sql;
			this.names = names;
			this.values = values;
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
			return (this.names != null && !this.names.isEmpty());
		}

		public List<String> getNames()
		{
			return names;
		}

		public void setNames(List<String> names)
		{
			this.names = names;
		}

		public List<Object> getValues()
		{
			return values;
		}

		public void setValues(List<Object> values)
		{
			this.values = values;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [sql=" + sql + ", names=" + names + ", values=" + values + "]";
		}
	}
}
