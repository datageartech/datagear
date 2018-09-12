/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.features.NotReadable;
import org.datagear.model.support.MU;
import org.datagear.model.support.PropertyModel;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.SqlBuilder;
import org.datagear.persistence.features.ColumnConverter;
import org.datagear.persistence.features.JdbcType;
import org.datagear.persistence.features.TableName;
import org.datagear.persistence.mapper.JoinTableMapper;
import org.datagear.persistence.mapper.Mapper;
import org.datagear.persistence.mapper.ModelTableMapper;
import org.datagear.persistence.mapper.PropertyModelMapper;
import org.datagear.persistence.mapper.PropertyTableMapper;
import org.datagear.persistence.mapper.RelationMapper;

/**
 * 抽象{@linkplain Model}数据访问对象。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractModelDataAccessObject extends AbstractDataAccessObject
{
	/** 持久化操作被忽略标识。对于共享属性值，很多情况下不会被处理，此标识作为返回值 */
	public static final int PERSISTENCE_IGNORED = -1;

	public AbstractModelDataAccessObject()
	{
		super();
	}

	/**
	 * 获取ID列名称。
	 * 
	 * @param model
	 * @return
	 */
	protected String[] getIdColumnNames(Model model)
	{
		return getKeyColumnNames(model, model.getIdProperties());
	}

	/**
	 * 获取可作为外键的属性列名称。
	 * 
	 * @param model
	 * @param properties
	 * @return
	 */
	protected String[] getKeyColumnNames(Model model, Property[] properties)
	{
		// KEY属性不允许有模型端和属性段具体模型名，所以addModelConcreteColumn和addPropertyConcreteColumn为false，
		// 参考RelationMapperResolver.checkKeyProperty(Model, Property...)
		return getColumnNames(model, properties, false, false, false);
	}

	/**
	 * 获取属性列名称数组。
	 * 
	 * @param model
	 * @param properties
	 * @param addModelConcreteColumn
	 * @param addModelOrderColumn
	 * @param addPropertyConcreteColumn
	 * @return
	 */
	protected String[] getColumnNames(Model model, Property[] properties, boolean addModelConcreteColumn,
			boolean addModelOrderColumn, boolean addPropertyConcreteColumn)
	{
		List<String> columnNames = new ArrayList<String>();

		for (Property property : properties)
		{
			RelationMapper relationMapper = getRelationMapper(model, property);
			PropertyModelMapper<?>[] propertyModelMappers = PropertyModelMapper.valueOf(property, relationMapper);

			addColumnNames(model, property, propertyModelMappers, addModelConcreteColumn, addModelOrderColumn,
					addPropertyConcreteColumn, columnNames);
		}

		return columnNames.toArray(new String[columnNames.size()]);
	}

	/**
	 * 获取属性列名称数组。
	 * 
	 * @param model
	 * @param property
	 * @param containsModelConcreteColumn
	 * @param containsModelOrderColumn
	 * @param containsPropertyConcreteColumn
	 * @return
	 */
	protected String[] getColumnNames(Model model, Property property, boolean containsModelConcreteColumn,
			boolean containsModelOrderColumn, boolean containsPropertyConcreteColumn)
	{
		List<String> columnNames = new ArrayList<String>();

		RelationMapper relationMapper = getRelationMapper(model, property);
		PropertyModelMapper<?>[] propertyModelMappers = PropertyModelMapper.valueOf(property, relationMapper);

		addColumnNames(model, property, propertyModelMappers, containsModelConcreteColumn, containsModelOrderColumn,
				containsPropertyConcreteColumn, columnNames);

		return columnNames.toArray(new String[columnNames.size()]);
	}

	/**
	 * 添加属性列名称至指定列表。
	 * <p>
	 * 如果属性在模型表内没有列，则不会添加。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyModelMappers
	 * @param addModelConcreteColumn
	 * @param addModelOrderColumn
	 * @param addPropertyConcreteColumn
	 * @param columnNames
	 */
	protected void addColumnNames(Model model, Property property, PropertyModelMapper<?>[] propertyModelMappers,
			boolean addModelConcreteColumn, boolean addModelOrderColumn, boolean addPropertyConcreteColumn,
			List<String> columnNames)
	{
		if (MU.isMultipleProperty(property))
			return;

		for (int i = 0; i < propertyModelMappers.length; i++)
		{
			PropertyModelMapper<?> pmm = propertyModelMappers[i];

			if (!pmm.isModelTableMapperInfo())
				continue;

			addColumnNames(model, property, pmm.castModelTableMapperInfo(), addModelConcreteColumn, addModelOrderColumn,
					addPropertyConcreteColumn, columnNames);
		}
	}

	/**
	 * 添加列名称至指定列表。
	 * 
	 * @param model
	 * @param property
	 * @param propertyModelMapper
	 * @param addModelConcreteColumn
	 * @param addModelOrderColumn
	 * @param addPropertyConcreteColumn
	 * @param columnNames
	 */
	protected void addColumnNames(Model model, Property property,
			PropertyModelMapper<ModelTableMapper> propertyModelMapper, boolean addModelConcreteColumn,
			boolean addModelOrderColumn, boolean addPropertyConcreteColumn, List<String> columnNames)
	{
		ModelTableMapper mapper = propertyModelMapper.getMapper();

		if (mapper.isPrimitivePropertyMapper())
		{
			columnNames.add(mapper.getPrimitiveColumnName());
		}
		else
		{
			addArrayToList(columnNames, mapper.getPropertyKeyColumnNames());

			if (mapper.hasModelConcreteColumn() && addModelConcreteColumn)
				columnNames.add(mapper.getModelConcreteColumnName());

			if (mapper.hasModelOrderColumn() && addModelOrderColumn)
				columnNames.add(mapper.getModelOrderColumnName());

			if (mapper.hasPropertyConcreteColumn() && addPropertyConcreteColumn)
				columnNames.add(mapper.getPropertyConcreteColumnName());
		}
	}

	/**
	 * 获取ID属性的列值数组。
	 * <p>
	 * 此方法返回值数组的元素顺序与{@linkplain #getIdColumnNames(Model)}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param id
	 * @return
	 */
	protected Object[][] getIdColumnValuesForId(Connection cn, Model model, Object[][] ids)
	{
		Object[][] idColumnValues = new Object[ids.length][];

		for (int i = 0; i < ids.length; i++)
			idColumnValues[i] = getIdColumnValuesForId(cn, model, ids[i]);

		return idColumnValues;
	}

	/**
	 * 获取ID属性的列值数组。
	 * <p>
	 * 此方法返回值数组的元素顺序与{@linkplain #getIdColumnNames(Model)}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param id
	 * @return
	 */
	protected Object[] getIdColumnValuesForId(Connection cn, Model model, Object[] id)
	{
		Property[] idProperties = model.getIdProperties();

		return getKeyColumnValues(cn, model, idProperties, id);
	}

	/**
	 * 获取ID属性的列值数组。
	 * <p>
	 * 此方法返回值数组的元素顺序与{@linkplain #getIdColumnNames(Model)}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 *            允许为{@code null}
	 * @return
	 */
	protected Object[][] getIdColumnValuesForObj(Connection cn, Model model, Object[] objs)
	{
		Object[][] idColumnValues = new Object[objs.length][];

		for (int i = 0; i < objs.length; i++)
			idColumnValues[i] = getIdColumnValuesForObj(cn, model, objs[i]);

		return idColumnValues;
	}

	/**
	 * 获取ID属性的列值数组。
	 * <p>
	 * 此方法返回值数组的元素顺序与{@linkplain #getIdColumnNames(Model)}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 *            允许为{@code null}
	 * @return
	 */
	protected Object[] getIdColumnValuesForObj(Connection cn, Model model, Object obj)
	{
		Property[] idProperties = model.getIdProperties();
		Object[] propertyValues = (obj == null ? null : MU.getPropertyValues(model, obj, idProperties));

		return getKeyColumnValues(cn, model, idProperties, propertyValues);
	}

	/**
	 * 获取可作为KEY属性的列值数组。
	 * <p>
	 * 此方法返回值数组的元素顺序与{@linkplain #getKeyColumnNames(Model, Property[])}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 *            允许为{@code null}
	 * @param keyProperties
	 * @return
	 */
	protected Object[] getKeyColumnValues(Connection cn, Model model, Object obj, Property[] keyProperties)
	{
		Object[] propertyValues = (obj == null ? null : MU.getPropertyValues(model, obj, keyProperties));

		return getKeyColumnValues(cn, model, keyProperties, propertyValues);
	}

	/**
	 * 获取可作为KEY属性的列值数组。
	 * <p>
	 * 此方法返回值数组的元素顺序与{@linkplain #getKeyColumnNames(Model, Property[])}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param keyProperties
	 * @param propertyValues
	 *            允许为{@code null}或者元素为{@code null}
	 * @return
	 */
	protected Object[] getKeyColumnValues(Connection cn, Model model, Property[] keyProperties, Object[] propertyValues)
	{
		// KEY属性不允许有模型端和属性段具体模型名，所以addModelConcreteColumn和addPropertyConcreteColumn为false，
		// 参考RelationMapperResolver.checkKeyProperty(Model, Property...)
		return getColumnValues(cn, model, keyProperties, propertyValues, false, null, false);
	}

	/**
	 * 获取属性值的列值。
	 * <p>
	 * 此方法返回值数组的元素顺序与{@linkplain #getColumnNames(Model, Property[], boolean, boolean, boolean)}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 *            允许为{@code null}
	 * @param properties
	 * @param addModelConcreteColumn
	 * @param modelOrderGenerator
	 *            允许为{@code null}，为{@code null}表示不添加
	 * @param addPropertyConcreteColumn
	 * @return
	 */
	protected Object[] getColumnValues(Connection cn, Model model, Object obj, Property[] properties,
			boolean addModelConcreteColumn, ModelOrderGenerator modelOrderGenerator, boolean addPropertyConcreteColumn)
	{
		Object[] propertyValues = (obj == null ? null : MU.getPropertyValues(model, obj, properties));

		return getColumnValues(cn, model, properties, propertyValues, addModelConcreteColumn, modelOrderGenerator,
				addPropertyConcreteColumn);
	}

	/**
	 * 获取属性值的列值。
	 * <p>
	 * 此方法返回值数组的元素顺序与{@linkplain #getColumnNames(Model, Property[], boolean, boolean, boolean)}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param properties
	 * @param propertyValues
	 *            允许为{@code null}或者元素为{@code null}
	 * @param addModelConcreteColumn
	 * @param modelOrderGenerator
	 *            允许为{@code null}，为{@code null}表示不添加
	 * @param addPropertyConcreteColumn
	 * @return
	 */
	protected Object[] getColumnValues(Connection cn, Model model, Property[] properties, Object[] propertyValues,
			boolean addModelConcreteColumn, ModelOrderGenerator modelOrderGenerator, boolean addPropertyConcreteColumn)
	{
		List<Object> columnValues = new ArrayList<Object>();

		for (int i = 0; i < properties.length; i++)
		{
			Property property = properties[i];

			RelationMapper relationMapper = getRelationMapper(model, property);
			PropertyModelMapper<?>[] propertyModelMappers = PropertyModelMapper.valueOf(property, relationMapper);

			addColumnValues(cn, model, property, propertyModelMappers,
					(propertyValues == null ? null : propertyValues[i]), addModelConcreteColumn, modelOrderGenerator,
					addPropertyConcreteColumn, columnValues);
		}

		return toObjectArray(columnValues);
	}

	/**
	 * 添加属性列值至列表。
	 * <p>
	 * 此方法的添加顺序与{@linkplain #addColumnNames(Model, Property, RelationMapper, PropertyModelMapper[], boolean, boolean, boolean, List)}一致。
	 * </p>
	 * 
	 * @param cn
	 * @param model
	 * @param property
	 * @param propertyModelMappers
	 * @param propertyValue
	 *            允许为{@code null}
	 * @param addModelConcreteColumn
	 * @param modelOrderGenerator
	 *            允许为{@code null}，为{@code null}表示不添加
	 * @param addPropertyConcreteColumn
	 * @param columnValues
	 */
	protected void addColumnValues(Connection cn, Model model, Property property,
			PropertyModelMapper<?>[] propertyModelMappers, Object propertyValue, boolean addModelConcreteColumn,
			ModelOrderGenerator modelOrderGenerator, boolean addPropertyConcreteColumn, List<Object> columnValues)
	{
		if (MU.isMultipleProperty(property))
			return;

		int pmodelIndex = getPropertyModelIndex(property, propertyValue);

		for (int i = 0; i < propertyModelMappers.length; i++)
		{
			PropertyModelMapper<?> pmm = propertyModelMappers[i];

			if (!pmm.isModelTableMapperInfo())
				continue;

			PropertyModelMapper<ModelTableMapper> mpmm = pmm.castModelTableMapperInfo();
			Model propertyModel = mpmm.getModel();
			ModelTableMapper mapper = mpmm.getMapper();

			if (mapper.isPrimitivePropertyMapper())
			{
				columnValues.add(getColumnValue(cn, model, property, pmm, (i == pmodelIndex ? propertyValue : null)));
			}
			else
			{
				Property[] propertyKeyProperties = getPropertyKeyProperties(mapper, pmm.getModel());
				Object[] propertyKeyColumnValues = getKeyColumnValues(cn, propertyModel,
						(i == pmodelIndex ? propertyValue : null), propertyKeyProperties);

				addArrayToList(columnValues, propertyKeyColumnValues);

				if (mapper.hasModelConcreteColumn() && addModelConcreteColumn)
				{
					if (i == pmodelIndex)
						columnValues.add(mapper.getModelConcreteColumnValue());
					else
						columnValues.add(null);
				}

				if (mapper.hasModelOrderColumn() && modelOrderGenerator != null)
				{
					if (i == pmodelIndex)
					{
						long modelOrder = modelOrderGenerator.generate(model, property, mpmm, propertyValue,
								propertyKeyColumnValues);

						columnValues.add(modelOrder);
					}
					else
						columnValues.add(null);
				}

				if (mapper.hasPropertyConcreteColumn() && addPropertyConcreteColumn)
				{
					if (i == pmodelIndex)
						columnValues.add(mapper.getPropertyConcreteColumnValue());
					else
						columnValues.add(null);
				}
			}
		}
	}

	/**
	 * 获取基本属性值的列值。
	 * 
	 * @param cn
	 * @param model
	 * @param property
	 * @param propertyModel
	 * @param propertyValue
	 *            允许为{@code null}
	 * @return
	 */
	protected Object getColumnValue(Connection cn, Model model, Property property, PropertyModel propertyModel,
			Object propertyValue)
	{
		if (!MU.isPrimitiveModel(propertyModel.getModel()))
			throw new IllegalArgumentException("[propertyModel.getModel()] must be primitive");

		ColumnConverter columnConverter = property.getFeature(ColumnConverter.class);

		if (columnConverter == null)
			return propertyValue;
		else
			return columnConverter.to(cn, model, property, propertyModel.getIndex(), propertyModel.getModel(),
					propertyValue);
	}

	/**
	 * 获取模型端外键列值数组。
	 * 
	 * @param cn
	 * @param mapper
	 * @param model
	 * @param obj
	 * @return
	 */
	protected Object[] getModelKeyColumnValues(Connection cn, JoinTableMapper mapper, Model model, Object obj)
	{
		Property[] keyProperties = getModelKeyProperties(mapper, model);

		return getKeyColumnValues(cn, model, obj, keyProperties);
	}

	/**
	 * 获取属性端外键列值数组。
	 * 
	 * @param cn
	 * @param mapper
	 * @param propertyModel
	 * @param propertyValue
	 * @return
	 */
	protected Object[] getPropertyKeyColumnValues(Connection cn, JoinTableMapper mapper, Model propertyModel,
			Object propertyValue)
	{
		Property[] keyProperties = getPropertyKeyProperties(mapper, propertyModel);

		return getKeyColumnValues(cn, propertyModel, propertyValue, keyProperties);
	}

	/**
	 * 获取模型端外键列值数组。
	 * 
	 * @param cn
	 * @param mapper
	 * @param model
	 * @param obj
	 * @return
	 */
	protected Object[] getModelKeyColumnValues(Connection cn, PropertyTableMapper mapper, Model model, Object obj)
	{
		Property[] keyProperties = getModelKeyProperties(mapper, model);

		return getKeyColumnValues(cn, model, obj, keyProperties);
	}

	/**
	 * 获取属性端外键列值数组。
	 * 
	 * @param cn
	 * @param mapper
	 * @param propertyModel
	 * @param propertyValue
	 * @return
	 */
	protected Object[] getPropertyKeyColumnValues(Connection cn, ModelTableMapper mapper, Model propertyModel,
			Object propertyValue)
	{
		Property[] keyProperties = getPropertyKeyProperties(mapper, propertyModel);

		return getKeyColumnValues(cn, propertyModel, propertyValue, keyProperties);
	}

	/**
	 * 获取{@linkplain ModelTableMapper}的所有列名称。
	 * 
	 * @param mapper
	 * @return
	 */
	protected String[] getModelTableMapperAllColumnNames(ModelTableMapper mapper)
	{
		String[] pkeyColumnNames = mapper.getPropertyKeyColumnNames();

		int length = pkeyColumnNames.length;
		if (mapper.hasPropertyConcreteColumn())
			length += 1;
		if (mapper.hasModelOrderColumn())
			length += 1;

		if (length == pkeyColumnNames.length)
			return pkeyColumnNames;

		String[] all = new String[length];

		System.arraycopy(pkeyColumnNames, 0, all, 0, pkeyColumnNames.length);
		int idx = pkeyColumnNames.length;
		if (mapper.hasModelConcreteColumn())
			all[idx++] = mapper.getPropertyConcreteColumnName();
		if (mapper.hasModelOrderColumn())
			all[idx++] = mapper.getModelOrderColumnName();

		return all;
	}

	/**
	 * 获取{@linkplain ModelTableMapper}的所有列值。
	 * 
	 * @param mapper
	 * @param propertyKeyColumnValues
	 * @param modelOrderColumnValue
	 *            允许为{@code null}
	 * @return
	 */
	protected Object[] getModelTableMapperAllColumnValues(ModelTableMapper mapper, Object[] propertyKeyColumnValues,
			Long modelOrderColumnValue)
	{
		int length = propertyKeyColumnValues.length;
		if (mapper.hasModelConcreteColumn())
			length += 1;
		if (mapper.hasModelOrderColumn())
			length += 1;

		if (length == propertyKeyColumnValues.length)
			return propertyKeyColumnValues;

		Object[] all = new Object[length];

		System.arraycopy(propertyKeyColumnValues, 0, all, 0, propertyKeyColumnValues.length);
		int idx = propertyKeyColumnValues.length;
		if (mapper.hasModelConcreteColumn())
			all[idx++] = mapper.getModelConcreteColumnValue();
		if (mapper.hasModelOrderColumn())
			all[idx++] = (modelOrderColumnValue == null ? 0L : modelOrderColumnValue.longValue());

		return all;
	}

	/**
	 * 获取{@linkplain PropertyTableMapper}的所有列名称。
	 * 
	 * @param mapper
	 * @return
	 */
	protected String[] getPropertyTableMapperAllColumnNames(PropertyTableMapper mapper)
	{
		String[] modelKeyColumnNames = mapper.getModelKeyColumnNames();

		int length = modelKeyColumnNames.length;
		if (mapper.hasModelConcreteColumn())
			length += 1;
		if (mapper.hasPropertyOrderColumn())
			length += 1;

		if (length == modelKeyColumnNames.length)
			return modelKeyColumnNames;

		String[] all = new String[length];

		System.arraycopy(modelKeyColumnNames, 0, all, 0, modelKeyColumnNames.length);
		int idx = modelKeyColumnNames.length;
		if (mapper.hasModelConcreteColumn())
			all[idx++] = mapper.getModelConcreteColumnName();
		if (mapper.hasPropertyOrderColumn())
			all[idx++] = mapper.getPropertyOrderColumnName();

		return all;
	}

	/**
	 * 获取{@linkplain PropertyTableMapper}的所有列值。
	 * 
	 * @param mapper
	 * @param modelKeyColumnValues
	 * @param propertyOrderColumnValue
	 *            允许为{@code null}
	 * @return
	 */
	protected Object[] getPropertyTableMapperAllColumnValues(PropertyTableMapper mapper, Object[] modelKeyColumnValues,
			Long propertyOrderColumnValue)
	{
		int length = modelKeyColumnValues.length;
		if (mapper.hasModelConcreteColumn())
			length += 1;
		if (mapper.hasPropertyOrderColumn())
			length += 1;

		if (length == modelKeyColumnValues.length)
			return modelKeyColumnValues;

		Object[] all = new Object[length];

		System.arraycopy(modelKeyColumnValues, 0, all, 0, modelKeyColumnValues.length);
		int idx = modelKeyColumnValues.length;
		if (mapper.hasModelConcreteColumn())
			all[idx++] = mapper.getModelConcreteColumnValue();
		if (mapper.hasPropertyOrderColumn())
			all[idx++] = (propertyOrderColumnValue == null ? 0L : propertyOrderColumnValue.longValue());

		return all;
	}

	/**
	 * 获取模型端外键属性数组。
	 * 
	 * @param mapper
	 * @param model
	 * @return
	 */
	protected Property[] getModelKeyProperties(JoinTableMapper mapper, Model model)
	{
		return MU.getProperties(model, mapper.getModelKeyPropertyNames());
	}

	/**
	 * 获取模型端外键属性数组。
	 * 
	 * @param mapper
	 * @param model
	 * @return
	 */
	protected Property[] getModelKeyProperties(PropertyTableMapper mapper, Model model)
	{
		return MU.getProperties(model, mapper.getModelKeyPropertyNames());
	}

	/**
	 * 获取属性端外键属性数组。
	 * 
	 * @param mapper
	 * @param propertyModel
	 * @return
	 */
	protected Property[] getPropertyKeyProperties(JoinTableMapper mapper, Model propertyModel)
	{
		return MU.getProperties(propertyModel, mapper.getPropertyKeyPropertyNames());
	}

	/**
	 * 获取属性端外键属性数组。
	 * 
	 * @param mapper
	 * @param propertyModel
	 * @return
	 */
	protected Property[] getPropertyKeyProperties(ModelTableMapper mapper, Model propertyModel)
	{
		return MU.getProperties(propertyModel, mapper.getPropertyKeyPropertyNames());
	}

	/**
	 * 获取所有{@linkplain RelationMapper}。
	 * 
	 * @param model
	 * @return
	 */
	protected RelationMapper[] getRelationMappers(Model model)
	{
		Property[] properties = model.getProperties();
		RelationMapper[] relationMappers = new RelationMapper[properties.length];

		for (int i = 0; i < properties.length; i++)
			relationMappers[i] = getRelationMapper(model, properties[i]);

		return relationMappers;
	}

	/**
	 * 获取指定{@linkplain Property}的{@linkplain RelationMapper}。
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	protected RelationMapper getRelationMapper(Model model, Property property)
	{
		RelationMapper relationMapper = property.getFeature(RelationMapper.class);

		return relationMapper;
	}

	/**
	 * 获取双向关联映射目标属性。
	 * 
	 * @param mapper
	 * @return
	 */
	protected String getMappedByWith(Mapper mapper)
	{
		if (mapper.isMappedByTarget())
			return mapper.getMappedBySource();
		else if (mapper.isMappedBySource())
			return mapper.getMappedByTarget();
		else
			return null;
	}

	/**
	 * 将列值转换为属性值。
	 * 
	 * @param cn
	 * @param rs
	 * @param row
	 * @param columnIndex
	 * @param model
	 * @param property
	 * @param propertyModel
	 * @return
	 */
	protected Object toPropertyValue(Connection cn, ResultSet rs, int row, int columnIndex, Model model,
			Property property, PropertyModel propertyModel)
	{
		ColumnConverter columnConverter = property.getFeature(ColumnConverter.class);

		if (columnConverter != null)
			return columnConverter.from(cn, rs, row, columnIndex, model, property, propertyModel.getIndex(),
					propertyModel.getModel());
		else
		{
			return getColumnValue(rs, row, columnIndex, MU.getType(propertyModel.getModel()));
		}
	}

	/**
	 * 构建ID查询视图。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @param idColumnNames
	 * @param idColumnValues
	 * @return
	 */
	protected SqlBuilder buildIdQuery(Dialect dialect, String table, Model model, String[] idColumnNames,
			Object[]... idColumnValues)
	{
		SqlBuilder sql = SqlBuilder.valueOf();

		sql.sql("SELECT ").delimit(",").sqld(idColumnNames).sql(" FROM ").sql(toQuoteName(dialect, table));

		if (idColumnValues != null && idColumnValues.length > 0)
		{
			sql.sql(" WHERE ");
			buildIdCondition(dialect, model, idColumnNames, sql, idColumnValues);
		}

		return sql;
	}

	/**
	 * 构建ID查询视图。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @param idColumnNames
	 * @param condition
	 * @return
	 */
	protected SqlBuilder buildIdQuery(Dialect dialect, String table, Model model, String[] idColumnNames,
			SqlBuilder condition)
	{
		SqlBuilder sql = SqlBuilder.valueOf();

		sql.sql("SELECT ").delimit(",").sqld(idColumnNames).sql(" FROM ").sql(toQuoteName(dialect, table));

		if (condition != null)
		{
			sql.sql(" WHERE ");
			sql.sql(condition);
		}

		return sql;
	}

	/**
	 * 构建Key字段查询视图。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @param keyProperties
	 * @param condition
	 *            允许为{@code null}
	 * @return
	 */
	protected SqlBuilder buildKeyQuery(Dialect dialect, String table, Model model, Property[] keyProperties,
			SqlBuilder condition)
	{
		SqlBuilder sql = SqlBuilder.valueOf();

		String[] keyColumns = toQuoteNames(dialect, getKeyColumnNames(model, keyProperties));

		sql.sql("SELECT ").delimit(",").sqld(keyColumns).sql(" FROM ").sql(toQuoteName(dialect, table));

		if (condition != null)
		{
			sql.sql(" WHERE ");
			sql.sql(condition);
		}

		return sql;
	}

	/**
	 * 构建ID查询条件。
	 * 
	 * @param cn
	 * @param dialect
	 * @param model
	 * @param ids
	 * @return
	 */
	protected SqlBuilder buildIdCondition(Connection cn, Dialect dialect, Model model, Object[]... ids)
	{
		String[] idColumnNames = getIdColumnNames(model);
		Object[][] idColumnValues = getIdColumnValuesForId(cn, model, ids);

		return buildIdCondition(dialect, model, idColumnNames, idColumnValues);
	}

	/**
	 * 构建ID查询条件。
	 * 
	 * @param dialect
	 * @param model
	 * @param idColumnNames
	 * @param idColumnValues
	 * @return
	 */
	protected SqlBuilder buildIdCondition(Dialect dialect, Model model, String[] idColumnNames,
			Object[]... idColumnValues)
	{
		return buildIdCondition(dialect, model, idColumnNames, SqlBuilder.valueOf(), idColumnValues);
	}

	/**
	 * 构建ID查询条件。
	 * 
	 * @param dialect
	 * @param model
	 * @param idColumnNames
	 * @param sqlBuilder
	 * @param idColumnValues
	 * @return
	 */
	protected SqlBuilder buildIdCondition(Dialect dialect, Model model, String[] idColumnNames, SqlBuilder sqlBuilder,
			Object[]... idColumnValues)
	{
		if (sqlBuilder == null)
			sqlBuilder = SqlBuilder.valueOf();

		sqlBuilder.delimit(" OR ");

		for (int i = 0; i < idColumnValues.length; i++)
		{
			Object[] idColumnValue = idColumnValues[i];

			SqlBuilder tmp = SqlBuilder.valueOf();

			if (idColumnValues.length > 1)
				tmp.sql("(");

			tmp.delimit(" AND ");

			for (int j = 0; j < idColumnNames.length; j++)
				tmp.sqldSuffix(toQuoteName(dialect, idColumnNames[j]), "= ?").arg(idColumnValue[j]);

			if (idColumnValues.length > 1)
				tmp.sql(")");

			sqlBuilder.sqld(tmp);
		}

		return sqlBuilder;
	}

	/**
	 * 构建属性表查询条件。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            模型表查询条件，允许为{@code null}。
	 * @param property
	 * @param propertyModelMapper
	 * @param propertyTableCondition
	 *            属性表查询条件，允许为{@code null}。
	 * @return
	 */
	protected SqlBuilder buildPropertyTableConditionForEntityModelTableMapper(Dialect dialect, String table,
			Model model, SqlBuilder condition, Property property,
			PropertyModelMapper<ModelTableMapper> propertyModelMapper, SqlBuilder propertyTableCondition)
	{
		ModelTableMapper mapper = propertyModelMapper.getMapper();
		Model propertyModel = propertyModelMapper.getModel();

		String[] mtableKeyColumnNames = toQuoteNames(dialect, mapper.getPropertyKeyColumnNames());

		// 构建模型表内的Key查询视图
		SqlBuilder mtableKeyQuery = SqlBuilder.valueOf();

		mtableKeyQuery.sql("SELECT ").delimit(",").sqld(mtableKeyColumnNames).sql(" FROM ")
				.sql(toQuoteName(dialect, table));

		if (condition != null)
			mtableKeyQuery.sql(" WHERE ").sql(condition);
		else
			mtableKeyQuery.sql(" WHERE 1=1");

		if (mapper.hasModelConcreteColumn())
		{
			String mconcreteColumnName = toQuoteName(dialect, mapper.getModelConcreteColumnName());
			mtableKeyQuery.sql(" AND ").sql(mconcreteColumnName).sql("=?").arg(mapper.getModelConcreteColumnValue());
		}

		SqlBuilder ptableCondition = SqlBuilder.valueOf();

		String[] ptableKeyColumnNames = getKeyColumnNames(propertyModel,
				getPropertyKeyProperties(mapper, propertyModel));

		// 构建属性表Key条件
		ptableCondition.sql("(").delimit(",").sqld(ptableKeyColumnNames).sql(") IN (").sql(mtableKeyQuery).sql(")");

		if (propertyTableCondition != null)
			ptableCondition.sql(" AND (").sql(propertyTableCondition).sql(")");

		return ptableCondition;
	}

	/**
	 * 构建属性表查询条件。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            模型表查询条件，允许为{@code null}。
	 * @param property
	 * @param propertyModelMapper
	 * @param propertyTableCondition
	 *            属性表查询条件，允许为{@code null}。
	 * @return
	 */
	protected SqlBuilder buildPropertyTableConditionForPropertyTableMapper(Dialect dialect, String table, Model model,
			SqlBuilder condition, Property property, PropertyModelMapper<PropertyTableMapper> propertyModelMapper,
			SqlBuilder propertyTableCondition)
	{
		PropertyTableMapper mapper = propertyModelMapper.getMapper();

		// 构建模型表内的属性Key查询视图
		SqlBuilder mtablekeyQuery = SqlBuilder.valueOf();

		String[] mtableKeyColumnNames = toQuoteNames(dialect,
				getKeyColumnNames(model, getModelKeyProperties(mapper, model)));

		mtablekeyQuery.sql("SELECT ").delimit(",").sqld(mtableKeyColumnNames).sql(" FROM ")
				.sql(toQuoteName(dialect, table));
		if (condition != null)
			mtablekeyQuery.sql(" WHERE ").sql(condition);

		// 构建属性表条件
		SqlBuilder ptableCondition = SqlBuilder.valueOf();

		String[] ptableKeyColumnNames = toQuoteNames(dialect, mapper.getModelKeyColumnNames());

		ptableCondition.sql("(").delimit(",").sqld(ptableKeyColumnNames).sql(") IN (").sql(mtablekeyQuery).sql(")");

		if (mapper.hasModelConcreteColumn())
		{
			String mconcreteColumnName = toQuoteName(dialect, mapper.getModelConcreteColumnName());
			ptableCondition.sql(" AND ").sql(mconcreteColumnName).sql("=?").arg(mapper.getModelConcreteColumnValue());
		}

		if (propertyTableCondition != null)
			ptableCondition.sql(" AND (").sql(propertyTableCondition).sql(")");

		return ptableCondition;
	}

	/**
	 * 构建属性表查询条件。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            模型表查询条件，允许为{@code null}。
	 * @param property
	 * @param propertyModelMapper
	 * @param propertyTableCondition
	 *            属性表查询条件，允许为{@code null}。
	 * @return
	 */
	protected SqlBuilder buildPropertyTableConditionForJoinTableMapper(Dialect dialect, String table, Model model,
			SqlBuilder condition, Property property, PropertyModelMapper<JoinTableMapper> propertyModelMapper,
			SqlBuilder propertyTableCondition)
	{
		JoinTableMapper mapper = propertyModelMapper.getMapper();
		Model propertyModel = propertyModelMapper.getModel();

		String jointable = toQuoteName(dialect, mapper.getJoinTableName());
		String[] modelKeyColumnNames = toQuoteNames(dialect, mapper.getModelKeyColumnNames());
		String[] propertyKeyColumnNames = toQuoteNames(dialect, mapper.getPropertyKeyColumnNames());

		// 构建模型表的模型Key查询视图
		SqlBuilder mtableQuery = SqlBuilder.valueOf();

		String[] mtableKeyColumnNames = toQuoteNames(dialect,
				getKeyColumnNames(model, getModelKeyProperties(mapper, model)));

		mtableQuery.sql("SELECT ").delimit(",").sqld(mtableKeyColumnNames).sql(" FROM ")
				.sql(toQuoteName(dialect, table));
		if (condition != null)
			mtableQuery.sql(" WHERE ").sql(condition);

		// 构建关联表内的属性Key查询视图
		SqlBuilder jtablePkeyQuery = SqlBuilder.valueOf();

		jtablePkeyQuery.sql("SELECT ").delimit(",").sqld(propertyKeyColumnNames).sql(" FROM ").sql(jointable)
				.sql(" WHERE ").sql("(").delimit(",").sqld(modelKeyColumnNames).sql(") IN (").sql(mtableQuery).sql(")");

		if (mapper.hasModelConcreteColumn())
		{
			String mconcreteColumnName = toQuoteName(dialect, mapper.getModelConcreteColumnName());
			jtablePkeyQuery.sql(" AND ").sql(mconcreteColumnName).sql("=?").arg(mapper.getModelConcreteColumnValue());
		}

		if (mapper.hasPropertyConcreteColumn())
		{
			String pconcreteColumnName = toQuoteName(dialect, mapper.getPropertyConcreteColumnName());
			jtablePkeyQuery.sql(" AND ").sql(pconcreteColumnName).sql("=?")
					.arg(mapper.getPropertyConcreteColumnValue());
		}

		SqlBuilder ptableCondition = SqlBuilder.valueOf();

		String[] ptableKeyColumnNames = getKeyColumnNames(propertyModel,
				getPropertyKeyProperties(mapper, propertyModel));

		ptableCondition.sql("(").delimit(",").sqld(ptableKeyColumnNames).sql(") IN (").sql(jtablePkeyQuery).sql(")");

		if (propertyTableCondition != null)
		{
			ptableCondition.sql(" AND (").sql(propertyTableCondition).sql(")");
		}

		return ptableCondition;
	}

	/**
	 * 构建关联查询条件。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @param condition
	 *            模型表查询条件，允许为{@code null}。
	 * @param property
	 * @param propertyModelMapper
	 * @param propertyTableCondition
	 *            属性表查询条件，允许为{@code null}。
	 * @return
	 */
	protected SqlBuilder buildJoinTableCondition(Dialect dialect, String table, Model model, SqlBuilder condition,
			Property property, PropertyModelMapper<JoinTableMapper> propertyModelMapper,
			SqlBuilder propertyTableCondition)
	{
		JoinTableMapper mapper = propertyModelMapper.getMapper();
		Model propertyModel = propertyModelMapper.getModel();

		String[] modelKeyColumnNames = toQuoteNames(dialect, mapper.getModelKeyColumnNames());
		String[] propertyKeyColumnNames = toQuoteNames(dialect, mapper.getPropertyKeyColumnNames());

		String ptable = toQuoteName(dialect, getTableName(propertyModel));

		// 构建模型表的模型Key查询视图
		SqlBuilder mtableQuery = SqlBuilder.valueOf();

		String[] mtableKeyColumnNames = toQuoteNames(dialect,
				getKeyColumnNames(model, getModelKeyProperties(mapper, model)));

		mtableQuery.sql("SELECT ").delimit(",").sqld(mtableKeyColumnNames).sql(" FROM ")
				.sql(toQuoteName(dialect, table));
		if (condition != null)
			mtableQuery.sql(" WHERE ").sql(condition);

		// 构建属性表的模型Key查询视图
		SqlBuilder ptableQuery = SqlBuilder.valueOf();

		String[] ptableKeyColumnNames = getKeyColumnNames(propertyModel,
				getPropertyKeyProperties(mapper, propertyModel));

		ptableQuery.sql("SELECT ").delimit(",").sqld(ptableKeyColumnNames).sql(" FROM ").sql(ptable);
		if (propertyTableCondition != null)
			ptableQuery.sql(" WHERE ").sql(propertyTableCondition);

		SqlBuilder jtableCondition = SqlBuilder.valueOf();

		jtableCondition.sql("(").delimit(",").sqld(modelKeyColumnNames).sql(") IN (").sql(mtableQuery).sql(")")
				.sql(" AND ").sql("(").delimit(",").sqld(propertyKeyColumnNames).sql(") IN (").sql(ptableQuery)
				.sql(")");

		if (mapper.hasModelConcreteColumn())
		{
			String mconcreteColumnName = toQuoteName(dialect, mapper.getModelConcreteColumnName());
			jtableCondition.sql(" AND ").sql(mconcreteColumnName).sql("=?").arg(mapper.getModelConcreteColumnValue());
		}

		if (mapper.hasPropertyConcreteColumn())
		{
			String pconcreteColumnName = toQuoteName(dialect, mapper.getPropertyConcreteColumnName());
			jtableCondition.sql(" AND ").sql(pconcreteColumnName).sql("=?")
					.arg(mapper.getPropertyConcreteColumnValue());
		}

		return jtableCondition;
	}

	/**
	 * 构建记录查询条件。
	 * <p>
	 * 此方法尽量唯一确定记录。
	 * </p>
	 * 
	 * @param cn
	 * @param dialect
	 * @param model
	 * @param objs
	 * @param ignorePropertyName
	 *            允许为{@code null}
	 * @return
	 */
	protected SqlBuilder buildRecordCondition(Connection cn, Dialect dialect, Model model, Object[] objs,
			String ignorePropertyName)
	{
		SqlBuilder condition = SqlBuilder.valueOf();
		condition.delimit(" OR ");

		for (int i = 0; i < objs.length; i++)
		{
			SqlBuilder recordCondition = SqlBuilder.valueOf().sql("(")
					.sql(buildRecordCondition(cn, dialect, model, objs[i], ignorePropertyName)).sql(")");

			condition.sqld(recordCondition);
		}

		return condition;
	}

	/**
	 * 构建记录查询条件。
	 * <p>
	 * 此方法尽量唯一确定记录。
	 * </p>
	 * 
	 * @param cn
	 * @param dialect
	 * @param model
	 * @param obj
	 * @param ignorePropertyName
	 *            允许为{@code null}
	 * @return
	 */
	protected SqlBuilder buildRecordCondition(Connection cn, Dialect dialect, Model model, Object obj,
			String ignorePropertyName)
	{
		if (MU.isEntityModel(model))
		{
			Property[] idProperties = model.getIdProperties();
			Property[] afterIdProperties = MU.removeIf(idProperties, ignorePropertyName);
			Object[] afterIdPropertyValues = MU.getPropertyValues(model, obj, afterIdProperties);

			return buildRecordCondition(cn, dialect, model, afterIdProperties, afterIdPropertyValues);
		}
		else
		{
			if (model.hasUniqueProperty())
			{
				Property[] ukProperties = MU.removeIf(model.getUniqueProperties()[0], ignorePropertyName);
				Object[] ukPropertyValues = MU.getPropertyValues(model, obj, ukProperties);

				return buildRecordCondition(cn, dialect, model, ukProperties, ukPropertyValues);
			}
			else
			{
				return buildRecordConditionForNoUnique(cn, dialect, model, obj, ignorePropertyName);
			}
		}
	}

	/**
	 * 构建没有KEY键的记录查询条件。
	 * <p>
	 * 此方法尽量唯一确定记录。
	 * </p>
	 * 
	 * @param cn
	 * @param dialect
	 * @param model
	 * @param obj
	 * @param ignorePropertyName
	 *            允许为{@code null}
	 * @return
	 */
	protected SqlBuilder buildRecordConditionForNoUnique(Connection cn, Dialect dialect, Model model, Object obj,
			String ignorePropertyName)
	{
		List<Property> recordProperties = new ArrayList<Property>();
		List<Object> recordPropertyValues = new ArrayList<Object>();

		Property[] properties = model.getProperties();

		for (Property property : properties)
		{
			if (property.hasFeature(NotReadable.class))
				continue;

			if (property.getName().equals(ignorePropertyName))
				continue;

			boolean add = false;

			Object propertyValue = MU.getPropertyValue(model, obj, property);

			// null可作为记录条件
			if (propertyValue == null)
			{
				add = true;
			}
			else
			{
				PropertyModelMapper<?> propertyModelMapper = PropertyModelMapper.valueOf(property,
						getRelationMapper(model, property), propertyValue);

				if (propertyModelMapper.isModelTableMapperInfo())
				{
					JdbcType jdbcType = property.getFeature(JdbcType.class);

					if (jdbcType != null)
					{
						int jdbcTypeValue = jdbcType.getValue(propertyModelMapper.getIndex());

						if (Types.BINARY == jdbcTypeValue || Types.BLOB == jdbcTypeValue
								|| Types.LONGVARBINARY == jdbcTypeValue || Types.VARBINARY == jdbcTypeValue)
						{
							add = false;
						}
						else
						{
							// XXX CLOB要不要呢？
							add = true;
						}
					}
					else
					{
						if (MU.isType(propertyModelMapper.getModel(), java.io.File.class))
							add = false;
						else
							add = true;
					}
				}
				else
					add = false;
			}

			if (add)
			{
				recordProperties.add(property);
				recordPropertyValues.add(propertyValue);
			}
		}

		return buildRecordCondition(cn, dialect, model, recordProperties.toArray(new Property[recordProperties.size()]),
				recordPropertyValues.toArray(new Object[recordPropertyValues.size()]));
	}

	/**
	 * 构建记录查询条件。
	 * 
	 * @param cn
	 * @param dialect
	 * @param model
	 * @param properties
	 * @param propertyValues
	 *            允许为{@code null}或者元素为{@code null}。
	 * @return
	 */
	protected SqlBuilder buildRecordCondition(Connection cn, Dialect dialect, Model model, Property[] properties,
			Object[] propertyValues)
	{
		if (properties == null || properties.length == 0)
			return SqlBuilder.valueOf();

		String[] columnNames = getKeyColumnNames(model, properties);

		if (columnNames == null || columnNames.length == 0)
			throw new IllegalArgumentException("[properties] has no inline column");

		Object[] columnValues = getKeyColumnValues(cn, model, properties, propertyValues);

		SqlBuilder sql = SqlBuilder.valueOf();
		sql.delimit(" AND ");

		for (int i = 0; i < columnNames.length; i++)
		{
			String columnNameQuote = toQuoteName(dialect, columnNames[i]);
			Object columnValue = columnValues[i];

			if (columnValue == null)
				sql.sqldSuffix(columnNameQuote, " IS NULL ");
			else
				sql.sqldSuffix(columnNameQuote, " =? ").arg(columnValue);
		}

		return sql;
	}

	/**
	 * 获取指定属性值的属性模型索引。
	 * <p>
	 * 如果{@code propertyValue}为{@code null}，将返回{@code -1}。
	 * </p>
	 * 
	 * @param property
	 * @param propertyValue
	 *            允许为{@code null}
	 * @return
	 */
	protected int getPropertyModelIndex(Property property, Object propertyValue)
	{
		int index = (propertyValue == null ? -1 : PropertyModel.valueOf(property, propertyValue).getIndex());

		return index;
	}

	/**
	 * 获取模型表名称。
	 * 
	 * @param model
	 * @return
	 */
	protected String getTableName(Model model)
	{
		TableName tableName = model.getFeature(TableName.class);

		return (tableName == null ? model.getName() : tableName.getValue());
	}

	/**
	 * 将对象按照模型归类。
	 * 
	 * @param models
	 * @param objs
	 * @return
	 */
	protected List<IndexValue<Object>>[] sortByModel(Model[] models, Object[] objs)
	{
		@SuppressWarnings("unchecked")
		List<IndexValue<Object>>[] indexValuess = new ArrayList[models.length];

		for (int i = 0; i < models.length; i++)
		{
			List<IndexValue<Object>> indexValues = new ArrayList<IndexValue<Object>>();

			for (int j = 0; j < objs.length; j++)
			{
				if (MU.isModelData(models[i], objs[j]))
					indexValues.add(new IndexValue<Object>(j, objs[j]));
			}

			indexValuess[i] = indexValues;
		}

		return indexValuess;
	}

	/**
	 * 转换为引号名字。
	 * 
	 * @param dialect
	 * @param name
	 * @return
	 */
	protected String toQuoteName(Dialect dialect, String name)
	{
		String iq = dialect.getIdentifierQuote();

		return iq + name + iq;
	}

	/**
	 * 转换为引号名字。
	 * 
	 * @param dialect
	 * @param names
	 * @return
	 */
	protected String[] toQuoteNames(Dialect dialect, String[] names)
	{
		String[] qnames = new String[names.length];

		String iq = dialect.getIdentifierQuote();

		for (int i = 0; i < names.length; i++)
			qnames[i] = iq + names[i] + iq;

		return qnames;
	}

	/**
	 * 判断对象是否是数组或者集合。
	 * 
	 * @param obj
	 * @return
	 */
	protected boolean isArrayOrCollection(Object obj)
	{
		if (obj == null)
			return false;

		if (obj instanceof Object[])
			return true;

		if (Collection.class.isAssignableFrom(obj.getClass()))
			return true;

		return false;
	}

	/**
	 * 给定数组是否为{@code null}或者有{@code null}的元素。
	 * 
	 * @param array
	 * @return
	 */
	protected boolean isNullOrNullElement(Object[] array)
	{
		if (array == null)
			return true;

		for (Object element : array)
		{
			if (element == null)
				return true;
		}

		return false;
	}

	/**
	 * 将对象转换为数组。
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Object[] toArray(Object obj)
	{
		if (obj == null)
			return null;

		if (obj instanceof Object[])
			return (Object[]) obj;
		else if (List.class.isAssignableFrom(obj.getClass()))
		{
			return ((List<Object>) obj).toArray();
		}
		else if (Collection.class.isAssignableFrom(obj.getClass()))
		{
			Collection<Object> cobj = (Collection<Object>) obj;

			Object[] array = new Object[cobj.size()];

			int i = 0;
			for (Object eobj : cobj)
			{
				array[i] = eobj;
				i++;
			}

			return array;
		}
		else
			return new Object[] { obj };
	}

	/**
	 * 将数组添加至列表。
	 * 
	 * @param list
	 * @param array
	 */
	protected <T> void addArrayToList(List<T> list, T[] array)
	{
		for (T t : array)
			list.add(t);
	}

	/**
	 * 将字符串列表转换为字符串数组。
	 * 
	 * @param list
	 * @return
	 */
	protected String[] toStringArray(List<String> list)
	{
		return list.toArray(new String[list.size()]);
	}

	/**
	 * 将对象列表转换为对象数组。
	 * 
	 * @param list
	 * @return
	 */
	protected Object[] toObjectArray(List<?> list)
	{
		return list.toArray(new Object[list.size()]);
	}

	/**
	 * 支持返回并设置生成值得更新操作。
	 * 
	 * @param cn
	 * @param sql
	 * @param model
	 * @param autoGeneratedProperties
	 * @param autoGeneratedPropertyNames
	 * @param generatedObj
	 * @return
	 */
	protected int executeUpdateForGeneratedProperties(Connection cn, SqlBuilder sql, Model model,
			List<Property> autoGeneratedProperties, List<String> autoGeneratedPropertyNames, Object generatedObj)
	{
		if (!autoGeneratedProperties.isEmpty())
		{
			GeneratedKeysUpdateResult result = executeUpdateWithGeneratedKeys(cn, sql,
					autoGeneratedPropertyNames.toArray(new String[autoGeneratedPropertyNames.size()]));

			ResultSet generatedKeys = result.getGeneratedKeys();

			try
			{
				if (generatedKeys.next())
				{
					ResultSetMetaData rsm = generatedKeys.getMetaData();
					int generatedKeyColumnCount = rsm.getColumnCount();

					if (generatedKeyColumnCount != autoGeneratedProperties.size())
						throw new PersistenceException(
								"The generated key Resultset column count [" + generatedKeyColumnCount
										+ "] does not match Property count [" + autoGeneratedProperties.size() + "]");

					for (int i = 1; i <= generatedKeyColumnCount; i++)
					{
						Property autoGeneratedProperty = autoGeneratedProperties.get(i - 1);

						Object propValue = toPropertyValue(cn, generatedKeys, 1, i, model, autoGeneratedProperty,
								PropertyModel.valueOf(autoGeneratedProperty, autoGeneratedProperty.getModel()));

						autoGeneratedProperty.set(generatedObj, propValue);
					}
				}

				return result.getUpdateCount();
			}
			catch (SQLException e)
			{
				throw new PersistenceException(e);
			}
			finally
			{
				result.close();
			}
		}
		else
		{
			return executeUpdate(cn, sql);
		}
	}

	/**
	 * 模型端排序值生成器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static interface ModelOrderGenerator
	{
		/**
		 * 生成模型端排序值。
		 * 
		 * @param model
		 * @param property
		 * @param propertyModelMapper
		 * @param propertyValue
		 * @param propertyKeyColumnValues
		 * @return
		 */
		long generate(Model model, Property property, PropertyModelMapper<ModelTableMapper> propertyModelMapper,
				Object propertyValue, Object[] propertyKeyColumnValues);
	}
}
