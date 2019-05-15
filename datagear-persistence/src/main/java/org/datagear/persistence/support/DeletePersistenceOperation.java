/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.SqlBuilder;
import org.datagear.persistence.features.KeyRule;
import org.datagear.persistence.mapper.JoinTableMapper;
import org.datagear.persistence.mapper.Mapper;
import org.datagear.persistence.mapper.MapperUtil;
import org.datagear.persistence.mapper.ModelTableMapper;
import org.datagear.persistence.mapper.PropertyTableMapper;

/**
 * 删除持久化操作。
 * 
 * @author datagear@163.com
 *
 */
public class DeletePersistenceOperation extends AbstractModelPersistenceOperation
{
	public DeletePersistenceOperation()
	{
		super();
	}

	/**
	 * 根据ID删除。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param ids
	 * @return
	 */
	public int deleteById(Connection cn, Dialect dialect, String table, Model model, Object[]... ids)
	{
		SqlBuilder idCondition = buildIdCondition(cn, dialect, model, ids);

		return delete(cn, dialect, table, model, idCondition, null);
	}

	/**
	 * 删除对象。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param objs
	 * @return
	 */
	public int delete(Connection cn, Dialect dialect, String table, Model model, Object[] objs)
	{
		SqlBuilder condition = buildRecordCondition(cn, dialect, model, objs, null);

		int count = delete(cn, dialect, table, model, condition, null);

		return count;
	}

	/**
	 * 删除属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param property
	 * @param propertyTableCondition
	 *            允许为{@code null}。
	 * @return
	 * 
	 */
	public int deletePropertyTableData(Connection cn, Dialect dialect, String table, Model model, SqlBuilder condition,
			Property property, SqlBuilder propertyTableCondition)
	{
		Mapper mapper = getMapper(model, property);

		return deletePropertyTableData(cn, dialect, table, model, condition, property, mapper, propertyTableCondition,
				true);
	}

	/**
	 * 删除数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param ignorePropertyName
	 *            忽略的属性名称，用于处理双向关联时，允许为{@code null}
	 * @return
	 * 
	 */
	protected int delete(Connection cn, Dialect dialect, String table, Model model, SqlBuilder condition,
			String ignorePropertyName)
	{
		deletePropertyTableDataForDeleteModelTableData(cn, dialect, table, model, condition, ignorePropertyName);

		return deleteModelTableData(cn, dialect, table, model, condition);
	}

	/**
	 * 删除模型表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @return
	 * 
	 */
	protected int deleteModelTableData(Connection cn, Dialect dialect, String table, Model model, SqlBuilder condition)
	{
		SqlBuilder sql = SqlBuilder.valueOf();

		sql.sql("DELETE FROM ").sql(toQuoteName(dialect, table));

		if (condition != null)
			sql.sql(" WHERE ").sql(condition);

		return executeUpdate(cn, sql);
	}

	/**
	 * 删除属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param ignorePropertyName
	 *            忽略的属性名称，用于处理双向关联时，允许为{@code null}
	 */
	protected void deletePropertyTableDataForDeleteModelTableData(Connection cn, Dialect dialect, String table,
			Model model, SqlBuilder condition, String ignorePropertyName)
	{
		Property[] properties = model.getProperties();

		for (int i = 0; i < properties.length; i++)
		{
			Property property = properties[i];

			if (ignorePropertyName != null && ignorePropertyName.equals(property.getName()))
				continue;

			Mapper mapper = getMapper(model, property);

			KeyRule propertyKeyDeleteRule = mapper.getPropertyKeyDeleteRule();

			if (propertyKeyDeleteRule != null && !propertyKeyDeleteRule.isManually())
				continue;

			deletePropertyTableData(cn, dialect, table, model, condition, property, mapper, null, false);
		}
	}

	/**
	 * 删除属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param ignorePropertyName
	 *            忽略的属性名称，用于处理双向关联时，允许为{@code null}
	 * @param updateModelTable
	 *            是否更新模型表数据
	 */
	protected void deletePropertyTableData(Connection cn, Dialect dialect, String table, Model model,
			SqlBuilder condition, String ignorePropertyName, boolean updateModelTable)
	{
		Property[] properties = model.getProperties();

		for (int i = 0; i < properties.length; i++)
		{
			Property property = properties[i];

			if (ignorePropertyName != null && ignorePropertyName.equals(property.getName()))
				continue;

			deletePropertyTableData(cn, dialect, table, model, condition, property, getMapper(model, property), null,
					updateModelTable);
		}
	}

	/**
	 * 删除属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param property
	 * @param mapper
	 * @param propertyTableCondition
	 *            允许为{@code null}。
	 * @param updateModelTable
	 *            是否更新模型表数据
	 * @return
	 * 
	 */
	protected int deletePropertyTableData(Connection cn, Dialect dialect, String table, Model model,
			SqlBuilder condition, Property property, Mapper mapper, SqlBuilder propertyTableCondition,
			boolean updateModelTable)
	{
		int re = 0;

		if (MapperUtil.isModelTableMapper(mapper))
		{
			ModelTableMapper pmm = MapperUtil.castModelTableMapper(mapper);

			re = deletePropertyTableDataForModelTableMapper(cn, dialect, table, model, condition, property, pmm,
					propertyTableCondition, updateModelTable);
		}
		else if (MapperUtil.isPropertyTableMapper(mapper))
		{
			PropertyTableMapper pmm = MapperUtil.castPropertyTableMapper(mapper);

			re = deletePropertyTableDataForPropertyTableMapper(cn, dialect, table, model, condition, property, pmm,
					propertyTableCondition);
		}
		else if (MapperUtil.isJoinTableMapper(mapper))
		{
			JoinTableMapper pmm = MapperUtil.castJoinTableMapper(mapper);

			re = deletePropertyTableDataForJoinTableMapper(cn, dialect, table, model, condition, property, pmm,
					propertyTableCondition);
		}
		else
			throw new UnsupportedOperationException();

		return re;
	}

	/**
	 * 删除属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param property
	 * @param mapper
	 * @param propertyTableCondition
	 *            允许为{@code null}。
	 * @param updateModelTable
	 *            是否更新模型表数据
	 * @return
	 * 
	 */
	protected int deletePropertyTableDataForModelTableMapper(Connection cn, Dialect dialect, String table, Model model,
			SqlBuilder condition, Property property, ModelTableMapper mapper, SqlBuilder propertyTableCondition,
			boolean updateModelTable)
	{
		if (mapper.isPrimitivePropertyMapper())
		{
			if (updateModelTable)
				return updatePropertyValueToNullForModelTableMapper(cn, dialect, table, model, propertyTableCondition,
						property, mapper);
			else
				return PersistenceManager.PERSISTENCE_IGNORED;
		}
		else
		{
			Model pmodel = MU.getModel(property);

			if (PMU.isPrivate(model, property))
			{
				// XXX 这里只能先删除属性实体，因为如果先删除关联表，就无法构建这个删除条件了
				SqlBuilder pcondition = buildPropertyTableConditionForEntityModelTableMapper(dialect, table, model,
						condition, property, mapper, propertyTableCondition);

				int deleted = delete(cn, dialect, getTableName(pmodel), pmodel, pcondition, getMappedByWith(mapper));

				if (updateModelTable)
					updatePropertyValueToNullForModelTableMapper(cn, dialect, table, model, propertyTableCondition,
							property, mapper);

				return deleted;
			}
			else
			{
				if (updateModelTable)
					return updatePropertyValueToNullForModelTableMapper(cn, dialect, table, model,
							propertyTableCondition, property, mapper);
				else
					return PersistenceManager.PERSISTENCE_IGNORED;
			}
		}
	}

	/**
	 * 删除属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param property
	 * @param mapper
	 * @param propertyTableCondition
	 *            允许为{@code null}。
	 */
	protected int deletePropertyTableDataForPropertyTableMapper(Connection cn, Dialect dialect, String table,
			Model model, SqlBuilder condition, Property property, PropertyTableMapper mapper,
			SqlBuilder propertyTableCondition)
	{
		int count = 0;

		if (mapper.isPrimitivePropertyMapper())
		{
			String ptable = toQuoteName(dialect, mapper.getPrimitiveTableName());
			String[] modelKeyColumnNames = toQuoteNames(dialect, mapper.getModelKeyColumnNames());

			SqlBuilder sql = SqlBuilder.valueOf();

			sql.sql("DELETE FROM ").sql(ptable).sql(" WHERE ").sql("(").delimit(",").sqld(modelKeyColumnNames)
					.sql(") IN (")
					.sql(buildKeyQuery(dialect, table, model, getModelKeyProperties(mapper, model), condition))
					.sql(")");

			if (mapper.hasModelConcreteColumn())
			{
				String mconcreteColumnName = toQuoteName(dialect, mapper.getModelConcreteColumnName());
				sql.sql(" AND ").sql(mconcreteColumnName).sql("=?").arg(mapper.getModelConcreteColumnValue());
			}

			if (propertyTableCondition != null)
				sql.sql(" AND (").sql(propertyTableCondition).sql(")");

			count = executeUpdate(cn, sql);
		}
		else
		{
			Model pmodel = MU.getModel(property);

			if (PMU.isPrivate(model, property))
			{
				SqlBuilder pcondition = buildPropertyTableConditionForPropertyTableMapper(dialect, table, model,
						condition, property, mapper, propertyTableCondition);

				count = delete(cn, dialect, getTableName(pmodel), pmodel, pcondition, getMappedByWith(mapper));
			}
			else
			{
				count = updateModelKeyToNullForEntityPropertyTableMapper(cn, dialect, table, model, condition, property,
						mapper, propertyTableCondition);
			}
		}

		return count;
	}

	/**
	 * 删除属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param property
	 * @param mapper
	 * @param propertyTableCondition
	 *            允许为{@code null}。
	 * @return
	 */
	protected int deletePropertyTableDataForJoinTableMapper(Connection cn, Dialect dialect, String table, Model model,
			SqlBuilder condition, Property property, JoinTableMapper mapper, SqlBuilder propertyTableCondition)
	{
		int count = 0;

		Model propertyModel = MU.getModel(property);

		String jointable = mapper.getJoinTableName();
		String propertyTable = getTableName(propertyModel);
		String[] modelKeyColumnNames = toQuoteNames(dialect, mapper.getModelKeyColumnNames());
		String[] pkeyColumnNames = toQuoteNames(dialect, mapper.getPropertyKeyColumnNames());

		if (PMU.isPrivate(model, property))
		{
			// XXX 这里只能先删除属性实体，因为如果先删除关联表，就无法构建这个删除条件了
			SqlBuilder pcondition = buildPropertyTableConditionForJoinTableMapper(dialect, table, model, condition,
					property, mapper, propertyTableCondition);

			delete(cn, dialect, propertyTable, propertyModel, pcondition, getMappedByWith(mapper));
		}

		SqlBuilder sql = SqlBuilder.valueOf();

		sql.sql("DELETE FROM ").sql(toQuoteName(dialect, jointable)).sql(" WHERE ").sql("(").delimit(",")
				.sqld(modelKeyColumnNames).sql(") IN (")
				.sql(buildKeyQuery(dialect, table, model, getModelKeyProperties(mapper, model), condition)).sql(")");

		if (mapper.hasModelConcreteColumn())
		{
			String mconcreteColumnName = toQuoteName(dialect, mapper.getModelConcreteColumnName());
			sql.sql(" AND ").sql(mconcreteColumnName).sql("=?").arg(mapper.getModelConcreteColumnValue());
		}

		if (propertyTableCondition != null)
		{
			SqlBuilder ptableQuery = buildKeyQuery(dialect, propertyTable, propertyModel,
					getPropertyKeyProperties(mapper, propertyModel), propertyTableCondition);

			sql.sql(" AND ").sql("(").delimit(",").sqld(pkeyColumnNames).sql(") IN (").sql(ptableQuery).sql(")");
		}

		if (mapper.hasPropertyConcreteColumn())
		{
			String pconcreteColumnName = toQuoteName(dialect, mapper.getPropertyConcreteColumnName());
			sql.sql(" AND ").sql(pconcreteColumnName).sql("=?").arg(mapper.getPropertyConcreteColumnValue());
		}

		count = executeUpdate(cn, sql);

		return count;
	}

	/**
	 * 更新模型表内的属性端外键为{@code null}。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param property
	 * @param mapper
	 * @return
	 * 
	 */
	protected int updatePropertyValueToNullForModelTableMapper(Connection cn, Dialect dialect, String table,
			Model model, SqlBuilder condition, Property property, ModelTableMapper mapper)
	{
		SqlBuilder sql = new SqlBuilder();

		sql.sql("UPDATE ").sql(toQuoteName(dialect, table)).sql(" SET ").delimit(",");

		if (mapper.isPrimitivePropertyMapper())
		{
			sql.sqldSuffix(toQuoteName(dialect, mapper.getPrimitiveColumnName()), " = null");
		}
		else
		{
			String[] pkeyColumnNames = mapper.getPropertyKeyColumnNames();

			for (int i = 0; i < pkeyColumnNames.length; i++)
				sql.sqldSuffix(toQuoteName(dialect, pkeyColumnNames[i]), " = null");

			if (mapper.hasPropertyConcreteColumn())
				sql.sqldSuffix(toQuoteName(dialect, mapper.getPropertyConcreteColumnName()), " = null");

			if (mapper.hasModelOrderColumn())
				sql.sqldSuffix(toQuoteName(dialect, mapper.getModelOrderColumnName()), " = null");
		}

		if (condition != null)
			sql.sql(" WHERE ").sql(condition);

		return executeUpdate(cn, sql);
	}

	/**
	 * 更新属性表内的模型端外键为{@code null}。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            允许为{@code null}。
	 * @param property
	 * @param mapper
	 * @param propertyTableCondition
	 * @return
	 * 
	 */
	protected int updateModelKeyToNullForEntityPropertyTableMapper(Connection cn, Dialect dialect, String table,
			Model model, SqlBuilder condition, Property property, PropertyTableMapper mapper,
			SqlBuilder propertyTableCondition)
	{
		int count = 0;

		Model propertyModel = MU.getModel(property);

		String ptable = toQuoteName(dialect, getTableName(propertyModel));
		String[] modelKeyColumnNames = toQuoteNames(dialect, mapper.getModelKeyColumnNames());

		SqlBuilder sql = SqlBuilder.valueOf();

		sql.sql("UPDATE ").sql(ptable).sql(" SET ").delimit(",").sqldSuffix(modelKeyColumnNames, "= null");

		if (mapper.hasModelConcreteColumn())
		{
			String mconcreteColumnName = toQuoteName(dialect, mapper.getModelConcreteColumnName());
			sql.sqldSuffix(mconcreteColumnName, "= null");
		}

		if (mapper.hasPropertyOrderColumn())
		{
			String porderColumnName = toQuoteName(dialect, mapper.getPropertyOrderColumnName());
			sql.sqldSuffix(porderColumnName, "= null");
		}

		sql.sql(" WHERE ").sql("(").delimit(",").sqld(modelKeyColumnNames).sql(") IN (")
				.sql(buildKeyQuery(dialect, table, model, getModelKeyProperties(mapper, model), condition)).sql(")");

		if (mapper.hasModelConcreteColumn())
		{
			String mconcreteColumnName = toQuoteName(dialect, mapper.getModelConcreteColumnName());
			sql.sql(" AND ").sql(mconcreteColumnName).sql("=?").arg(mapper.getModelConcreteColumnValue());
		}

		if (propertyTableCondition != null)
		{
			sql.sql(" AND (").sql(propertyTableCondition).sql(")");
		}

		count = executeUpdate(cn, sql);

		return count;
	}
}
