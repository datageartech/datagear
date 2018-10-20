/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.UnsupportedModelCharacterException;
import org.datagear.persistence.features.ColumnName;
import org.datagear.persistence.features.KeyRule;
import org.datagear.persistence.features.ManyToMany;
import org.datagear.persistence.features.ManyToOne;
import org.datagear.persistence.features.MappedBy;
import org.datagear.persistence.features.ModelConcreteColumnName;
import org.datagear.persistence.features.ModelConcreteColumnValue;
import org.datagear.persistence.features.ModelKeyColumnName;
import org.datagear.persistence.features.ModelKeyDeleteRule;
import org.datagear.persistence.features.ModelKeyPropertyName;
import org.datagear.persistence.features.ModelKeyUpdateRule;
import org.datagear.persistence.features.ModelOrderColumnName;
import org.datagear.persistence.features.OneToOne;
import org.datagear.persistence.features.PointType;
import org.datagear.persistence.features.PropertyConcreteColumnName;
import org.datagear.persistence.features.PropertyConcreteColumnValue;
import org.datagear.persistence.features.PropertyKeyColumnName;
import org.datagear.persistence.features.PropertyKeyDeleteRule;
import org.datagear.persistence.features.PropertyKeyPropertyName;
import org.datagear.persistence.features.PropertyKeyUpdateRule;
import org.datagear.persistence.features.PropertyOrderColumnName;
import org.datagear.persistence.features.RelationPoint;
import org.datagear.persistence.features.TableName;

/**
 * 关联关系映射解析器。
 * 
 * @author datagear@163.com
 *
 */
public class RelationMapperResolver
{
	public RelationMapperResolver()
	{
		super();
	}

	/**
	 * 解析{@linkplain RelationMapper}。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	public RelationMapper resolve(Model model, Property property)
	{
		RelationType relationType = resolveRelationType(model, property);

		RelationMapperImpl relationMapperImpl = new RelationMapperImpl();
		relationMapperImpl.setRelationType(relationType);

		Mapper[] mappers = resolveMappers(model, property, relationType);
		relationMapperImpl.setMappers(mappers);

		return relationMapperImpl;
	}

	/**
	 * 解析所有{@linkplain RelationMapper}。
	 * 
	 * @param model
	 * @return
	 */
	public RelationMapper[] resolveAll(Model model)
	{
		if (!MU.isCompositeModel(model))
			throw new IllegalArgumentException("[model] must be composite");

		Property[] properties = model.getProperties();

		RelationMapper[] relationMappers = new RelationMapper[properties.length];

		for (int i = 0; i < properties.length; i++)
			relationMappers[i] = resolve(model, properties[i]);

		return relationMappers;
	}

	/**
	 * 解析属性的关系类型。
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	protected RelationType resolveRelationType(Model model, Property property)
	{
		if (MU.isSingleProperty(property))
		{
			if (property.hasFeature(ManyToOne.class))
				return RelationType.MANY_TO_ONE;
			else
				return RelationType.ONE_TO_ONE;
		}
		else
		{
			if (property.hasFeature(ManyToMany.class))
				return RelationType.MANY_TO_MANY;
			else
				return RelationType.ONE_TO_MANY;
		}
	}

	/**
	 * 解析{@linkplain Mapper}数组。
	 * 
	 * @param model
	 * @param property
	 * @param relationType
	 */
	protected Mapper[] resolveMappers(Model model, Property property, RelationType relationType)
	{
		Model[] pcmodels = MU.getModels(property);

		Mapper[] mappers = new Mapper[pcmodels.length];

		for (int i = 0; i < pcmodels.length; i++)
		{
			Mapper mapper = resolveMapper(model, property, pcmodels[i], i, relationType);
			mappers[i] = mapper;
		}

		return mappers;
	}

	/**
	 * 解析{@linkplain Mapper}。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param relationType
	 * @return
	 */
	protected Mapper resolveMapper(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, RelationType relationType)
	{
		Mapper mapper = null;

		Property mappedBySource = findMappedBySource(model, property, propertyConcreteModel, propertyConcreteModelIdx);
		String mappedBySourceName = (mappedBySource == null ? null : mappedBySource.getName());

		if (RelationType.ONE_TO_ONE.equals(relationType))
		{
			mapper = resolveOneToOneMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					mappedBySourceName);
		}
		else if (RelationType.ONE_TO_MANY.equals(relationType))
		{
			mapper = resolveOneToManyMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					mappedBySourceName);
		}
		else if (RelationType.MANY_TO_ONE.equals(relationType))
		{
			mapper = resolveManyToOneMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					mappedBySourceName);
		}
		else if (RelationType.MANY_TO_MANY.equals(relationType))
		{
			mapper = resolveManyToManyMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					mappedBySourceName);
		}
		else
			throw new UnsupportedOperationException();

		return mapper;
	}

	/**
	 * 解析一对一映射。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param mappedBySource
	 * @return
	 */
	protected Mapper resolveOneToOneMapper(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, String mappedBySource)
	{
		Mapper mapper = null;

		Property mappedByTargetProperty = null;

		if (mappedBySource == null)
			mappedByTargetProperty = findMappedByTarget(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);

		if (mappedByTargetProperty != null)
		{
			int myIdx = MU.getPropertyModelIndex(mappedByTargetProperty, model);

			mapper = resolveOneToOneMapper(propertyConcreteModel, mappedByTargetProperty, model, myIdx,
					property.getName());
			mapper = reverseMappedByTargetMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					RelationType.ONE_TO_ONE, mappedByTargetProperty, mapper);
		}
		else
		{
			PointType pointType = resolveRelationPointType(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, RelationType.ONE_TO_ONE);

			if (PointType.JOIN.equals(pointType))
			{
				mapper = resolveJoinTableMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
						RelationType.ONE_TO_ONE, mappedBySource);
			}
			else if (PointType.PROPERTY.equals(pointType))
			{
				mapper = resolvePropertyTableMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
						RelationType.ONE_TO_ONE, mappedBySource);
			}
			else
			{
				mapper = resolveModelTableMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
						RelationType.ONE_TO_ONE, mappedBySource);
			}
		}

		return mapper;
	}

	/**
	 * 解析一对多映射。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param mappedBySource
	 * @return
	 */
	protected Mapper resolveOneToManyMapper(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, String mappedBySource)
	{
		Mapper mapper = null;

		Property mappedByTargetProperty = null;

		if (mappedBySource == null)
			mappedByTargetProperty = findMappedByTarget(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);

		if (mappedByTargetProperty != null)
		{
			int myIdx = MU.getPropertyModelIndex(mappedByTargetProperty, model);

			mapper = resolveManyToOneMapper(propertyConcreteModel, mappedByTargetProperty, model, myIdx,
					property.getName());
			mapper = reverseMappedByTargetMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					RelationType.ONE_TO_MANY, mappedByTargetProperty, mapper);
		}
		else
		{
			PointType pointType = resolveRelationPointType(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, RelationType.ONE_TO_ONE);

			if (PointType.JOIN.equals(pointType))
			{
				mapper = resolveJoinTableMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
						RelationType.ONE_TO_MANY, mappedBySource);
			}
			else
			{
				mapper = resolvePropertyTableMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
						RelationType.ONE_TO_MANY, mappedBySource);
			}
		}

		return mapper;
	}

	/**
	 * 解析多对一映射。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param mappedBySource
	 * @return
	 */
	protected Mapper resolveManyToOneMapper(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, String mappedBySource)
	{
		Mapper mapper = null;

		Property mappedByTargetProperty = null;

		if (mappedBySource == null)
			mappedByTargetProperty = findMappedByTarget(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);

		if (mappedByTargetProperty != null)
		{
			int myIdx = MU.getPropertyModelIndex(mappedByTargetProperty, model);

			mapper = resolveOneToManyMapper(propertyConcreteModel, mappedByTargetProperty, model, myIdx,
					property.getName());
			mapper = reverseMappedByTargetMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					RelationType.MANY_TO_ONE, mappedByTargetProperty, mapper);
		}
		else
		{
			PointType pointType = resolveRelationPointType(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, RelationType.ONE_TO_ONE);

			if (PointType.JOIN.equals(pointType))
			{
				mapper = resolveJoinTableMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
						RelationType.MANY_TO_ONE, mappedBySource);
			}
			else
			{
				mapper = resolveModelTableMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
						RelationType.MANY_TO_ONE, mappedBySource);
			}
		}

		return mapper;
	}

	/**
	 * 解析多对多映射。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param mappedBySource
	 * @return
	 */
	protected Mapper resolveManyToManyMapper(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, String mappedBySource)
	{
		Mapper mapper = null;

		Property mappedByTargetProperty = null;

		if (mappedBySource == null)
			mappedByTargetProperty = findMappedByTarget(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);

		if (mappedByTargetProperty != null)
		{
			int myIdx = MU.getPropertyModelIndex(mappedByTargetProperty, model);

			mapper = resolveManyToManyMapper(propertyConcreteModel, mappedByTargetProperty, model, myIdx,
					property.getName());
			mapper = reverseMappedByTargetMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					RelationType.MANY_TO_MANY, mappedByTargetProperty, mapper);
		}
		else
		{
			mapper = resolveJoinTableMapper(model, property, propertyConcreteModel, propertyConcreteModelIdx,
					RelationType.MANY_TO_MANY, mappedBySource);
		}

		return mapper;
	}

	/**
	 * 解析{@linkplain ModelTableMapper}。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param relationType
	 * @param mappedBySource
	 * @return
	 */
	protected ModelTableMapper resolveModelTableMapper(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, RelationType relationType, String mappedBySource)
	{
		ModelTableMapperImpl mapperImpl = new ModelTableMapperImpl();

		mapperImpl.setMappedBySource(mappedBySource);

		mapperImpl.setPrimitivePropertyMapper(MU.isPrimitiveModel(propertyConcreteModel));

		if (mapperImpl.isPrimitivePropertyMapper())
		{
			String columnName = resolvePropertyColumnName(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);
			mapperImpl.setPrimitiveColumnName(columnName);
		}
		else
		{
			Property[] pkeyProperties = resolvePropertyKeyProperties(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);
			mapperImpl.setPropertyKeyPropertyNames(MU.getPropertyNames(propertyConcreteModel, pkeyProperties));

			String[] pkeyColumnNames = resolvePropertyKeyColumnNames(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, pkeyProperties);
			mapperImpl.setPropertyKeyColumnNames(pkeyColumnNames);

			resolveConcreteColumnInfo(model, property, propertyConcreteModel, propertyConcreteModelIdx, mapperImpl,
					relationType);

			String morderColumnName = resolveModelOrderColumnName(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);
			if (morderColumnName != null)
				mapperImpl.setModelOrderColumnName(morderColumnName);

			mapperImpl.setPropertyKeyUpdateRule(resolvePropertyKeyUpdateRule(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, mappedBySource));
			mapperImpl.setPropertyKeyDeleteRule(resolvePropertyKeyDeleteRule(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, mappedBySource));
		}

		return mapperImpl;
	}

	/**
	 * 解析{@linkplain PropertyTableMapper}。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param relationType
	 * @param mappedBySource
	 * @return
	 */
	protected PropertyTableMapper resolvePropertyTableMapper(Model model, Property property,
			Model propertyConcreteModel, int propertyConcreteModelIdx, RelationType relationType, String mappedBySource)
	{
		PropertyTableMapperImpl mapperImpl = new PropertyTableMapperImpl();

		mapperImpl.setMappedBySource(mappedBySource);

		mapperImpl.setPrimitivePropertyMapper(MU.isPrimitiveModel(propertyConcreteModel));

		if (mapperImpl.isPrimitivePropertyMapper())
		{
			mapperImpl.setPrimitiveTableName(resolvePropertyTableName(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, relationType));
			mapperImpl.setPrimitiveColumnName(
					resolvePropertyColumnName(model, property, propertyConcreteModel, propertyConcreteModelIdx));
		}

		Property[] mkeyProperties = resolveModelKeyProperties(model, property, propertyConcreteModel,
				propertyConcreteModelIdx);
		mapperImpl.setModelKeyPropertyNames(MU.getPropertyNames(model, mkeyProperties));

		String[] mkeyColumnNames = resolveModelKeyColumnNames(model, property, propertyConcreteModel,
				propertyConcreteModelIdx, mkeyProperties);
		mapperImpl.setModelKeyColumnNames(mkeyColumnNames);

		resolveConcreteColumnInfo(model, property, propertyConcreteModel, propertyConcreteModelIdx, mapperImpl,
				relationType);

		String porderColumnName = resolvePropertyOrderColumnName(model, property, propertyConcreteModel,
				propertyConcreteModelIdx);
		if (porderColumnName != null)
			mapperImpl.setPropertyOrderColumnName(porderColumnName);

		mapperImpl.setPropertyKeyUpdateRule(resolvePropertyKeyUpdateRule(model, property, propertyConcreteModel,
				propertyConcreteModelIdx, mappedBySource));
		mapperImpl.setPropertyKeyDeleteRule(resolvePropertyKeyDeleteRule(model, property, propertyConcreteModel,
				propertyConcreteModelIdx, mappedBySource));

		return mapperImpl;
	}

	/**
	 * 解析{@linkplain JoinTableMapper}。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param relationType
	 * @param mappedBySource
	 * @return
	 */
	protected JoinTableMapper resolveJoinTableMapper(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, RelationType relationType, String mappedBySource)
	{
		JoinTableMapperImpl mapperImpl = new JoinTableMapperImpl();

		mapperImpl.setMappedBySource(mappedBySource);

		String tableName = resolveJoinTableName(model, property, propertyConcreteModel, propertyConcreteModelIdx,
				relationType);
		mapperImpl.setJoinTableName(tableName);

		Property[] mkeyProperties = resolveModelKeyProperties(model, property, propertyConcreteModel,
				propertyConcreteModelIdx);
		mapperImpl.setModelKeyPropertyNames(MU.getPropertyNames(model, mkeyProperties));

		String[] mkeyColumnNames = resolveModelKeyColumnNames(model, property, propertyConcreteModel,
				propertyConcreteModelIdx, mkeyProperties);
		mapperImpl.setModelKeyColumnNames(mkeyColumnNames);

		Property[] pkeyProperties = resolvePropertyKeyProperties(model, property, propertyConcreteModel,
				propertyConcreteModelIdx);
		mapperImpl.setPropertyKeyPropertyNames(MU.getPropertyNames(propertyConcreteModel, pkeyProperties));

		String[] pkeyColumnNames = resolvePropertyKeyColumnNames(model, property, propertyConcreteModel,
				propertyConcreteModelIdx, pkeyProperties);
		mapperImpl.setPropertyKeyColumnNames(pkeyColumnNames);

		resolveConcreteColumnInfo(model, property, propertyConcreteModel, propertyConcreteModelIdx, mapperImpl,
				relationType);

		String porderColumnName = resolvePropertyOrderColumnName(model, property, propertyConcreteModel,
				propertyConcreteModelIdx);
		if (porderColumnName != null)
			mapperImpl.setPropertyOrderColumnName(porderColumnName);

		String morderColumnName = resolveModelOrderColumnName(model, property, propertyConcreteModel,
				propertyConcreteModelIdx);
		if (morderColumnName != null)
			mapperImpl.setModelOrderColumnName(morderColumnName);

		mapperImpl.setPropertyKeyUpdateRule(resolvePropertyKeyUpdateRule(model, property, propertyConcreteModel,
				propertyConcreteModelIdx, mappedBySource));
		mapperImpl.setPropertyKeyDeleteRule(resolvePropertyKeyDeleteRule(model, property, propertyConcreteModel,
				propertyConcreteModelIdx, mappedBySource));

		return mapperImpl;
	}

	/**
	 * 解析{@linkplain Mapper#getModelConcreteColumnName()}、
	 * {@linkplain Mapper#getModelConcreteColumnValue()}和
	 * {@linkplain Mapper#getPropertyConcreteColumnName()}、
	 * {@linkplain Mapper#getPropertyConcreteColumnValue()}。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param mapper
	 * @param relationType
	 */
	protected void resolveConcreteColumnInfo(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, AbstractMapper mapper, RelationType relationType)
	{
		String mconcreteColumnName = resolveModelConcreteColumnName(model, property, propertyConcreteModel,
				propertyConcreteModelIdx);

		if (mconcreteColumnName != null && !mconcreteColumnName.isEmpty())
		{
			Object pconcreteColumnValue = resolveModelConcreteColumnValue(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);

			mapper.setModelConcreteColumnName(mconcreteColumnName);
			mapper.setModelConcreteColumnValue(pconcreteColumnValue);
		}

		String pconcreteColumnName = resolvePropertyConcreteColumnName(model, property, propertyConcreteModel,
				propertyConcreteModelIdx);

		if (pconcreteColumnName != null && !pconcreteColumnName.isEmpty())
		{
			Object pconcreteColumnValue = resolvePropertyConcreteColumnValue(model, property, propertyConcreteModel,
					propertyConcreteModelIdx);

			mapper.setPropertyConcreteColumnName(pconcreteColumnName);
			mapper.setPropertyConcreteColumnValue(pconcreteColumnValue);
		}
	}

	/**
	 * 解析{@linkplain PointType}。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param relationType
	 * @return
	 */
	protected PointType resolveRelationPointType(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, RelationType relationType)
	{
		RelationPoint relationPoint = property.getFeature(RelationPoint.class);
		if (relationPoint != null)
			return relationPoint.getValue(propertyConcreteModelIdx);

		Property mappedByProperty = null;
		int mappedByIndex = -1;

		MappedBy mappedBy = property.getFeature(MappedBy.class);
		if (mappedBy != null)
		{
			String mappedByName = mappedBy.getValue(propertyConcreteModelIdx);

			if (mappedByName == null)
				throw new RelationMapperResolverException(
						"[" + model + "] 's [" + property + "] 's " + MappedBy.class.getSimpleName() + " feature 's "
								+ propertyConcreteModelIdx + "-th value must not be null");

			mappedByProperty = propertyConcreteModel.getProperty(mappedByName);

			if (mappedByProperty == null)
				throw new RelationMapperResolverException(
						"No property named [" + mappedByName + "] found in [" + model + "]");

			mappedByIndex = MU.getPropertyModelIndex(mappedByProperty, model);
		}

		if (RelationType.ONE_TO_ONE.equals(relationType))
		{
			if (mappedByProperty != null)
			{
				PointType mappedPointType = resolveRelationPointType(propertyConcreteModel, mappedByProperty, model,
						mappedByIndex, RelationType.ONE_TO_ONE);

				if (PointType.MODEL.equals(mappedPointType))
					return PointType.PROPERTY;
				else if (PointType.PROPERTY.equals(mappedPointType))
					return PointType.MODEL;
				else
					return mappedPointType;
			}
			else if (property.hasFeature(PropertyKeyColumnName.class) && property.hasFeature(ModelKeyColumnName.class))
				return PointType.JOIN;
			else if (property.hasFeature(PropertyKeyColumnName.class))
				return PointType.MODEL;
			else if (property.hasFeature(ModelKeyColumnName.class))
				return PointType.PROPERTY;
			else if (MU.isPrimitiveModel(propertyConcreteModel))
				return PointType.MODEL;
			else
				throw new RelationMapperResolverException(
						"The " + PointType.class.getSimpleName() + " for [" + model + "] 's property [" + property
								+ "] 's " + propertyConcreteModelIdx + "-th property Model resolving is ambiguous");
		}
		else if (RelationType.ONE_TO_MANY.equals(relationType))
		{
			return PointType.PROPERTY;
		}
		else if (RelationType.MANY_TO_ONE.equals(relationType))
		{
			return PointType.MODEL;
		}
		else if (RelationType.MANY_TO_MANY.equals(relationType))
		{
			return PointType.JOIN;
		}
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * 查找{@linkplain MappedBy}目标属性。
	 * <p>
	 * 如果无{@linkplain MappedBy}，此方法将返回{@code null}。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyconcreteModel
	 * @param propertyconcreteModelIdx
	 * @return
	 */
	protected Property findMappedByTarget(Model model, Property property, Model propertyconcreteModel,
			int propertyconcreteModelIdx)
	{
		MappedBy mappedBy = property.getFeature(MappedBy.class);

		if (mappedBy == null)
			return null;

		String mappedByTargetName = mappedBy.getValue(propertyconcreteModelIdx);

		return propertyconcreteModel.getProperty(mappedByTargetName);
	}

	/**
	 * 查找{@linkplain MappedBy}源属性。
	 * <p>
	 * 如果无{@linkplain MappedBy}源，此方法将返回{@code null}。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyconcreteModel
	 * @param propertyconcreteModelIdx
	 * @return
	 */
	protected Property findMappedBySource(Model model, Property property, Model propertyconcreteModel,
			int propertyconcreteModelIdx)
	{
		MappedBy mappedBy = property.getFeature(MappedBy.class);

		if (mappedBy != null)
			return null;

		Property[] propertyProperties = propertyconcreteModel.getProperties();

		if (propertyProperties == null)
			return null;

		String propertyName = property.getName();

		for (Property propertyProperty : propertyProperties)
		{
			MappedBy propertyMappedBy = propertyProperty.getFeature(MappedBy.class);

			if (propertyMappedBy == null)
				continue;

			int myModelIdx = MU.getModelIndex(propertyProperty.getModels(), model);

			if (myModelIdx < 0)
				continue;

			if (propertyName.equals(propertyMappedBy.getValue(myModelIdx)))
				return propertyProperty;
		}

		return null;
	}

	/**
	 * 反转{@linkplain MappedBy}目标{@linkplain Mapper}关系。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param relationType
	 * @param mappedByTargetProperty
	 * @param mappedByTargetMapper
	 * @return
	 */
	protected Mapper reverseMappedByTargetMapper(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, RelationType relationType, Property mappedByTargetProperty,
			Mapper mappedByTargetMapper)
	{
		if (mappedByTargetMapper instanceof ModelTableMapper)
		{
			ModelTableMapper src = (ModelTableMapper) mappedByTargetMapper;

			PropertyTableMapperImpl dest = new PropertyTableMapperImpl();

			dest.setPrimitivePropertyMapper(MU.isPrimitiveModel(propertyConcreteModel));

			if (dest.isPrimitivePropertyMapper())
			{
				dest.setPrimitiveTableName(resolvePropertyTableName(model, property, propertyConcreteModel,
						propertyConcreteModelIdx, relationType));
				dest.setPrimitiveColumnName(
						resolvePropertyColumnName(model, property, propertyConcreteModel, propertyConcreteModelIdx));
			}

			dest.setModelKeyPropertyNames(src.getPropertyKeyPropertyNames());
			dest.setModelKeyColumnNames(src.getPropertyKeyColumnNames());
			dest.setModelConcreteColumnName(src.getPropertyConcreteColumnName());
			dest.setModelConcreteColumnValue(src.getPropertyConcreteColumnValue());
			dest.setPropertyOrderColumnName(src.getModelOrderColumnName());

			dest.setMappedByTarget(mappedByTargetProperty.getName());

			dest.setPropertyKeyUpdateRule(resolvePropertyKeyUpdateRule(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, mappedByTargetProperty.getName()));
			dest.setPropertyKeyDeleteRule(resolvePropertyKeyDeleteRule(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, mappedByTargetProperty.getName()));

			return dest;
		}
		else if (mappedByTargetMapper instanceof PropertyTableMapper)
		{
			PropertyTableMapper src = (PropertyTableMapper) mappedByTargetMapper;

			ModelTableMapperImpl dest = new ModelTableMapperImpl();

			dest.setPrimitivePropertyMapper(MU.isPrimitiveModel(propertyConcreteModel));

			if (dest.isPrimitivePropertyMapper())
			{
				dest.setPrimitiveColumnName(
						resolvePropertyColumnName(model, property, propertyConcreteModel, propertyConcreteModelIdx));
			}

			dest.setPropertyKeyPropertyNames(src.getModelKeyPropertyNames());
			dest.setPropertyKeyColumnNames(src.getModelKeyColumnNames());
			dest.setPropertyConcreteColumnName(src.getModelConcreteColumnName());
			dest.setPropertyConcreteColumnValue(src.getModelConcreteColumnValue());
			dest.setModelOrderColumnName(src.getPropertyOrderColumnName());

			dest.setMappedByTarget(mappedByTargetProperty.getName());

			dest.setPropertyKeyUpdateRule(resolvePropertyKeyUpdateRule(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, mappedByTargetProperty.getName()));
			dest.setPropertyKeyDeleteRule(resolvePropertyKeyDeleteRule(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, mappedByTargetProperty.getName()));

			return dest;
		}
		else if (mappedByTargetMapper instanceof JoinTableMapper)
		{
			JoinTableMapper src = (JoinTableMapper) mappedByTargetMapper;

			JoinTableMapperImpl dest = new JoinTableMapperImpl();

			dest.setJoinTableName(src.getJoinTableName());

			dest.setModelKeyPropertyNames(src.getPropertyKeyPropertyNames());
			dest.setModelKeyColumnNames(src.getPropertyKeyColumnNames());
			dest.setModelConcreteColumnName(src.getPropertyConcreteColumnName());
			dest.setModelConcreteColumnValue(src.getPropertyConcreteColumnValue());
			dest.setPropertyOrderColumnName(src.getModelOrderColumnName());

			dest.setPropertyKeyPropertyNames(src.getModelKeyPropertyNames());
			dest.setPropertyKeyColumnNames(src.getModelKeyColumnNames());
			dest.setPropertyConcreteColumnName(src.getModelConcreteColumnName());
			dest.setPropertyConcreteColumnValue(src.getModelConcreteColumnValue());
			dest.setModelOrderColumnName(src.getPropertyOrderColumnName());

			dest.setMappedByTarget(mappedByTargetProperty.getName());

			dest.setPropertyKeyUpdateRule(resolvePropertyKeyUpdateRule(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, mappedByTargetProperty.getName()));
			dest.setPropertyKeyDeleteRule(resolvePropertyKeyDeleteRule(model, property, propertyConcreteModel,
					propertyConcreteModelIdx, mappedByTargetProperty.getName()));

			return dest;
		}
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * 解析关联表名称。
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	protected String resolveJoinTableName(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, RelationType relationType)
	{
		String tableName = null;

		TableName tableNameFeature = property.getFeature(TableName.class);

		if (tableNameFeature != null)
			tableName = tableNameFeature.getValue(propertyConcreteModelIdx);

		if (tableName == null)
		{
			if (MU.isAbstractedProperty(property))
				tableName = model.getName() + "_" + property.getName() + "_" + propertyConcreteModel.getName();
			else
				tableName = model.getName() + "_" + property.getName();
		}

		return tableName;
	}

	/**
	 * 解析属性表名称。
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	protected String resolvePropertyTableName(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, RelationType relationType)
	{
		String tableName = null;

		TableName tableNameFeature = property.getFeature(TableName.class);

		if (tableNameFeature != null)
			tableName = tableNameFeature.getValue(propertyConcreteModelIdx);

		if (tableName == null)
		{
			TableName ptableNameFeature = propertyConcreteModel.getFeature(TableName.class);

			if (ptableNameFeature != null)
				tableName = ptableNameFeature.getValue();
		}

		if (tableName == null)
		{
			if (MU.isAbstractedProperty(property))
				tableName = model.getName() + "_" + property.getName() + "_" + propertyConcreteModel.getName();
			else
				tableName = model.getName() + "_" + property.getName();
		}

		return tableName;
	}

	/**
	 * 解析属性字段名。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected String resolvePropertyColumnName(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		String strColumnName = null;

		ColumnName columnName = property.getFeature(ColumnName.class);

		if (columnName == null)
		{
			if (property.isAbstracted())
				strColumnName = property.getName() + "_" + propertyConcreteModel.getName();
			else
				strColumnName = property.getName();
		}
		else
		{
			strColumnName = columnName.getValue(propertyConcreteModelIdx);
		}

		return strColumnName;
	}

	/**
	 * 解析{@linkplain ModelKeyPropertyName}所表示的{@linkplain Property}。
	 * <p>
	 * 如果无{@linkplain ModelKeyPropertyName}特性，此方法将返回默认实现。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected Property[] resolveModelKeyProperties(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		Property[] keyProperties = null;

		String[] propertyNames = null;

		ModelKeyPropertyName modelKeyPropertyName = property.getFeature(ModelKeyPropertyName.class);

		if (modelKeyPropertyName != null)
			propertyNames = modelKeyPropertyName.getValue(propertyConcreteModelIdx);

		if (propertyNames != null)
			keyProperties = MU.getProperties(model, propertyNames);
		else
			keyProperties = model.getIdProperties();

		checkKeyProperty(model, keyProperties);

		return keyProperties;
	}

	/**
	 * 解析{@linkplain ModelKeyColumnName}所表示的列名称。
	 * <p>
	 * 如果无{@linkplain ModelKeyColumnName}特性，此方法将返回默认实现。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param modelKeyProperties
	 * @return
	 */
	protected String[] resolveModelKeyColumnNames(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, Property[] modelKeyProperties)
	{
		String[] mkeyColumnNames = null;

		ModelKeyColumnName modelKeyColumnName = property.getFeature(ModelKeyColumnName.class);

		if (modelKeyColumnName != null)
			mkeyColumnNames = modelKeyColumnName.getValue(propertyConcreteModelIdx);

		if (mkeyColumnNames == null)
		{
			mkeyColumnNames = doResolveKeyColumnNames(model, modelKeyProperties);

			for (int i = 0; i < mkeyColumnNames.length; i++)
				mkeyColumnNames[i] = model.getName() + "_" + mkeyColumnNames[i];
		}

		return mkeyColumnNames;
	}

	/**
	 * 解析{@linkplain PropertyKeyPropertyName}所表示的{@linkplain Property}。
	 * <p>
	 * 如果无{@linkplain PropertyKeyPropertyName}特性，此方法将返回默认实现。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected Property[] resolvePropertyKeyProperties(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		Property[] keyProperties = null;

		String[] propertyNames = null;

		PropertyKeyPropertyName propertyKeyPropertyName = property.getFeature(PropertyKeyPropertyName.class);

		if (propertyKeyPropertyName != null)
			propertyNames = propertyKeyPropertyName.getValue(propertyConcreteModelIdx);

		if (propertyNames != null)
			keyProperties = MU.getProperties(propertyConcreteModel, propertyNames);
		else
			keyProperties = propertyConcreteModel.getIdProperties();

		checkKeyProperty(propertyConcreteModel, keyProperties);

		return keyProperties;
	}

	/**
	 * 解析{@linkplain PropertyKeyColumnName}所表示的列名称。
	 * <p>
	 * 如果无{@linkplain PropertyKeyColumnName}特性，此方法将返回默认实现。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param propertyKeyProperties
	 * @return
	 */
	protected String[] resolvePropertyKeyColumnNames(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, Property[] propertyKeyProperties)
	{
		String[] pknames = null;

		PropertyKeyColumnName propertyKeyColumnName = property.getFeature(PropertyKeyColumnName.class);

		if (propertyKeyColumnName != null)
			pknames = propertyKeyColumnName.getValue(propertyConcreteModelIdx);

		if (pknames == null)
		{
			pknames = doResolveKeyColumnNames(propertyConcreteModel, propertyKeyProperties);

			for (int i = 0; i < pknames.length; i++)
				pknames[i] = property.getName() + "_" + pknames[i];
		}

		return pknames;
	}

	/**
	 * 获取{@linkplain ModelConcreteColumnName}所表示的列名称。
	 * <p>
	 * 如果无{@linkplain ModelConcreteColumnName}特性，此方法将返回默认实现。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected String resolveModelConcreteColumnName(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		String cname = null;

		ModelConcreteColumnName modelConcreteColumnName = property.getFeature(ModelConcreteColumnName.class);

		if (modelConcreteColumnName != null)
			cname = modelConcreteColumnName.getValue(propertyConcreteModelIdx);

		return cname;
	}

	/**
	 * 获取{@linkplain PropertyConcreteColumnName}所表示的列名称。
	 * <p>
	 * 如果无{@linkplain PropertyConcreteColumnName}特性，此方法将返回默认实现。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected String resolvePropertyConcreteColumnName(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		String cname = null;

		PropertyConcreteColumnName propertyConcreteColumnName = property.getFeature(PropertyConcreteColumnName.class);

		if (propertyConcreteColumnName != null)
			cname = propertyConcreteColumnName.getValue(propertyConcreteModelIdx);

		return cname;
	}

	/**
	 * 解析{@linkplain ModelConcreteColumnValue}。
	 * <p>
	 * 如果无{@linkplain ModelConcreteColumnValue}，它将返回默认值。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected Object resolveModelConcreteColumnValue(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		Object value = null;

		ModelConcreteColumnValue concreteColumnValues = property.getFeature(ModelConcreteColumnValue.class);

		if (concreteColumnValues != null)
			value = concreteColumnValues.getValue(propertyConcreteModelIdx);

		if (value == null)
			value = model.getName();

		return value;
	}

	/**
	 * 解析{@linkplain PropertyConcreteColumnValue}。
	 * <p>
	 * 如果无{@linkplain PropertyConcreteColumnValue}，它将返回默认值。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected Object resolvePropertyConcreteColumnValue(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		Object value = null;

		PropertyConcreteColumnValue concreteColumnValues = property.getFeature(PropertyConcreteColumnValue.class);

		if (concreteColumnValues != null)
			value = concreteColumnValues.getValue(propertyConcreteModelIdx);

		if (value == null)
		{
			value = propertyConcreteModel.getName();
		}

		return value;
	}

	/**
	 * 解析{@linkplain ModelOrderColumnName}所表示的列名称。
	 * <p>
	 * 如果没有{@linkplain ModelOrderColumnName}，此方法将返回{@code null}。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected String resolveModelOrderColumnName(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		String oname = null;

		ModelOrderColumnName morderColumnNames = property.getFeature(ModelOrderColumnName.class);

		if (morderColumnNames != null)
			oname = morderColumnNames.getValue(propertyConcreteModelIdx);

		return oname;
	}

	/**
	 * 解析{@linkplain PropertyOrderColumnName}所表示的列名称。
	 * <p>
	 * 如果没有{@linkplain PropertyOrderColumnName}，此方法将返回{@code null}。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @return
	 */
	protected String resolvePropertyOrderColumnName(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx)
	{
		String oname = null;

		PropertyOrderColumnName morderColumnNames = property.getFeature(PropertyOrderColumnName.class);

		if (morderColumnNames != null)
			oname = morderColumnNames.getValue(propertyConcreteModelIdx);

		return oname;
	}

	/**
	 * 解析{@linkplain PropertyKeyUpdateRule}。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param mappedBy
	 * @return
	 */
	protected KeyRule resolvePropertyKeyUpdateRule(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, String mappedBy)
	{
		KeyRule keyRule = null;

		PropertyKeyUpdateRule propertyKeyUpdateRule = property.getFeature(PropertyKeyUpdateRule.class);

		if (propertyKeyUpdateRule != null)
			keyRule = propertyKeyUpdateRule.getValue(propertyConcreteModelIdx);

		if (keyRule == null && !isNullOrEmpty(mappedBy))
		{
			Property mappedByProperty = propertyConcreteModel.getProperty(mappedBy);

			int mappedbyIndex = MU.getPropertyModelIndex(mappedByProperty, model);

			ModelKeyUpdateRule modelKeyUpdateRule = mappedByProperty.getFeature(ModelKeyUpdateRule.class);

			if (modelKeyUpdateRule != null)
				keyRule = modelKeyUpdateRule.getValue(mappedbyIndex);
		}

		return keyRule;
	}

	/**
	 * 解析{@linkplain PropertyKeyDeleteRule}。
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @param propertyConcreteModelIdx
	 * @param mappedBy
	 * @return
	 */
	protected KeyRule resolvePropertyKeyDeleteRule(Model model, Property property, Model propertyConcreteModel,
			int propertyConcreteModelIdx, String mappedBy)
	{
		KeyRule keyRule = null;

		PropertyKeyDeleteRule propertyKeyDeleteRule = property.getFeature(PropertyKeyDeleteRule.class);

		if (propertyKeyDeleteRule != null)
			keyRule = propertyKeyDeleteRule.getValue(propertyConcreteModelIdx);

		if (keyRule == null && !isNullOrEmpty(mappedBy))
		{
			Property mappedByProperty = propertyConcreteModel.getProperty(mappedBy);

			int mappedbyIndex = MU.getPropertyModelIndex(mappedByProperty, model);

			ModelKeyDeleteRule modelKeyDeleteRule = mappedByProperty.getFeature(ModelKeyDeleteRule.class);

			if (modelKeyDeleteRule != null)
				keyRule = modelKeyDeleteRule.getValue(mappedbyIndex);
		}

		return keyRule;
	}

	/**
	 * 解析可作为Key键（主键或者外键）{@linkplain Property}的字段名数组。
	 * 
	 * @param model
	 * @param keyProperties
	 * @return
	 */
	protected String[] doResolveKeyColumnNames(Model model, Property[] keyProperties)
	{
		List<String> keyColumnNames = new ArrayList<String>();

		for (int i = 0; i < keyProperties.length; i++)
		{
			String[] myColumnNames = doResolveKeyColumnNames(model, keyProperties[i]);

			keyColumnNames.addAll(Arrays.asList(myColumnNames));
		}

		return keyColumnNames.toArray(new String[keyColumnNames.size()]);
	}

	/**
	 * 解析可作为Key键（主键或者外键）{@linkplain Property}的字段名数组。
	 * <p>
	 * {@code keyProperty}必须符合{@linkplain #checkKeyProperty(Model, Property...)}校验。
	 * </p>
	 * 
	 * @param model
	 * @param keyProperty
	 * @return
	 */
	protected String[] doResolveKeyColumnNames(Model model, Property keyProperty)
	{
		Model propertyModel = keyProperty.getModel();

		String[] keyColumnNames = null;

		if (MU.isPrimitiveModel(propertyModel))
		{
			String columnName = resolvePropertyColumnName(model, keyProperty, propertyModel, 0);
			keyColumnNames = new String[] { columnName };
		}
		else
		{
			Property[] pkeyProperties = resolvePropertyKeyProperties(model, keyProperty, propertyModel, 0);

			keyColumnNames = doResolveKeyColumnNames(propertyModel, pkeyProperties);
		}

		return keyColumnNames;
	}

	/**
	 * 检查给定{@linkplain Property}是否能作为KEY（主键或者外键）键。
	 * <p>
	 * 可作为Key键的属性必须满足如下条件：
	 * </p>
	 * <ul>
	 * <li>属性必须是{@linkplain ModelTableMapper}；</li>
	 * <li>属性必须是具体属性（{@linkplain Property#isAbstracted()}为{@code false}）且无{@linkplain ModelConcreteColumnName}和{@linkplain PropertyConcreteColumnName}；
	 * <p>
	 * </p>
	 * </li>
	 * </ul>
	 * 
	 * @param model
	 * @param properties
	 */
	protected void checkKeyProperty(Model model, Property... properties)
	{
		for (Property property : properties)
		{
			if (property.isAbstracted())
				throw new UnsupportedModelCharacterException(
						"Key property [" + property.getName() + "] of abstracted is not supported");

			if (property.hasFeature(ModelConcreteColumnName.class))
				throw new UnsupportedModelCharacterException("Key property [" + property.getName() + "] of ["
						+ ModelConcreteColumnName.class.getSimpleName() + "] is not supported");

			if (property.hasFeature(PropertyConcreteColumnName.class))
				throw new UnsupportedModelCharacterException("Key property [" + property.getName() + "] of ["
						+ PropertyConcreteColumnName.class.getSimpleName() + "] is not supported");

			RelationType relationType = resolveRelationType(model, property);

			if (!RelationType.ONE_TO_ONE.equals(relationType) && !RelationType.MANY_TO_ONE.equals(relationType))
				throw new IllegalArgumentException("Key property [" + property.getName() + "] must only be ["
						+ OneToOne.class.getSimpleName() + "] or [" + ManyToOne.class.getSimpleName() + "]");

			Model propertyModel = property.getModel();

			PointType pointType = resolveRelationPointType(model, property, propertyModel, 0, relationType);

			if (!PointType.MODEL.equals(pointType))
				throw new IllegalArgumentException("Key property [" + property.getName() + "] must be ["
						+ ModelTableMapper.class.getSimpleName() + "]");
		}
	}

	/**
	 * 判断字符串是否为{@code null}或者空。
	 * 
	 * @param s
	 * @return
	 */
	protected boolean isNullOrEmpty(String s)
	{
		return s == null || s.isEmpty();
	}

	protected static class RelationMapperImpl implements RelationMapper
	{
		private Mapper[] mappers;

		private RelationType relationType;

		public RelationMapperImpl()
		{
			super();
		}

		public RelationMapperImpl(Mapper[] mappers, RelationType relationType)
		{
			super();
			this.mappers = mappers;
			this.relationType = relationType;
		}

		@Override
		public Mapper[] getMappers()
		{
			return mappers;
		}

		public void setMappers(Mapper[] mappers)
		{
			this.mappers = mappers;
		}

		public RelationType getRelationType()
		{
			return relationType;
		}

		public void setRelationType(RelationType relationType)
		{
			this.relationType = relationType;
		}

		@Override
		public boolean isOneToOne()
		{
			return RelationType.ONE_TO_ONE.equals(this.relationType);
		}

		@Override
		public boolean isOneToMany()
		{
			return RelationType.ONE_TO_MANY.equals(this.relationType);
		}

		@Override
		public boolean isManyToOne()
		{
			return RelationType.MANY_TO_ONE.equals(this.relationType);
		}

		@Override
		public boolean isManyToMany()
		{
			return RelationType.MANY_TO_MANY.equals(this.relationType);
		}
	}

	protected static abstract class AbstractMapper implements Mapper
	{
		private String mappedByTarget;

		private String mappedBySource;

		private String modelConcreteColumnName;

		private Object modelConcreteColumnValue;

		private String propertyConcreteColumnName;

		private Object propertyConcreteColumnValue;

		private KeyRule propertyKeyUpdateRule;

		private KeyRule propertyKeyDeleteRule;

		public AbstractMapper()
		{
			super();
		}

		@Override
		public String getMappedByTarget()
		{
			return mappedByTarget;
		}

		public void setMappedByTarget(String mappedByTarget)
		{
			this.mappedByTarget = mappedByTarget;
		}

		@Override
		public String getMappedBySource()
		{
			return mappedBySource;
		}

		public void setMappedBySource(String mappedBySource)
		{
			this.mappedBySource = mappedBySource;
		}

		@Override
		public String getModelConcreteColumnName()
		{
			return modelConcreteColumnName;
		}

		public void setModelConcreteColumnName(String modelConcreteColumnName)
		{
			this.modelConcreteColumnName = modelConcreteColumnName;
		}

		@Override
		public Object getModelConcreteColumnValue()
		{
			return modelConcreteColumnValue;
		}

		public void setModelConcreteColumnValue(Object modelConcreteColumnValue)
		{
			this.modelConcreteColumnValue = modelConcreteColumnValue;
		}

		@Override
		public String getPropertyConcreteColumnName()
		{
			return propertyConcreteColumnName;
		}

		public void setPropertyConcreteColumnName(String propertyConcreteColumnName)
		{
			this.propertyConcreteColumnName = propertyConcreteColumnName;
		}

		@Override
		public Object getPropertyConcreteColumnValue()
		{
			return propertyConcreteColumnValue;
		}

		public void setPropertyConcreteColumnValue(Object propertyConcreteColumnValue)
		{
			this.propertyConcreteColumnValue = propertyConcreteColumnValue;
		}

		@Override
		public KeyRule getPropertyKeyUpdateRule()
		{
			return propertyKeyUpdateRule;
		}

		public void setPropertyKeyUpdateRule(KeyRule propertyKeyUpdateRule)
		{
			this.propertyKeyUpdateRule = propertyKeyUpdateRule;
		}

		@Override
		public KeyRule getPropertyKeyDeleteRule()
		{
			return propertyKeyDeleteRule;
		}

		public void setPropertyKeyDeleteRule(KeyRule propertyKeyDeleteRule)
		{
			this.propertyKeyDeleteRule = propertyKeyDeleteRule;
		}

		@Override
		public boolean isMappedBySource()
		{
			return (this.mappedByTarget != null);
		}

		@Override
		public boolean isMappedByTarget()
		{
			return (this.mappedBySource != null);
		}

		@Override
		public boolean hasModelConcreteColumn()
		{
			return (this.modelConcreteColumnName != null);
		}

		@Override
		public boolean hasPropertyConcreteColumn()
		{
			return (this.propertyConcreteColumnName != null);
		}
	}

	protected static class ModelTableMapperImpl extends AbstractMapper implements ModelTableMapper
	{
		public static final int PROPERTY_TYPE_ENTITY = 0;

		public static final int PROPERTY_TYPE_PRIMITIVE_VALUE = 2;

		private boolean primitivePropertyMapper;

		private String primitiveColumnName;

		private String[] propertyKeyPropertyNames;

		private String[] propertyKeyColumnNames;

		private String modelOrderColumnName;

		public ModelTableMapperImpl()
		{
			super();
		}

		@Override
		public boolean isPrimitivePropertyMapper()
		{
			return primitivePropertyMapper;
		}

		public void setPrimitivePropertyMapper(boolean primitivePropertyMapper)
		{
			this.primitivePropertyMapper = primitivePropertyMapper;
		}

		@Override
		public String getPrimitiveColumnName()
		{
			return primitiveColumnName;
		}

		public void setPrimitiveColumnName(String primitiveColumnName)
		{
			this.primitiveColumnName = primitiveColumnName;
		}

		@Override
		public String[] getPropertyKeyPropertyNames()
		{
			return propertyKeyPropertyNames;
		}

		public void setPropertyKeyPropertyNames(String[] propertyKeyPropertyNames)
		{
			this.propertyKeyPropertyNames = propertyKeyPropertyNames;
		}

		@Override
		public String[] getPropertyKeyColumnNames()
		{
			return propertyKeyColumnNames;
		}

		public void setPropertyKeyColumnNames(String[] propertyKeyColumnNames)
		{
			this.propertyKeyColumnNames = propertyKeyColumnNames;
		}

		@Override
		public String getModelOrderColumnName()
		{
			return modelOrderColumnName;
		}

		public void setModelOrderColumnName(String modelOrderColumnName)
		{
			this.modelOrderColumnName = modelOrderColumnName;
		}

		@Override
		public boolean hasModelOrderColumn()
		{
			return (this.modelOrderColumnName != null);
		}
	}

	protected static class PropertyTableMapperImpl extends AbstractMapper implements PropertyTableMapper
	{
		private boolean primitivePropertyMapper;

		private String primitiveTableName;

		private String primitiveColumnName;

		private String[] modelKeyPropertyNames;

		private String[] modelKeyColumnNames;

		private String propertyOrderColumnName;

		public PropertyTableMapperImpl()
		{
			super();
		}

		@Override
		public boolean isPrimitivePropertyMapper()
		{
			return primitivePropertyMapper;
		}

		public void setPrimitivePropertyMapper(boolean primitivePropertyMapper)
		{
			this.primitivePropertyMapper = primitivePropertyMapper;
		}

		@Override
		public String getPrimitiveTableName()
		{
			return primitiveTableName;
		}

		public void setPrimitiveTableName(String primitiveTableName)
		{
			this.primitiveTableName = primitiveTableName;
		}

		@Override
		public String getPrimitiveColumnName()
		{
			return primitiveColumnName;
		}

		public void setPrimitiveColumnName(String primitiveColumnName)
		{
			this.primitiveColumnName = primitiveColumnName;
		}

		@Override
		public String[] getModelKeyPropertyNames()
		{
			return modelKeyPropertyNames;
		}

		public void setModelKeyPropertyNames(String[] modelKeyPropertyNames)
		{
			this.modelKeyPropertyNames = modelKeyPropertyNames;
		}

		@Override
		public String[] getModelKeyColumnNames()
		{
			return modelKeyColumnNames;
		}

		public void setModelKeyColumnNames(String[] modelKeyColumnNames)
		{
			this.modelKeyColumnNames = modelKeyColumnNames;
		}

		@Override
		public String getPropertyOrderColumnName()
		{
			return propertyOrderColumnName;
		}

		public void setPropertyOrderColumnName(String propertyOrderColumnName)
		{
			this.propertyOrderColumnName = propertyOrderColumnName;
		}

		@Override
		public boolean hasPropertyOrderColumn()
		{
			return (this.propertyOrderColumnName != null);
		}
	}

	protected static class JoinTableMapperImpl extends AbstractMapper implements JoinTableMapper
	{
		private String joinTableName;

		private String[] modelKeyPropertyNames;

		private String[] modelKeyColumnNames;

		private String modelOrderColumnName;

		private String propertyOrderColumnName;

		private String[] propertyKeyPropertyNames;

		private String[] propertyKeyColumnNames;

		public JoinTableMapperImpl()
		{
			super();
		}

		@Override
		public String getJoinTableName()
		{
			return joinTableName;
		}

		public void setJoinTableName(String joinTableName)
		{
			this.joinTableName = joinTableName;
		}

		@Override
		public String[] getModelKeyPropertyNames()
		{
			return modelKeyPropertyNames;
		}

		public void setModelKeyPropertyNames(String[] modelKeyPropertyNames)
		{
			this.modelKeyPropertyNames = modelKeyPropertyNames;
		}

		@Override
		public String[] getModelKeyColumnNames()
		{
			return modelKeyColumnNames;
		}

		public void setModelKeyColumnNames(String[] modelKeyColumnNames)
		{
			this.modelKeyColumnNames = modelKeyColumnNames;
		}

		@Override
		public String getPropertyOrderColumnName()
		{
			return propertyOrderColumnName;
		}

		public void setPropertyOrderColumnName(String propertyOrderColumnName)
		{
			this.propertyOrderColumnName = propertyOrderColumnName;
		}

		@Override
		public String[] getPropertyKeyPropertyNames()
		{
			return propertyKeyPropertyNames;
		}

		public void setPropertyKeyPropertyNames(String[] propertyKeyPropertyNames)
		{
			this.propertyKeyPropertyNames = propertyKeyPropertyNames;
		}

		@Override
		public String[] getPropertyKeyColumnNames()
		{
			return propertyKeyColumnNames;
		}

		public void setPropertyKeyColumnNames(String[] propertyKeyColumnNames)
		{
			this.propertyKeyColumnNames = propertyKeyColumnNames;
		}

		@Override
		public String getModelOrderColumnName()
		{
			return modelOrderColumnName;
		}

		public void setModelOrderColumnName(String modelOrderColumnName)
		{
			this.modelOrderColumnName = modelOrderColumnName;
		}

		@Override
		public boolean hasModelOrderColumn()
		{
			return (this.modelOrderColumnName != null);
		}

		@Override
		public boolean hasPropertyOrderColumn()
		{
			return (this.propertyOrderColumnName != null);
		}
	}
}
