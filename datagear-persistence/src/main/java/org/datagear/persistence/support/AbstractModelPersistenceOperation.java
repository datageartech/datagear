/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.datagear.connection.JdbcUtil;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.model.support.PropertyModel;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.UnsupportedModelCharacterException;
import org.springframework.core.convert.ConversionException;
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
public class AbstractModelPersistenceOperation extends AbstractModelDataAccessObject
{
	public AbstractModelPersistenceOperation()
	{
		super();
	}

	/**
	 * 执行给定的属性值SQL语句并获取真正的属性值。
	 * 
	 * @param cn
	 * @param model
	 * @param property
	 * @param sqlExpression
	 * @param sqlResultMap
	 *            用于缓存SQL表达式求值结果的映射表
	 * @param conversionService
	 * @return
	 */
	protected Object executeQueryForGetPropertySqlValueResult(Connection cn, Model model, Property property,
			String sqlExpression, Map<String, Object> sqlResultMap, ConversionService conversionService)
	{
		if (!MU.isSingleProperty(property) || !MU.isConcretePrimitiveProperty(property))
			throw new UnsupportedModelCharacterException("[" + model + "] 's [" + property + "] is sql value ["
					+ sqlExpression + "], it must be single, concrete and primitive.");

		if (sqlResultMap.containsKey(sqlExpression))
		{
			Object propertyValue = sqlResultMap.get(sqlExpression);

			Model pmodel = property.getModel();

			if (propertyValue != null && !MU.isType(pmodel, propertyValue.getClass()))
			{
				try
				{
					propertyValue = conversionService.convert(propertyValue, pmodel.getType());
				}
				catch (ConversionException e)
				{

				}
			}

			return propertyValue;
		}
		else
		{
			Statement st = null;
			ResultSet rs = null;
			try
			{
				st = cn.createStatement();
				rs = st.executeQuery(PMU.getSqlForSqlExpression(sqlExpression));

				Object propertyValue = null;

				if (rs.next())
					propertyValue = toPropertyValue(cn, rs, 1, 1, model, property, PropertyModel.valueOf(property, 0));

				sqlResultMap.put(sqlExpression, propertyValue);

				return propertyValue;
			}
			catch (SQLException e)
			{
				throw new PersistenceException(e);
			}
			finally
			{
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(st);
			}
		}
	}
}
