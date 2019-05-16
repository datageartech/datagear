/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.util;

import java.util.Locale;

import org.datagear.model.Label;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.features.DescLabel;
import org.datagear.model.features.NameLabel;
import org.datagear.model.support.IllegalPropertyPathException;
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
	 * 判断给定属性是否是私有的。
	 * <p>
	 * 私有属性没有独立生命周期。
	 * </p>
	 * 
	 * @param propertyPathInfo
	 * @return
	 */
	public static boolean isPrivatePropertyTail(PropertyPathInfo propertyPathInfo)
	{
		Property property = propertyPathInfo.getPropertyTail();
		return PMU.isPrivate(propertyPathInfo.getOwnerModelTail(), property);
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
					parent = property.getModel();
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

	/**
	 * 获取{@linkplain Model}的展示描述。
	 * 
	 * @param model
	 * @param locale
	 * @return
	 */
	public static String displayDesc(Model model, Locale locale)
	{
		DescLabel descLabel = model.getFeature(DescLabel.class);

		if (descLabel != null)
		{
			Label label = descLabel.getValue();

			if (label != null)
				return label.getValue(locale);
		}

		return "";
	}

	/**
	 * 获取{@linkplain Property}的展示描述。
	 * 
	 * @param model
	 * @param locale
	 * @return
	 */
	public static String displayDesc(Property property, Locale locale)
	{
		DescLabel descLabel = property.getFeature(DescLabel.class);

		if (descLabel != null)
		{
			Label label = descLabel.getValue();

			if (label != null)
				return label.getValue(locale);
		}

		return "";
	}
}
