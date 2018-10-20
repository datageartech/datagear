/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.datagear.connection.JdbcUtil;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.UnsupportedModelCharacterException;
import org.datagear.persistence.support.ExpressionResolver.Expression;
import org.springframework.core.convert.ConversionService;

/**
 * 抽象持久化操作类。
 * <p>
 * 此类是持久化操作类（insert、update、delete、select等）的上级类，封装公用方法。
 * </p>
 * <p>
 * 如果把所有持久化操作都封装到一个类中，会使这个类非常庞大，难于维护（之前的实现{@code DefaultPersistenceManager}
 * 即是如此），因此考虑按照操作类型拆分类。
 * </p>
 * 
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractModelPersistenceOperation extends AbstractModelDataAccessObject
{
	public AbstractModelPersistenceOperation()
	{
		super();
	}

	/**
	 * 计算给定表达式的真正属性值。
	 * 
	 * @param cn
	 * @param model
	 * @param property
	 * @param expressionPropValue
	 * @param expressions
	 * @param expressionValueCache
	 * @param conversionService
	 * @param expressionResolver
	 * @return
	 */
	protected Object evaluatePropertyValueForQueryExpressions(Connection cn, Model model, Property property,
			String expressionPropValue, List<Expression> expressions, Map<String, Object> expressionValueCache,
			ConversionService conversionService, ExpressionResolver expressionResolver)
	{
		if (!MU.isSingleProperty(property) || !MU.isConcretePrimitiveProperty(property))
			throw new UnsupportedModelCharacterException("[" + model + "] 's [" + property + "] is sql expression ["
					+ expressionPropValue + "], it must be single, concrete and primitive.");

		List<Object> expressionValues = new ArrayList<Object>();

		for (int i = 0, len = expressions.size(); i < len; i++)
		{
			Expression expression = expressions.get(i);

			String cacheKey = (expression.hasName() ? expression.getName() : expression.getContent());

			if (expressionValueCache.containsKey(cacheKey))
			{
				Object value = expressionValueCache.get(cacheKey);
				expressionValues.add(value);
			}
			else if (!isSelectSql(expression.getContent()))
			{
				expressionValues.add(expression.getExpression());
			}
			else
			{
				Statement st = null;
				ResultSet rs = null;
				try
				{
					st = cn.createStatement();
					rs = st.executeQuery(expression.getContent());

					Object value = null;

					if (rs.next())
						value = rs.getObject(1);

					expressionValues.add(value);
					expressionValueCache.put(cacheKey, value);
				}
				catch (SQLException e)
				{
					throw new SqlExpressionErrorException(expression, e);
				}
				finally
				{
					JdbcUtil.closeResultSet(rs);
					JdbcUtil.closeStatement(st);
				}
			}
		}

		String evaluated = expressionResolver.evaluate(expressionPropValue, expressions, expressionValues, "");

		return conversionService.convert(evaluated, property.getModel().getType());
	}

	/**
	 * 判断给定SQL语句是否是“SELECT”语句。
	 * 
	 * @param sql
	 * @return
	 */
	protected boolean isSelectSql(String sql)
	{
		if (sql == null || sql.isEmpty())
			return false;

		return Pattern.matches(SELECT_SQL_REGEX, sql);
	}

	protected static final String SELECT_SQL_REGEX = "^\\s*((?i)select)\\s+\\S+[\\s\\S]*$";
}
