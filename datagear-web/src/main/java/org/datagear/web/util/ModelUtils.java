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
	 * 获取指定{@linkplain PropertyPath}的{@linkplain Property}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @return
	 */
	// public static Property getProperty(Model model, PropertyPath
	// propertyPath)
	// {
	// Property property = null;
	//
	// Model parent = model;
	//
	// for (int i = 0, len = propertyPath.length(); i < len; i++)
	// {
	// if (propertyPath.isElement(i))
	// continue;
	// else if (propertyPath.isProperty(i))
	// {
	// String pname = propertyPath.getPropertyName(i);
	//
	// property = model.getProperty(pname);
	//
	// if (property == null)
	// throw new IllegalPropertyPathException("[" + propertyPath + "] is
	// illegal, no Property named ["
	// + pname + "] found in Model [" + parent + "]");
	//
	// if (i < len - 1)
	// {
	// if (MU.isConcreteProperty(property))
	// parent = property.getModel();
	// else if (MU.isAbstractedProperty(property))
	// {
	// if (!propertyPath.hasPropertyConcreteModelName(i))
	// throw new IllegalPropertyPathException("[" + propertyPath
	// + "] is illegal, concrete property model name should be defined after ["
	// + pname
	// + "]");
	//
	// String pcmn = propertyPath.getPropertyConcreteModelName(i);
	//
	// Model myModel = MU.getModel(MU.getModels(property), pcmn);
	//
	// if (myModel == null)
	// throw new IllegalPropertyPathException("[" + propertyPath
	// + "] is illegal, concrete property model not found of name [" + pcmn +
	// "]");
	//
	// parent = myModel;
	// }
	// else
	// throw new UnsupportedOperationException();
	// }
	// }
	// else
	// throw new UnsupportedOperationException();
	// }
	//
	// return property;
	// }

	/**
	 * 获取指定{@linkplain PropertyPath}的{@linkplain NameLabel}文本值。
	 * 
	 * @param model
	 * @param propertyPath
	 * @param locale
	 * @return
	 */
	public static String getNameLabelValuePath(Model model, PropertyPath propertyPath, Locale locale)
	{
		return getNameLabelValuePath(model, propertyPath, locale, " - ", true);
	}

	/**
	 * 获取指定{@linkplain PropertyPath}的{@linkplain NameLabel}文本值。
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
	public static String getNameLabelValuePath(Model model, PropertyPath propertyPath, Locale locale, String splitter,
			boolean appendModelLabel)
	{
		StringBuilder sb = new StringBuilder();

		if (appendModelLabel)
		{
			sb.append(PropertyPath.escapePropertyName(getNameLabelValue(model, locale)));
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
					sb.append(PropertyPath.escapePropertyName(pname));
				else
					sb.append(getNameLabelValue(property, locale));

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
	 * 获取{@linkplain Model}的{@linkplain NameLabel}文本值。
	 * 
	 * @param model
	 * @param locale
	 * @return
	 */
	public static String getNameLabelValue(Model model, Locale locale)
	{
		String re = null;

		String tableNameStr = null;
		String labelStr = null;

		TableName tableName = model.getFeature(TableName.class);
		if (tableName != null)
			tableNameStr = tableName.getValue();

		NameLabel nameLabel = model.getFeature(NameLabel.class);

		if (nameLabel != null)
		{
			Label label = nameLabel.getValue();
			if (label != null)
				labelStr = label.getValue(locale);
		}

		// 两者相同，取一个即可
		if (tableNameStr != null && tableNameStr.equalsIgnoreCase(labelStr))
			labelStr = null;

		if (tableNameStr == null || tableNameStr.isEmpty())
			re = labelStr;
		else if (labelStr == null || labelStr.isEmpty())
			re = tableNameStr;
		else
			re = tableNameStr + "(" + labelStr + ")";

		if (re == null || re.isEmpty())
			re = model.getName();

		return re;
	}

	/**
	 * 获取{@linkplain Property}的{@linkplain NameLabel}文本值。
	 * 
	 * @param property
	 * @param locale
	 * @return
	 */
	public static String getNameLabelValue(Property property, Locale locale)
	{
		String re = null;

		String columnName = null;
		String nameLabel = null;

		ColumnName columnNameFeature = property.getFeature(ColumnName.class);
		if (columnNameFeature != null)
			columnName = columnNameFeature.getValue();

		NameLabel nameLabelFeature = property.getFeature(NameLabel.class);

		if (nameLabelFeature != null)
		{
			Label label = nameLabelFeature.getValue();
			if (label != null)
				nameLabel = label.getValue(locale);
		}

		// 两者相同，取一个即可
		if (columnName != null && columnName.equalsIgnoreCase(nameLabel))
			nameLabel = null;

		if (columnName == null || columnName.isEmpty())
			re = nameLabel;
		else if (nameLabel == null || nameLabel.isEmpty())
			re = columnName;
		else
			re = columnName + "(" + nameLabel + ")";

		if (re == null || re.isEmpty())
			re = property.getName();

		re = PropertyPath.escapePropertyName(re);

		return re;
	}
}
