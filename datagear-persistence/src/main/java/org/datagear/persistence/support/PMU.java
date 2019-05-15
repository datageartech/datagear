/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.util.Collection;
import java.util.List;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.collection.SizeOnlyCollection;
import org.datagear.persistence.features.KeyRule;
import org.datagear.persistence.features.KeyRule.RuleType;
import org.datagear.persistence.features.MappedBy;
import org.datagear.persistence.mapper.Mapper;
import org.datagear.persistence.mapper.MapperUtil;
import org.datagear.persistence.mapper.ModelTableMapper;

/**
 * 持久化模型公用方法集。
 * 
 * @author datagear@163.com
 *
 */
public class PMU
{
	/**
	 * 判断给定{@linkplain Property}是否是私有的。
	 * <p>
	 * 如果属性值没有独立的生命周期，那么它就是私有的。
	 * </p>
	 * <p>
	 * 属性值只有私有的和共享的两种可能。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	public static boolean isPrivate(Model model, Property property)
	{
		Mapper mapper = property.getFeature(Mapper.class);

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
	 * 判断给定{@linkplain Property}是否是共享的。
	 * <p>
	 * 如果属性值有独立的生命周期，那么它就是共享的。
	 * </p>
	 * <p>
	 * 属性值只有私有的和共享的两种可能。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	public static boolean isShared(Model model, Property property)
	{
		return !isPrivate(model, property);
	}

	/**
	 * 设置属性值。
	 * <p>
	 * 此方法会处理{@linkplain MappedBy}双向关联，反向赋值。
	 * </p>
	 * <p>
	 * 注意：此方法不会处理反向集合属性，因为此时obj仅是反向集合中的单个元素
	 * </p>
	 * 
	 * @param model
	 * @param obj
	 * @param property
	 * @param propertyValue
	 */
	public static void setPropertyValue(Model model, Object obj, Property property, Object propertyValue)
	{
		Model pmodel = MU.getModel(property);

		if (propertyValue == null)
		{
			if (pmodel.getType().isPrimitive())
				;
			else
				property.set(obj, null);
		}
		else if (MU.isPrimitiveModel(pmodel))
		{
			property.set(obj, propertyValue);
		}
		else
		{
			property.set(obj, propertyValue);

			setPropertyValueMappedByIf(model, obj, property, propertyValue);
		}
	}

	/**
	 * 设置属性值的{@linkplain MappedBy}反向属性值。
	 * 
	 * @param model
	 * @param obj
	 * @param property
	 * @param propertyValueOrElement
	 */
	public static boolean setPropertyValueMappedByIf(Model model, Object obj, Property property,
			Object propertyValueOrElement)
	{
		if (propertyValueOrElement == null)
			return false;

		Model pmodel = MU.getModel(property);

		if (MU.isPrimitiveModel(pmodel))
			return false;

		Mapper mapper = property.getFeature(Mapper.class);

		String mappedTarget = null;
		if (mapper.isMappedBySource())
			mappedTarget = mapper.getMappedByTarget();
		else if (mapper.isMappedByTarget())
			mappedTarget = mapper.getMappedBySource();

		// 反向设置属性值
		if (mappedTarget != null)
		{
			Property mappedProperty = MU.getProperty(pmodel, mappedTarget);

			// 不处理反向集合属性，因为此时obj仅是反向集合中的单个元素
			if (MU.isMultipleProperty(mappedProperty))
				return false;

			if (propertyValueOrElement instanceof Collection<?>)
			{
				@SuppressWarnings("unchecked")
				Collection<Object> collection = (Collection<Object>) propertyValueOrElement;

				for (Object ele : collection)
					mappedProperty.set(ele, obj);
			}
			else if (propertyValueOrElement instanceof Object[])
			{
				Object[] array = (Object[]) propertyValueOrElement;

				for (Object ele : array)
					mappedProperty.set(ele, obj);
			}
			else
				mappedProperty.set(propertyValueOrElement, obj);

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
	public static Object[] toArray(Object obj)
	{
		if (obj == null)
			return null;

		if (obj instanceof Object[])
			return (Object[]) obj;

		Class<?> objType = obj.getClass();

		if (SizeOnlyCollection.class.isAssignableFrom(objType))
		{
			return new Object[0];
		}
		else if (List.class.isAssignableFrom(objType))
		{
			return ((List<Object>) obj).toArray();
		}
		else if (Collection.class.isAssignableFrom(objType))
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
}
