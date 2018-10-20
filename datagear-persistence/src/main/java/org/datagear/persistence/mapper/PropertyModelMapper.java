/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.PropertyModel;

/**
 * 属性模型映射信息。
 * <p>
 * 此类描述特定属性模型（{@linkplain Property#getModel()}中的某个）的映射信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class PropertyModelMapper<T extends Mapper> extends PropertyModel
{
	/** 属性的关系映射 */
	private RelationMapper relationMapper;

	/** 映射信息 */
	private T mapper;

	public PropertyModelMapper()
	{
		super();
	}

	public PropertyModelMapper(Property property, int index, Model model, RelationMapper relationMapper, T mapper)
	{
		super(property, index, model);
		this.relationMapper = relationMapper;
		this.mapper = mapper;
	}

	public RelationMapper getRelationMapper()
	{
		return relationMapper;
	}

	public void setRelationMapper(RelationMapper relationMapper)
	{
		this.relationMapper = relationMapper;
	}

	public T getMapper()
	{
		return mapper;
	}

	public void setMapper(T mapper)
	{
		this.mapper = mapper;
	}

	/**
	 * 是否是{@linkplain ModelTableMapper}。
	 * 
	 * @return
	 */
	public boolean isModelTableMapperInfo()
	{
		return MapperUtil.isModelTableMapper(this.mapper);
	}

	/**
	 * 强转为{@linkplain ModelTableMapper}。
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PropertyModelMapper<ModelTableMapper> castModelTableMapperInfo()
	{
		return (PropertyModelMapper<ModelTableMapper>) this;
	}

	/**
	 * 是否是{@linkplain PropertyTableMapper}。
	 * 
	 * @return
	 */
	public boolean isPropertyTableMapperInfo()
	{
		return MapperUtil.isPropertyTableMapper(this.mapper);
	}

	/**
	 * 强转为{@linkplain PropertyTableMapper}。
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PropertyModelMapper<PropertyTableMapper> castPropertyTableMapperInfo()
	{
		return (PropertyModelMapper<PropertyTableMapper>) this;
	}

	/**
	 * 是否是{@linkplain JoinTableMapper}。
	 * 
	 * @return
	 */
	public boolean isJoinTableMapperInfo()
	{
		return MapperUtil.isJoinTableMapper(this.mapper);
	}

	/**
	 * 强转为{@linkplain JoinTableMapper}。
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PropertyModelMapper<JoinTableMapper> castJoinTableMapperInfo()
	{
		return (PropertyModelMapper<JoinTableMapper>) this;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [property=" + getProperty() + ", index=" + getIndex() + ", model="
				+ getModel() + ", mapper=" + mapper + "]";
	}

	/**
	 * 构建{@linkplain PropertyModelMapper}。
	 * 
	 * @param property
	 * @param relationMapper
	 * @param propertyValue
	 * @return
	 */
	public static PropertyModelMapper<?> valueOf(Property property, RelationMapper relationMapper, Object propertyValue)
	{
		PropertyModel pmm = PropertyModel.valueOf(property, propertyValue);

		Mapper[] mappers = relationMapper.getMappers();

		return new PropertyModelMapper<Mapper>(property, pmm.getIndex(), pmm.getModel(), relationMapper,
				mappers[pmm.getIndex()]);
	}

	/**
	 * 构建{@linkplain PropertyModelMapper}。
	 * 
	 * @param property
	 * @param relationMapper
	 * @param propertyModel
	 * @return
	 */
	public static PropertyModelMapper<?> valueOf(Property property, RelationMapper relationMapper, Model propertyModel)
	{
		PropertyModel pmm = PropertyModel.valueOf(property, propertyModel);

		Mapper[] mappers = relationMapper.getMappers();

		return new PropertyModelMapper<Mapper>(property, pmm.getIndex(), propertyModel, relationMapper,
				mappers[pmm.getIndex()]);
	}

	/**
	 * 构建{@linkplain PropertyModelMapper}。
	 * 
	 * @param property
	 * @param relationMapper
	 * @param propertyModelIndex
	 * @return
	 */
	public static PropertyModelMapper<?> valueOf(Property property, RelationMapper relationMapper,
			int propertyModelIndex)
	{
		PropertyModel pmm = PropertyModel.valueOf(property, propertyModelIndex);

		Mapper[] mappers = relationMapper.getMappers();

		return new PropertyModelMapper<Mapper>(property, pmm.getIndex(), pmm.getModel(), relationMapper,
				mappers[pmm.getIndex()]);
	}

	/**
	 * 构建{@linkplain PropertyModelMapper}。
	 * 
	 * @param property
	 * @param relationMapper
	 * @param propertyModel
	 * @return
	 */
	public static PropertyModelMapper<?> valueOf(Property property, RelationMapper relationMapper,
			PropertyModel propertyModel)
	{
		Mapper[] mappers = relationMapper.getMappers();

		return new PropertyModelMapper<Mapper>(property, propertyModel.getIndex(), propertyModel.getModel(),
				relationMapper, mappers[propertyModel.getIndex()]);
	}

	/**
	 * 构建{@linkplain PropertyModelMapper}数组。
	 * 
	 * @param property
	 * @param relationMapper
	 * @return
	 */
	public static PropertyModelMapper<?>[] valueOf(Property property, RelationMapper relationMapper)
	{
		Model[] pmodels = property.getModels();
		Mapper[] mappers = relationMapper.getMappers();

		PropertyModelMapper<?>[] propertyModelMappers = new PropertyModelMapper[pmodels.length];

		for (int i = 0; i < pmodels.length; i++)
			propertyModelMappers[i] = new PropertyModelMapper<Mapper>(property, i, pmodels[i], relationMapper,
					mappers[i]);

		return propertyModelMappers;
	}
}
