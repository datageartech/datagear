/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.util;

import java.util.Locale;

import org.datagear.model.Label;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.features.NameLabel;
import org.datagear.model.support.IllegalPropertyPathException;
import org.datagear.model.support.MU;
import org.datagear.model.support.PropertyPath;
import org.datagear.model.support.PropertyPathInfo;
import org.datagear.persistence.features.ColumnName;
import org.datagear.persistence.features.TableName;
import org.datagear.persistence.support.PMU;

/**
 * {@linkplain Model}常用工具类。
 * 
 * @author datagear@163.com
 *
 */
public class ModelUtils
{
	/**
	 * 将属性路径字符串转换为{@linkplain PropertyPath}。
	 * 
	 * @param propertyPath
	 * @return
	 */
	public static PropertyPath toPropertyPath(String propertyPath)
	{
		return PropertyPath.valueOf(propertyPath);
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @return
	 */
	public static PropertyPathInfo toPropertyPathInfo(Model model, String propertyPath)
	{
		return toPropertyPathInfo(model, toPropertyPath(propertyPath));
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @return
	 */
	public static PropertyPathInfo toPropertyPathInfo(Model model, PropertyPath propertyPath)
	{
		return PropertyPathInfo.valueOf(model, propertyPath);
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @param obj
	 * @return
	 */
	public static PropertyPathInfo toPropertyPathInfo(Model model, String propertyPath, Object obj)
	{
		return toPropertyPathInfo(model, toPropertyPath(propertyPath), obj);
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @param obj
	 * @return
	 */
	public static PropertyPathInfo toPropertyPathInfo(Model model, PropertyPath propertyPath, Object obj)
	{
		return PropertyPathInfo.valueOf(model, propertyPath, obj);
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * <p>
	 * 如果{@linkplain PropertyPathInfo#hasModelTail()}为{@code false}，此方法将抛出
	 * {@linkplain IllegalArgumentException}。
	 * </p>
	 * 
	 * @param model
	 * @param propertyPath
	 * @return
	 */
	public static PropertyPathInfo toPropertyPathInfoConcrete(Model model, String propertyPath)
	{
		return toPropertyPathInfoConcrete(model, toPropertyPath(propertyPath));
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * <p>
	 * 如果{@linkplain PropertyPathInfo#hasModelTail()}为{@code false}，此方法将抛出
	 * {@linkplain IllegalArgumentException}。
	 * </p>
	 * 
	 * @param model
	 * @param propertyPath
	 * @return
	 */
	public static PropertyPathInfo toPropertyPathInfoConcrete(Model model, PropertyPath propertyPath)
	{
		PropertyPathInfo propertyPathInfo = PropertyPathInfo.valueOf(model, propertyPath);

		if (!propertyPathInfo.hasModelTail())
			throw new IllegalArgumentException(
					"The tail Model of the property path [" + propertyPath + "] must not be null");

		return propertyPathInfo;
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * <p>
	 * 如果{@linkplain PropertyPathInfo#hasModelTail()}为{@code false}，此方法将抛出
	 * {@linkplain IllegalArgumentException}。
	 * </p>
	 * 
	 * @param model
	 * @param propertyPath
	 * @param obj
	 * @return
	 */
	public static PropertyPathInfo toPropertyPathInfoConcrete(Model model, String propertyPath, Object obj)
	{
		return toPropertyPathInfoConcrete(model, toPropertyPath(propertyPath), obj);
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * <p>
	 * 如果{@linkplain PropertyPathInfo#hasModelTail()}为{@code false}，此方法将抛出
	 * {@linkplain IllegalArgumentException}。
	 * </p>
	 * 
	 * @param model
	 * @param propertyPath
	 * @param obj
	 * @return
	 */
	public static PropertyPathInfo toPropertyPathInfoConcrete(Model model, PropertyPath propertyPath, Object obj)
	{
		PropertyPathInfo propertyPathInfo = PropertyPathInfo.valueOf(model, propertyPath, obj);

		if (!propertyPathInfo.hasModelTail())
			throw new IllegalArgumentException(
					"The tail Model of the property path [" + propertyPath + "] must not be null");

		return propertyPathInfo;
	}

	/**
	 * 判断给定属性具体模型是否是隶属属性模型。
	 * <p>
	 * 隶属属性模型没有独立生命周期。
	 * </p>
	 * 
	 * @param propertyPathInfo
	 * @return
	 */
	public static boolean isPrivatePropertyModelTail(PropertyPathInfo propertyPathInfo)
	{
		if (!propertyPathInfo.hasModelTail())
			throw new IllegalArgumentException("The tail Model of the property path ["
					+ propertyPathInfo.getPropertyPath() + "] must not be null");

		Model tailModel = propertyPathInfo.getModelTail();

		Property property = propertyPathInfo.getPropertyTail();

		return PMU.isPrivate(propertyPathInfo.getOwnerModelTail(), property, tailModel);
	}

	/**
	 * 获取指定{@linkplain PropertyPath}的展示名称。
	 * 
	 * @param model
	 * @param propertyPath
	 * @param locale
	 * @return
	 */
	public static String displayName(Model model, PropertyPath propertyPath, Locale locale)
	{
		return displayName(model, propertyPath, locale, " - ", true);
	}

	/**
	 * 获取指定{@linkplain PropertyPath}的展示名称。
	 * 
	 * @param model
	 * @param propertyPath
	 * @param locale
	 * @param splitter
	 *            分隔符
	 * @param appendModelLabel
	 *            是否在开头添加{@code model}的标签。
	 * @return
	 */
	public static String displayName(Model model, PropertyPath propertyPath, Locale locale, String splitter,
			boolean appendModelLabel)
	{
		StringBuilder sb = new StringBuilder();

		if (appendModelLabel)
		{
			sb.append(displayName(model, locale));
			sb.append(splitter);
		}

		Model parent = model;

		for (int i = 0, len = propertyPath.length(); i < len; i++)
		{
			if (propertyPath.isElement(i))
				continue;
			else if (propertyPath.isProperty(i))
			{
				String pname = propertyPath.getPropertyName(i);
				Property property = parent.getProperty(pname);

				if (i < len - 1 && property == null)
					throw new IllegalPropertyPathException("[" + propertyPath + "] is illegal, no Property named ["
							+ pname + "] found in Model [" + parent + "]");

				if (i > 0)
					sb.append(splitter);

				// 集合属性值size没有Property
				if (property == null)
					sb.append(pname);
				else
					sb.append(displayName(property, locale));

				if (i < len - 1)
				{
					if (MU.isConcreteProperty(property))
						parent = property.getModel();
					else if (MU.isAbstractedProperty(property))
					{
						if (!propertyPath.hasPropertyModelIndex(i))
							throw new IllegalPropertyPathException("[" + propertyPath
									+ "] is illegal, concrete property model name should be defined after [" + pname
									+ "]");

						Model myModel = MU.getPropertyModel(property, propertyPath.getPropertyModelIndex(i));

						parent = myModel;
					}
					else
						throw new UnsupportedOperationException();
				}
			}
			else
				throw new UnsupportedOperationException();
		}

		return sb.toString();
	}

	/**
	 * 获取{@linkplain Model}的展示名称。
	 * 
	 * @param model
	 * @param locale
	 * @return
	 */
	public static String displayName(Model model, Locale locale)
	{
		NameLabel nameLabel = model.getFeature(NameLabel.class);

		if (nameLabel != null)
		{
			Label label = nameLabel.getValue();

			if (label != null)
				return label.getValue(locale);
		}

		TableName tableName = model.getFeature(TableName.class);
		if (tableName != null)
			return tableName.getValue();

		return model.getName();
	}

	/**
	 * 获取{@linkplain Property}的展示名称。
	 * 
	 * @param property
	 * @param locale
	 * @return
	 */
	public static String displayName(Property property, Locale locale)
	{
		NameLabel nameLabel = property.getFeature(NameLabel.class);

		if (nameLabel != null)
		{
			Label label = nameLabel.getValue();

			if (label != null)
				return label.getValue(locale);
		}

		ColumnName columnNameFeature = property.getFeature(ColumnName.class);
		if (columnNameFeature != null)
			return columnNameFeature.getValue();

		return property.getName();
	}
}
