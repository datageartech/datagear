/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.features.KeyRule;
import org.datagear.persistence.features.KeyRule.RuleType;
import org.datagear.persistence.mapper.Mapper;
import org.datagear.persistence.mapper.MapperUtil;
import org.datagear.persistence.mapper.ModelTableMapper;
import org.datagear.persistence.mapper.RelationMapper;

/**
 * 持久化模型公用方法集。
 * 
 * @author datagear@163.com
 *
 */
public class PMU
{
	/**
	 * 判断给定{@linkplain Property}的具体{@linkplain Model}是否是私有的。
	 * <p>
	 * 如果属性值没有独立的生命周期，那么它就是私有的。
	 * </p>
	 * <p>
	 * 属性值只有私有的和共享的两种可能。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @return
	 */
	public static boolean isPrivate(Model model, Property property, Model propertyConcreteModel)
	{
		RelationMapper relationMapper = property.getFeature(RelationMapper.class);
		int myIndex = MU.getPropertyModelIndex(property, propertyConcreteModel);
		Mapper mapper = relationMapper.getMappers()[myIndex];

		KeyRule keyRule = mapper.getPropertyKeyDeleteRule();

		if (keyRule != null && RuleType.CASCADE.equals(keyRule.getRuleType()))
			return true;

		if (MapperUtil.isModelTableMapper(mapper))
		{
			ModelTableMapper modelTableMapper = MapperUtil.castModelTableMapper(mapper);

			return modelTableMapper.isPrimitivePropertyMapper();
		}
		else if (MapperUtil.isPropertyTableMapper(mapper))
		{
			return true;
		}
		else if (MapperUtil.isJoinTableMapper(mapper))
		{
			return false;
		}
		else
			return false;
	}

	/**
	 * 判断给定{@linkplain Property}的具体{@linkplain Model}是否是共享的。
	 * <p>
	 * 如果属性值有独立的生命周期，那么它就是共享的。
	 * </p>
	 * <p>
	 * 属性值只有私有的和共享的两种可能。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param propertyConcreteModel
	 * @return
	 */
	public static boolean isShared(Model model, Property property, Model propertyConcreteModel)
	{
		return !isPrivate(model, property, propertyConcreteModel);
	}
}
