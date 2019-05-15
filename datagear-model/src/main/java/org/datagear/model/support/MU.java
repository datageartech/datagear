/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import org.datagear.model.InstanceCreationException;
import org.datagear.model.Model;
import org.datagear.model.Property;

/**
 * 模型工具集类。
 * 
 * @author datagear@163.com
 *
 */
public class MU
{
	/**
	 * 判断给定{@linkplain Model 模型}是否是<i>实体模型</i>。
	 * <p>
	 * <i>实体模型</i>的{@linkplain Model#hasIdProperty()}为{@code true}。
	 * </p>
	 * 
	 * @param model
	 * @return
	 */
	public static boolean isEntityModel(Model model)
	{
		return model.hasIdProperty();
	}

	/**
	 * 判断给定{@linkplain Model 模型}是否是<i>值模型</i>。
	 * <p>
	 * <i>值模型</i>的{@linkplain Model#hasIdProperty()}为{@code false}。
	 * </p>
	 * 
	 * @param model
	 * @return
	 */
	public static boolean isValueModel(Model model)
	{
		return !model.hasIdProperty();
	}

	/**
	 * 判断给定{@linkplain Model 模型}是否是<i>复合模型</i>。
	 * <p>
	 * <i>复合模型</i>的{@linkplain Model#hasProperty()}为{@code true}。
	 * </p>
	 * 
	 * @param model
	 * @return
	 */
	public static boolean isCompositeModel(Model model)
	{
		return model.hasProperty();
	}

	/**
	 * 判断给定{@linkplain Model 模型}是否是<i>基本模型</i>。
	 * <p>
	 * <i>基本模型</i>的{@linkplain Model#hasProperty()}为{@code false}。
	 * </p>
	 * 
	 * @param model
	 * @return
	 */
	public static boolean isPrimitiveModel(Model model)
	{
		return !model.hasProperty();
	}

	/**
	 * 获取属性模型。
	 * 
	 * @param property
	 * @return
	 */
	public static Model getModel(Property property)
	{
		return property.getModel();
	}

	/**
	 * 判断给定{@linkplain Property 属性}是否是单元属性。
	 * <p>
	 * 单元属性的{@linkplain Property#isArray()}为{@code false}且
	 * {@linkplain Property#isCollection()}为{@code false}。
	 * </p>
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isSingleProperty(Property property)
	{
		return !property.isArray() && !property.isCollection();
	}

	/**
	 * 判断给定{@linkplain Property 属性}是否是多元属性。
	 * <p>
	 * 多元属性的{@linkplain Property#isArray()}为{@code true}或者
	 * {@linkplain Property#isCollection()}为{@code true}。
	 * </p>
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isMultipleProperty(Property property)
	{
		return property.isArray() || property.isCollection();
	}

	/**
	 * 判断给定{@linkplain Property}是否是基本属性。
	 * <p>
	 * 基本属性的{@linkplain Property#getModel()}是基本模型。
	 * </p>
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isPrimitiveProperty(Property property)
	{
		return isPrimitiveModel(property.getModel());
	}

	/**
	 * 判断给定{@linkplain Property}是否是复合属性。
	 * <p>
	 * 复合属性的{@linkplain Property#getModel()}是复合模型。
	 * </p>
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isCompositeProperty(Property property)
	{
		return isCompositeModel(property.getModel());
	}

	/**
	 * 获取模型数据类型。
	 * 
	 * @param model
	 * @return
	 */
	public static Class<?> getType(Model model)
	{
		return model.getType();
	}

	/**
	 * 判断给定{@linkplain Model 模型}的{@linkplain Model#getType() 类型}是否是目标类型。
	 * 
	 * @param model
	 * @param type
	 * @return
	 */
	public static boolean isType(Model model, Class<?> type)
	{
		Class<?> myType = model.getType();

		return (type.equals(myType));
	}

	/**
	 * 获取属性数据类型。
	 * 
	 * @param property
	 * @return
	 */
	public static Class<?> getType(Property property)
	{
		return property.getModel().getType();
	}

	/**
	 * 判断给定{@linkplain Property 属性}的类型是否是目标类型。
	 * 
	 * @param property
	 * @param type
	 * @return
	 */
	public static boolean isType(Property property, Class<?> type)
	{
		Class<?> myType = property.getModel().getType();

		return (type.equals(myType));
	}

	/**
	 * 获取{@linkplain Property}。
	 * <p>
	 * 如果未找到，它将抛出异常。
	 * </p>
	 * 
	 * @param model
	 * @param propertyName
	 * @return
	 */
	public static Property getProperty(Model model, String propertyName)
	{
		Property property = model.getProperty(propertyName);

		if (property == null)
			throw new IllegalArgumentException("No property named [" + propertyName + "] found in [" + model + "]");

		return property;
	}

	/**
	 * 获取指定{@linkplain Property}的索引。
	 * 
	 * @param model
	 * @param property
	 * @return 索引，如果未找到，则抛出{@linkplain IllegalArgumentException}。
	 */
	public static int getPropertyIndex(Model model, Property property)
	{
		Property[] properties = model.getProperties();

		for (int i = 0; i < properties.length; i++)
		{
			if (properties[i] == property)
				return i;
		}

		throw new IllegalArgumentException("No property [" + property + "] found in [" + model + "]");
	}

	/**
	 * 获取指定名称的{@linkplain Property}数组。
	 * <p>
	 * 此方法不会返回空数组和{@code null}元素。
	 * </p>
	 * 
	 * @param model
	 * @param propertyNames
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Property[] getProperties(Model model, String[] propertyNames) throws IllegalArgumentException
	{
		Property[] properties = new Property[propertyNames.length];

		for (int i = 0; i < properties.length; i++)
		{
			Property property = model.getProperty(propertyNames[i]);

			if (property == null)
				throw new IllegalArgumentException("No Property named [" + propertyNames[i] + "] is found");

			properties[i] = property;
		}

		return properties;
	}

	/**
	 * 如果有，从{@linkplain Property}数组中移除指定名称的一个，否则返回原数组。
	 * 
	 * @param properties
	 * @param removePropertyName
	 *            允许为{@code null}
	 * @return
	 */
	public static Property[] removeIf(Property[] properties, String removePropertyName)
	{
		if (removePropertyName == null || removePropertyName.isEmpty())
			return properties;
		else
		{
			int removeIndex = -1;

			for (int i = 0; i < properties.length; i++)
			{
				Property property = properties[i];

				if (property.getName().equals(removePropertyName))
				{
					removeIndex = i;
					break;
				}
			}

			if (removeIndex < 0)
				return properties;
			else
			{
				Property[] newProperties = new Property[properties.length - 1];

				for (int i = 0; i < properties.length; i++)
				{
					if (i < removeIndex)
						newProperties[i] = properties[i];
					else if (i == removeIndex)
						;
					else
						newProperties[i - 1] = properties[i];
				}

				return newProperties;
			}
		}
	}

	/**
	 * 获取{@linkplain Property}名称数组。
	 * 
	 * @param model
	 * @param properties
	 * @return
	 */
	public static String[] getPropertyNames(Model model, Property[] properties)
	{
		String[] propertyNames = new String[properties.length];

		for (int i = 0; i < propertyNames.length; i++)
			propertyNames[i] = properties[i].getName();

		return propertyNames;
	}

	/**
	 * 获取属性值。
	 * 
	 * @param model
	 * @param obj
	 *            允许为{@code null}
	 * @param property
	 * @return
	 */
	public static Object getPropertyValue(Model model, Object obj, Property property)
	{
		return (obj == null ? null : property.get(obj));
	}

	/**
	 * 获取属性值。
	 * 
	 * @param model
	 * @param obj
	 *            允许为{@code null}
	 * @param properties
	 * @return
	 */
	public static Object[] getPropertyValues(Model model, Object obj, Property[] properties)
	{
		Object[] propValues = new Object[properties.length];

		for (int i = 0; i < properties.length; i++)
		{
			propValues[i] = getPropertyValue(model, obj, properties[i]);
		}

		return propValues;
	}

	/**
	 * 获取所有属性值。
	 * 
	 * @param model
	 * @param obj
	 *            允许为{@code null}
	 * @return
	 */
	public static Object[] getPropertyValues(Model model, Object obj)
	{
		return getPropertyValues(model, obj, model.getProperties());
	}

	/**
	 * 设置属性值。
	 * 
	 * @param model
	 * @param obj
	 * @param property
	 * @param propertyValue
	 */
	public static void setPropertyValue(Model model, Object obj, Property property, Object propertyValue)
	{
		property.set(obj, propertyValue);
	}

	/**
	 * 设置属性值。
	 * 
	 * @param model
	 * @param obj
	 * @param properties
	 * @param propertyValues
	 */
	public static void setPropertyValues(Model model, Object obj, Property[] properties, Object[] propertyValues)
	{
		for (int i = 0; i < properties.length; i++)
			setPropertyValue(model, obj, properties[i], propertyValues[i]);
	}

	/**
	 * 设置所有属性值。
	 * 
	 * @param model
	 * @param obj
	 * @param propertyValues
	 */
	public static void setPropertyValues(Model model, Object obj, Object[] propertyValues)
	{
		setPropertyValues(model, obj, model.getProperties(), propertyValues);
	}

	/**
	 * 创建{@linkplain Model}实例对象。
	 * 
	 * @param model
	 * @return
	 * @throws InstanceCreationException
	 */
	public static Object instance(Model model) throws InstanceCreationException
	{
		return model.newInstance();
	}

	/**
	 * 浅拷贝对象。
	 * 
	 * @param model
	 * @param obj
	 * @return
	 * @throws InstanceCreationException
	 */
	public static Object clone(Model model, Object obj) throws InstanceCreationException
	{
		Object clonedObj = instance(model);

		Property[] properties = model.getProperties();

		for (Property property : properties)
		{
			Object pv = property.get(obj);
			property.set(clonedObj, pv);
		}

		return clonedObj;
	}
}
