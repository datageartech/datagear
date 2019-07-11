/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.features.NotEditable;
import org.datagear.model.features.NotReadable;
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
import org.springframework.core.convert.ConversionService;

/**
 * 更新持久化操作类。
 * 
 * @author datagear@163.com
 *
 */
public class UpdatePersistenceOperation extends AbstractExpressionModelPersistenceOperation
{
	private InsertPersistenceOperation insertPersistenceOperation;

	private DeletePersistenceOperation deletePersistenceOperation;

	public UpdatePersistenceOperation()
	{
		super();
	}

	public UpdatePersistenceOperation(InsertPersistenceOperation insertPersistenceOperation,
			DeletePersistenceOperation deletePersistenceOperation, ConversionService conversionService,
			NameExpressionResolver variableExpressionResolver, NameExpressionResolver sqlExpressionResolver)
	{
		super(conversionService, variableExpressionResolver, sqlExpressionResolver);
		this.insertPersistenceOperation = insertPersistenceOperation;
		this.deletePersistenceOperation = deletePersistenceOperation;
	}

	public InsertPersistenceOperation getInsertPersistenceOperation()
	{
		return insertPersistenceOperation;
	}

	public void setInsertPersistenceOperation(InsertPersistenceOperation insertPersistenceOperation)
	{
		this.insertPersistenceOperation = insertPersistenceOperation;
	}

	public DeletePersistenceOperation getDeletePersistenceOperation()
	{
		return deletePersistenceOperation;
	}

	public void setDeletePersistenceOperation(DeletePersistenceOperation deletePersistenceOperation)
	{
		this.deletePersistenceOperation = deletePersistenceOperation;
	}

	/**
	 * 更新。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param updateProperties
	 *            要更新的属性，为{@code null}表示全部更新
	 * @param originalObj
	 *            原始数据
	 * @param updateObj
	 *            待更新的数据
	 * @return
	 */
	public int update(Connection cn, Dialect dialect, String table, Model model, Property[] updateProperties,
			Object originalObj, Object updateObj)
	{
		SqlBuilder originalCondition = buildRecordCondition(cn, dialect, model, originalObj, null);

		return update(cn, dialect, table, model, updateProperties, originalCondition, originalObj, updateObj, null,
				null, null, new ExpressionEvaluationContext());
	}

	/**
	 * 更新。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param updateProperties
	 *            要更新的属性，为{@code null}表示全部更新
	 * @param originalObj
	 *            原始数据
	 * @param updateObj
	 *            待更新的数据
	 * @param expressionEvaluationContext
	 * @return
	 */
	public int update(Connection cn, Dialect dialect, String table, Model model, Property[] updateProperties,
			Object originalObj, Object updateObj, ExpressionEvaluationContext expressionEvaluationContext)
	{
		SqlBuilder originalCondition = buildRecordCondition(cn, dialect, model, originalObj, null);

		return update(cn, dialect, table, model, updateProperties, originalCondition, originalObj, updateObj, null,
				null, null, expressionEvaluationContext);
	}

	/**
	 * 更新属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 * @param property
	 * @param mapper
	 * @param updatePropertyProperties
	 *            要更新属性模型的属性数组，如果为{@code null}，则全部更新。
	 * @param originalPropertyValue
	 *            原始属性值
	 * @param updatePropertyValue
	 *            待更新的属性值，允许为{@code null}
	 * @return
	 */
	public int updatePropertyTableData(Connection cn, Dialect dialect, String table, Model model, SqlBuilder condition,
			Property property, Mapper mapper, Property[] updatePropertyProperties, Object originalPropertyValue,
			Object updatePropertyValue)
	{
		return updatePropertyTableData(cn, dialect, table, model, condition, property, mapper, updatePropertyProperties,
				originalPropertyValue, updatePropertyValue, null, true, new ExpressionEvaluationContext());
	}

	/**
	 * 更新属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 * @param property
	 * @param mapper
	 * @param updatePropertyProperties
	 *            要更新属性模型的属性数组，如果为{@code null}，则全部更新。
	 * @param originalPropertyValue
	 *            原始属性值
	 * @param updatePropertyValue
	 *            待更新的属性值，允许为{@code null}
	 * @param expressionEvaluationContext
	 * @return
	 */
	public int updatePropertyTableData(Connection cn, Dialect dialect, String table, Model model, SqlBuilder condition,
			Property property, Mapper mapper, Property[] updatePropertyProperties, Object originalPropertyValue,
			Object updatePropertyValue, ExpressionEvaluationContext expressionEvaluationContext)
	{
		return updatePropertyTableData(cn, dialect, table, model, condition, property, mapper, updatePropertyProperties,
				originalPropertyValue, updatePropertyValue, null, true, expressionEvaluationContext);
	}

	/**
	 * 更新。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param updateProperties
	 *            要更新的属性，为{@code null}表示全部更新
	 * @param originalCondition
	 *            用于确定原始数据记录的模型表条件
	 * @param originalObj
	 *            原始数据
	 * @param updateObj
	 *            待更新的数据
	 * @param extraColumnNames
	 *            附加列名称数组，允许为{@code null}
	 * @param extraColumnValues
	 *            附加列值，允许为{@code null}
	 * @param ignorePropertyName
	 *            忽略的属性名称，用于处理双向关联时，允许为{@code null}
	 * @param expressionEvaluationContext
	 * @return
	 */
	protected int update(Connection cn, Dialect dialect, String table, Model model, Property[] updateProperties,
			SqlBuilder originalCondition, Object originalObj, Object updateObj, String[] extraColumnNames,
			Object[] extraColumnValues, String ignorePropertyName,
			ExpressionEvaluationContext expressionEvaluationContext)
	{
		int count = 0;

		if (updateProperties == null)
			updateProperties = model.getProperties();

		Mapper[] mappers = getMappers(model, updateProperties);

		Object[] originalPropertyValues = MU.getPropertyValues(model, originalObj, updateProperties);
		Object[] updatePropertyValues = MU.getPropertyValues(model, updateObj, updateProperties);

		// 先求得SQL表达式属性值并赋予obj，因为某些驱动程序并不支持任意设置Statement.getGeneratedKeys()
		for (int i = 0; i < updateProperties.length; i++)
		{
			Property property = updateProperties[i];

			if (isUpdateIgnoreProperty(model, property, ignorePropertyName, true))
				continue;

			Object propertyValue = updatePropertyValues[i];

			Object evalPropertyValue = evaluatePropertyValueIfExpression(cn, model, property, propertyValue,
					expressionEvaluationContext);

			if (evalPropertyValue != propertyValue)
			{
				propertyValue = evalPropertyValue;
				updatePropertyValues[i] = evalPropertyValue;
				property.set(updateObj, evalPropertyValue);
			}
		}

		boolean propertyUpdated = false;

		List<UpdateInfoForAutoKeyUpdateRule> updateInfoForAutoKeyUpdateRules = new ArrayList<UpdateInfoForAutoKeyUpdateRule>();

		// 先处理删除属性值，它不会受外键约束的影响；
		// 先处理KeyRule.isManually()为true的更新属性值操作，它不会受外键约束的影响，并且如果先更新模型表，里更的外键值可能会被更新，那么关联属性值更新则会失效；
		for (int i = 0; i < updateProperties.length; i++)
		{
			Property property = updateProperties[i];

			if (isUpdateIgnoreProperty(model, property, ignorePropertyName, false))
				continue;

			Mapper mapper = mappers[i];
			Object originalPropertyValue = originalPropertyValues[i];
			Object updatePropertyValue = updatePropertyValues[i];

			if (updatePropertyValue == null)
			{
				if (originalPropertyValue != null)
				{
					// 更新操作时，不处理清除集合属性值
					if (!MU.isMultipleProperty(property))
					{
						int myCount = deletePersistenceOperation.deletePropertyTableData(cn, dialect, table, model,
								originalCondition, property, mapper, null, false);

						if (propertyUpdated == false && myCount > 0)
							propertyUpdated = true;
					}
				}
				else
					;
			}
			else if (MU.isMultipleProperty(property))
			{
				Object[] originalPropertyValueElements = toArray(originalPropertyValue);
				Object[] updatePropertyValueElements = toArray(updatePropertyValue);

				int opveLen = (originalPropertyValueElements == null ? 0 : originalPropertyValueElements.length);
				int upevLen = (updatePropertyValueElements == null ? 0 : updatePropertyValueElements.length);

				for (int j = 0; j < Math.max(opveLen, upevLen); j++)
				{
					Object originalPropertyValueElement = (j >= opveLen ? null : originalPropertyValueElements[j]);
					Object updatePropertyValueElement = (j >= upevLen ? null : updatePropertyValueElements[j]);

					int myCount = 0;

					if (originalPropertyValueElement == null && updatePropertyValueElement == null)
						continue;
					// 添加
					else if (originalPropertyValueElement == null)
					{
						KeyRule propertyKeyUpdateRule = mapper.getPropertyKeyUpdateRule();

						if (propertyKeyUpdateRule == null || propertyKeyUpdateRule.isManually())
						{
							myCount = insertPersistenceOperation.insertPropertyTableData(cn, dialect, table, model,
									updateObj, property, mapper, new Object[] { updatePropertyValueElement }, null,
									expressionEvaluationContext);
						}
						else
						{
							UpdateInfoForAutoKeyUpdateRule updateInfo = new UpdateInfoForAutoKeyUpdateRule(property, i,
									mapper, originalPropertyValueElement, updatePropertyValueElement);
							updateInfoForAutoKeyUpdateRules.add(updateInfo);
						}
					}
					// 删除
					else if (updatePropertyValueElement == null)
					{
						myCount = deletePersistenceOperation.deletePropertyTableData(cn, dialect, table, model,
								originalCondition, property, mapper, null, false);
					}
					// 更新
					else
					{
						KeyRule propertyKeyUpdateRule = mapper.getPropertyKeyUpdateRule();

						if (propertyKeyUpdateRule == null || propertyKeyUpdateRule.isManually())
						{
							myCount = updatePropertyTableData(cn, dialect, table, model, originalCondition, property,
									mapper, null, originalPropertyValueElement, updatePropertyValueElement, updateObj,
									false, expressionEvaluationContext);
						}
						else
						{
							UpdateInfoForAutoKeyUpdateRule updateInfo = new UpdateInfoForAutoKeyUpdateRule(property, i,
									mapper, originalPropertyValueElement, updatePropertyValueElement);
							updateInfoForAutoKeyUpdateRules.add(updateInfo);
						}
					}

					if (propertyUpdated == false && myCount > 0)
						propertyUpdated = true;
				}
			}
			else
			{
				if (PMU.isShared(model, property))
					continue;

				KeyRule propertyKeyUpdateRule = mapper.getPropertyKeyUpdateRule();

				if (propertyKeyUpdateRule == null || propertyKeyUpdateRule.isManually())
				{
					int myCount = updatePropertyTableData(cn, dialect, table, model, originalCondition, property,
							mapper, null, originalPropertyValue, updatePropertyValue, updateObj, false,
							expressionEvaluationContext);

					if (myCount == 0)
						myCount = insertPersistenceOperation.insertPropertyTableData(cn, dialect, table, model,
								updateObj, property, mapper, new Object[] { updatePropertyValue }, null,
								expressionEvaluationContext);

					if (propertyUpdated == false && myCount > 0)
						propertyUpdated = true;
				}
				else
				{
					UpdateInfoForAutoKeyUpdateRule updateInfo = new UpdateInfoForAutoKeyUpdateRule(property, i, mapper,
							originalPropertyValue, updatePropertyValue);
					updateInfoForAutoKeyUpdateRules.add(updateInfo);
				}
			}
		}

		// 更新模型表数据
		count = updateModelTableData(cn, dialect, table, model, originalCondition, updateProperties, updateObj,
				originalPropertyValues, extraColumnNames, extraColumnValues, ignorePropertyName);

		// 如果count=0，说明记录不存在，不应该再更新子记录，会报外键不存在的错
		if (count != 0)
		{
			// 处理KeyRule.isManually()为false的更新属性值操作
			if (!updateInfoForAutoKeyUpdateRules.isEmpty())
			{
				// 在执行updateModelTableData后，关联外键会被级联更新，所以要使用updateObj构造条件
				SqlBuilder updateCondition = buildRecordCondition(cn, dialect, model, updateObj, null);

				for (UpdateInfoForAutoKeyUpdateRule updateInfo : updateInfoForAutoKeyUpdateRules)
				{
					Object updatePropertyValue = updateInfo.getUpdatePropertyValue();

					int myCount = updatePropertyTableData(cn, dialect, table, model, updateCondition,
							updateInfo.getProperty(), updateInfo.getMapper(), null,
							updateInfo.getOriginalPropertyValue(), updatePropertyValue, null, false,
							expressionEvaluationContext);

					if (myCount == 0)
						myCount = insertPersistenceOperation.insertPropertyTableData(cn, dialect, table, model,
								updateObj, updateInfo.getProperty(), updateInfo.getMapper(),
								new Object[] { updatePropertyValue }, null, expressionEvaluationContext);

					if (propertyUpdated == false && myCount > 0)
						propertyUpdated = true;
				}
			}
		}

		// 仅修改了复合属性值时，也应该返回1
		if (count < 0 && propertyUpdated)
			count = 1;

		return count;
	}

	/**
	 * 更新模型表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            模型表查询条件，允许为{@code null}
	 * @param updateProperties
	 * @param updateObj
	 * @param originalPropertyValues
	 *            允许为{@code null}或者元素为{@code null}
	 * @param extraColumnNames
	 *            附加列名称数组，允许为{@code null}
	 * @param extraColumnValues
	 *            附加列值，允许为{@code null}
	 * @param ignorePropertyName
	 *            忽略的属性名称，用于处理双向关联时，允许为{@code null}
	 * @return
	 */
	protected int updateModelTableData(Connection cn, Dialect dialect, String table, Model model, SqlBuilder condition,
			Property[] updateProperties, Object updateObj, Object[] originalPropertyValues, String[] extraColumnNames,
			Object[] extraColumnValues, String ignorePropertyName)
	{
		SqlBuilder sql = SqlBuilder.valueOf().sql("UPDATE ").sql(toQuoteName(dialect, table)).sql(" SET ").delimit(",");
		int sqlLength = sql.sqlLength();

		ModelOrderGenerator modelOrderGenerator = new ModelOrderGenerator()
		{
			@Override
			public long generate(Model model, Property property, ModelTableMapper mapper, Object propertyValue,
					Object[] propertyKeyColumnValues)
			{
				// TODO 实现排序值生成逻辑
				return 0;
			}
		};

		for (int i = 0; i < updateProperties.length; i++)
		{
			Property property = updateProperties[i];

			if (isUpdateIgnoreProperty(model, property, ignorePropertyName, true))
				continue;

			Object originalPropertyValue = (originalPropertyValues == null ? null : originalPropertyValues[i]);
			Object updatePropertyValue = MU.getPropertyValue(model, updateObj, property);

			// 如果属性值未修改，则不更新
			if (isPropertyValueUnchangedForUpdateModelTableData(model, property, originalPropertyValue,
					updatePropertyValue))
				continue;

			Mapper mapper = getMapper(model, property);

			List<Object> myOriginalColumnValues = new ArrayList<Object>();
			List<Object> myUpdateColumnValues = new ArrayList<Object>();

			addColumnValues(cn, model, property, mapper, originalPropertyValue, true, modelOrderGenerator, true,
					myOriginalColumnValues);
			addColumnValues(cn, model, property, mapper, updatePropertyValue, true, modelOrderGenerator, true,
					myUpdateColumnValues);

			if (myOriginalColumnValues.equals(myUpdateColumnValues))
				continue;

			List<String> myColumnNames = new ArrayList<String>();
			addColumnNames(model, property, mapper, true, true, true, myColumnNames);

			sql.sqldSuffix(toQuoteNames(dialect, toStringArray(myColumnNames)), "=?")
					.arg(toObjectArray(myUpdateColumnValues));
		}

		if (extraColumnNames != null)
			sql.sqldSuffix(toQuoteNames(dialect, extraColumnNames), "=?").arg(extraColumnValues);

		int nowSqlLength = sql.sqlLength();

		if (condition != null)
			sql.sql(" WHERE ").sql(condition);

		if (nowSqlLength == sqlLength)
			return PersistenceManager.PERSISTENCE_UNCHANGED;
		else
			return executeUpdate(cn, sql);
	}

	/**
	 * 更新属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 * @param property
	 * @param mapper
	 * @param updatePropertyProperties
	 *            要更新属性模型的属性数组，如果为{@code null}，则全部更新。
	 * @param originalPropertyValue
	 *            原始属性值
	 * @param updatePropertyValue
	 *            待更新的属性值，允许为{@code null}
	 * @param keyUpdateObj
	 *            需要处理外键更新的对象，允许为{@code null}
	 * @param updateModelTable
	 *            是否更新模型表数据
	 * @param expressionEvaluationContext
	 * @return
	 */
	protected int updatePropertyTableData(Connection cn, Dialect dialect, String table, Model model,
			SqlBuilder condition, Property property, Mapper mapper, Property[] updatePropertyProperties,
			Object originalPropertyValue, Object updatePropertyValue, Object keyUpdateObj, boolean updateModelTable,
			ExpressionEvaluationContext expressionEvaluationContext)
	{
		int count = 0;

		if (MapperUtil.isModelTableMapper(mapper))
		{
			ModelTableMapper mtm = MapperUtil.castModelTableMapper(mapper);

			count = updatePropertyTableDataForModelTableMapper(cn, dialect, table, model, condition, property, mtm,
					updatePropertyProperties, originalPropertyValue, updatePropertyValue, updateModelTable,
					expressionEvaluationContext);
		}
		else if (MapperUtil.isPropertyTableMapper(mapper))
		{
			PropertyTableMapper ptm = MapperUtil.castPropertyTableMapper(mapper);

			count = updatePropertyTableDataForPropertyTableMapper(cn, dialect, table, model, condition, property, ptm,
					updatePropertyProperties, originalPropertyValue, updatePropertyValue, keyUpdateObj,
					expressionEvaluationContext);
		}
		else if (MapperUtil.isJoinTableMapper(mapper))
		{
			JoinTableMapper jtm = MapperUtil.castJoinTableMapper(mapper);

			count = updatePropertyTableDataForJoinTableMapper(cn, dialect, table, model, condition, property, jtm,
					updatePropertyProperties, originalPropertyValue, updatePropertyValue, keyUpdateObj,
					expressionEvaluationContext);
		}
		else
			throw new UnsupportedOperationException();

		return count;
	}

	/**
	 * 更新属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            模型表查询条件，允许为{@code null}
	 * @param property
	 * @param mapper
	 * @param updatePropertyProperties
	 *            要更新属性模型的属性数组，如果为{@code null}，则全部更新。
	 * @param originalPropertyValue
	 *            原始属性值，基本属性值时允许为{@code null}
	 * @param updatePropertyValue
	 *            待更新的属性值，允许为{@code null}
	 * @param updateModelTable
	 *            是否更新模型表数据
	 * @param expressionEvaluationContext
	 * @return
	 */
	protected int updatePropertyTableDataForModelTableMapper(Connection cn, Dialect dialect, String table, Model model,
			SqlBuilder condition, Property property, ModelTableMapper mapper, Property[] updatePropertyProperties,
			Object originalPropertyValue, Object updatePropertyValue, boolean updateModelTable,
			ExpressionEvaluationContext expressionEvaluationContext)
	{
		int count = 0;

		if (mapper.isPrimitivePropertyMapper())
		{
			if (updateModelTable)
			{
				Property[] properties = new Property[] { property };
				Object[] originalPropertyValues = new Object[] { originalPropertyValue };

				Object updateObj = MU.instance(model);
				property.set(updateObj, updatePropertyValue);

				count = updateModelTableData(cn, dialect, table, model, condition, properties, updateObj,
						originalPropertyValues, null, null, null);
			}
			else
				count = PersistenceManager.PERSISTENCE_IGNORED;
		}
		else
		{
			Model propertyModel = MU.getModel(property);

			if (PMU.isPrivate(model, property))
			{
				count = update(cn, dialect, table, propertyModel, null,
						buildRecordCondition(cn, dialect, propertyModel, originalPropertyValue, null),
						originalPropertyValue, updatePropertyValue, null, null, getMappedByWith(mapper),
						expressionEvaluationContext);

				if (updateModelTable)
				{
					Property[] properties = new Property[] { property };
					Object[] originalPropertyValues = new Object[] { originalPropertyValue };

					Object updateObj = model.newInstance();
					property.set(updateObj, updatePropertyValue);

					count = updateModelTableData(cn, dialect, table, model, condition, properties, updateObj,
							originalPropertyValues, null, null, null);
				}
			}
			else
			{
				if (updateModelTable)
				{
					Property[] properties = new Property[] { property };
					Object[] originalPropertyValues = new Object[] { originalPropertyValue };

					Object updateObj = model.newInstance();
					property.set(updateObj, updatePropertyValue);

					count = updateModelTableData(cn, dialect, table, model, condition, properties, updateObj,
							originalPropertyValues, null, null, null);
				}
				else
					return PersistenceManager.PERSISTENCE_IGNORED;
			}
		}

		return count;
	}

	/**
	 * 更新属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 * @param property
	 * @param mapper
	 * @param updatePropertyProperties
	 *            要更新属性模型的属性数组，如果为{@code null}，则全部更新。
	 * @param originalPropertyValue
	 *            原始属性值
	 * @param updatePropertyValue
	 *            待更新的属性值，允许为{@code null}
	 * @param keyUpdateObj
	 *            需要处理外键更新的对象，允许为{@code null}
	 * @param expressionEvaluationContext
	 * @return
	 */
	protected int updatePropertyTableDataForPropertyTableMapper(Connection cn, Dialect dialect, String table,
			Model model, SqlBuilder condition, Property property, PropertyTableMapper mapper,
			Property[] updatePropertyProperties, Object originalPropertyValue, Object updatePropertyValue,
			Object keyUpdateObj, ExpressionEvaluationContext expressionEvaluationContext)
	{
		int count = 0;

		Model propertyModel = MU.getModel(property);

		String[] mkeyColumnNames = null;
		Object[] mkeyColumnValues = null;

		if (keyUpdateObj != null)
		{
			mkeyColumnNames = mapper.getModelKeyColumnNames();
			mkeyColumnValues = getModelKeyColumnValues(cn, mapper, model, keyUpdateObj);
		}

		// 如果是单元属性，则不必需要recordCondtion
		SqlBuilder recordCondition = (MU.isMultipleProperty(property)
				? buildRecordCondition(cn, dialect, propertyModel, originalPropertyValue, getMappedByWith(mapper))
				: null);

		SqlBuilder ptableCondition = buildPropertyTableConditionForPropertyTableMapper(dialect, table, model, condition,
				property, mapper, recordCondition);

		if (mapper.isPrimitivePropertyMapper())
		{
			String ptable = mapper.getPrimitiveTableName();

			boolean changed = !isPropertyValueUnchangedForUpdateModelTableData(model, property, originalPropertyValue,
					updatePropertyValue);

			if (!changed && mkeyColumnNames == null)
				count = PersistenceManager.PERSISTENCE_UNCHANGED;
			else
			{
				SqlBuilder sql = SqlBuilder.valueOf();

				sql.sql("UPDATE ").sql(toQuoteName(dialect, ptable)).sql(" SET ").delimit(",");

				if (changed)
				{
					String columnName = toQuoteName(dialect, mapper.getPrimitiveColumnName());

					Object evalUpdatePropertyValue = evaluatePropertyValueIfExpression(cn, model, property,
							updatePropertyValue, expressionEvaluationContext);

					if (evalUpdatePropertyValue != updatePropertyValue)
					{
						updatePropertyValue = evalUpdatePropertyValue;
						Object columnValue = getColumnValue(cn, model, property, evalUpdatePropertyValue);

						sql.sqldSuffix(columnName, "=" + columnValue);
					}
					else
					{
						Object columnValue = getColumnValue(cn, model, property, updatePropertyValue);
						sql.sqldSuffix(columnName, "=?").arg(columnValue);
					}
				}

				if (mkeyColumnNames != null)
					sql.sqldSuffix(mkeyColumnNames, "=?").arg(mkeyColumnValues);

				sql.sql(" WHERE ").sql(ptableCondition);

				count = executeUpdate(cn, sql);
			}
		}
		else
		{
			count = update(cn, dialect, getTableName(propertyModel), propertyModel, updatePropertyProperties,
					ptableCondition, originalPropertyValue, updatePropertyValue, mkeyColumnNames, mkeyColumnValues,
					getMappedByWith(mapper), expressionEvaluationContext);
		}

		return count;
	}

	/**
	 * 更新属性表数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 * @param property
	 * @param mapper
	 * @param updatePropertyProperties
	 *            要更新属性模型的属性数组，如果为{@code null}，则全部更新。
	 * @param originalPropertyValue
	 *            原始属性值
	 * @param updatePropertyValue
	 *            待更新的属性值，允许为{@code null}
	 * @param keyUpdateObj
	 *            需要处理外键更新的对象，允许为{@code null}
	 * @param expressionEvaluationContext
	 * @return
	 */
	protected int updatePropertyTableDataForJoinTableMapper(Connection cn, Dialect dialect, String table, Model model,
			SqlBuilder condition, Property property, JoinTableMapper mapper, Property[] updatePropertyProperties,
			Object originalPropertyValue, Object updatePropertyValue, Object keyUpdateObj,
			ExpressionEvaluationContext expressionEvaluationContext)
	{
		int count = 0;

		Model propertyModel = MU.getModel(property);
		String mappedByWith = getMappedByWith(mapper);

		// 如果是单元属性，则不必需要recordCondtion
		SqlBuilder recordCondition = (MU.isMultipleProperty(property)
				? buildRecordCondition(cn, dialect, propertyModel, originalPropertyValue, mappedByWith)
				: null);

		SqlBuilder ptableCondition = buildPropertyTableConditionForJoinTableMapper(dialect, table, propertyModel,
				condition, property, mapper, recordCondition);

		if (PMU.isPrivate(model, property))
		{
			count = update(cn, dialect, getTableName(propertyModel), propertyModel, updatePropertyProperties,
					ptableCondition, originalPropertyValue, updatePropertyValue, null, null, mappedByWith,
					expressionEvaluationContext);

			if (keyUpdateObj != null)
			{
				count = updatePropertyTableDataRelationForJoinTableMapper(cn, dialect, table, propertyModel, condition,
						property, mapper, originalPropertyValue, updatePropertyValue, keyUpdateObj);
			}
		}
		else
		{
			if (keyUpdateObj != null)
			{
				count = updatePropertyTableDataRelationForJoinTableMapper(cn, dialect, table, propertyModel, condition,
						property, mapper, originalPropertyValue, updatePropertyValue, keyUpdateObj);
			}
			else
				count = PersistenceManager.PERSISTENCE_IGNORED;
		}

		return count;
	}

	/**
	 * 更新属性表数据的关联关系。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 * @param property
	 * @param mapper
	 * @param originalPropertyValue
	 * @param updatePropertyValue
	 * @param keyUpdateObj
	 * @return
	 */
	protected int updatePropertyTableDataRelationForJoinTableMapper(Connection cn, Dialect dialect, String table,
			Model model, SqlBuilder condition, Property property, JoinTableMapper mapper, Object originalPropertyValue,
			Object updatePropertyValue, Object keyUpdateObj)
	{
		Model propertyModel = MU.getModel(property);

		SqlBuilder propertyTableCondition = buildRecordCondition(cn, dialect, propertyModel, originalPropertyValue,
				null);
		SqlBuilder joinTableCondtion = buildJoinTableCondition(dialect, table, model, condition, property, mapper,
				propertyTableCondition);

		String joinTableName = toQuoteName(dialect, mapper.getJoinTableName());
		String[] mkeyColumnNames = toQuoteNames(dialect, mapper.getModelKeyColumnNames());
		String[] pkeyColumnNames = toQuoteNames(dialect, mapper.getPropertyKeyColumnNames());

		Object[] updateModelKeyColumnValues = getModelKeyColumnValues(cn, mapper, model, keyUpdateObj);
		Object[] updatePropertyKeyColumnValues = getPropertyKeyColumnValues(cn, mapper, propertyModel,
				updatePropertyValue);

		SqlBuilder sql = SqlBuilder.valueOf();

		sql.sql("UPDATE ").sql(joinTableName).sql(" SET ").delimit(",").sqldSuffix(mkeyColumnNames, "=?")
				.arg(updateModelKeyColumnValues).sqldSuffix(pkeyColumnNames, "=?").arg(updatePropertyKeyColumnValues)
				.sql(" WHERE ").sql(joinTableCondtion);

		return executeUpdate(cn, sql);
	}

	/**
	 * 是否是更新忽略属性。
	 * 
	 * @param model
	 * @param property
	 * @param ignorePropertyName
	 * @param ignoreMultipleProperty
	 * @return
	 */
	protected boolean isUpdateIgnoreProperty(Model model, Property property, String ignorePropertyName,
			boolean ignoreMultipleProperty)
	{
		if (MU.isMultipleProperty(property) && ignoreMultipleProperty)
			return true;

		if (ignorePropertyName != null && ignorePropertyName.equals(property.getName()))
			return true;

		if (property.hasFeature(NotReadable.class) || property.hasFeature(NotEditable.class))
			return true;

		return false;
	}

	/**
	 * 判断属性值是否未作修改。
	 * 
	 * @param model
	 * @param property
	 * @param originalPropertyValue
	 * @param updatePropertyValue
	 * @return
	 */
	protected boolean isPropertyValueUnchangedForUpdateModelTableData(Model model, Property property,
			Object originalPropertyValue, Object updatePropertyValue)
	{
		if (MU.isMultipleProperty(property))
			throw new UnsupportedOperationException();

		if (originalPropertyValue == null)
		{
			return (updatePropertyValue == null);
		}
		else if (updatePropertyValue == null)
		{
			return (originalPropertyValue == null);
		}
		else
		{
			// 仅比较基本属性值，复合属性值如果存在循环引用，equals会出现死循环
			if (MU.isPrimitiveProperty(property))
			{
				return (originalPropertyValue.equals(updatePropertyValue));
			}
			else
				return false;
		}
	}

	protected static class UpdateInfoForAutoKeyUpdateRule
	{
		private Property property;

		private int propertyIndex;

		private Mapper mapper;

		private Object originalPropertyValue;

		private Object updatePropertyValue;

		public UpdateInfoForAutoKeyUpdateRule()
		{
			super();
		}

		public UpdateInfoForAutoKeyUpdateRule(Property property, int propertyIndex, Mapper mapper,
				Object originalPropertyValue, Object updatePropertyValue)
		{
			super();
			this.property = property;
			this.propertyIndex = propertyIndex;
			this.mapper = mapper;
			this.originalPropertyValue = originalPropertyValue;
			this.updatePropertyValue = updatePropertyValue;
		}

		public Property getProperty()
		{
			return property;
		}

		public void setProperty(Property property)
		{
			this.property = property;
		}

		public int getPropertyIndex()
		{
			return propertyIndex;
		}

		public void setPropertyIndex(int propertyIndex)
		{
			this.propertyIndex = propertyIndex;
		}

		public Mapper getMapper()
		{
			return mapper;
		}

		public void setMapper(Mapper mapper)
		{
			this.mapper = mapper;
		}

		public Object getOriginalPropertyValue()
		{
			return originalPropertyValue;
		}

		public void setOriginalPropertyValue(Object originalPropertyValue)
		{
			this.originalPropertyValue = originalPropertyValue;
		}

		public Object getUpdatePropertyValue()
		{
			return updatePropertyValue;
		}

		public void setUpdatePropertyValue(Object updatePropertyValue)
		{
			this.updatePropertyValue = updatePropertyValue;
		}
	}
}
