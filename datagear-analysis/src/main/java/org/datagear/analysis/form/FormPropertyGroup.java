/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.datagear.analysis.AdditionsAware;
import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.LabelUtil;
import org.datagear.util.i18n.Labeled;

/**
 * 图表插件属性分组。
 * 
 * @author datagear@163.com
 *
 */
public class FormPropertyGroup extends AbstractLabeled implements AdditionsAware, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	public static final String PROPERTY_NAMES = "names";
	public static final String PROPERTY_ADDITIONS = AdditionsAware.PROPERTY_ADDITIONS;

	/** 分组内的{@linkplain FormProperty#getName()} */
	private List<String> names = Collections.emptyList();

	/** 扩展属性 */
	private Map<String, ?> additions = null;

	public FormPropertyGroup()
	{
		super();
	}

	public FormPropertyGroup(List<String> names)
	{
		super();
		this.names = names;
	}

	public List<String> getNames()
	{
		return names;
	}

	public void setNames(List<String> names)
	{
		this.names = names;
	}

	@Override
	public Map<String, ?> getAdditions()
	{
		return additions;
	}

	public void setAdditions(Map<String, ?> additions)
	{
		this.additions = additions;
	}

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param locale
	 * @return
	 */
	public FormPropertyGroup toLocale(Locale locale)
	{
		FormPropertyGroup target = new FormPropertyGroup(this.names);
		LabelUtil.concrete(this, target, locale);
		target.setAdditions(this.additions);

		return target;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [names=" + names + ", nameLabel=" + getNameLabel() + ", descLabel="
				+ getDescLabel() + "]";
	}

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param groups
	 * @param locale
	 * @return
	 */
	public static List<FormPropertyGroup> toLocale(List<FormPropertyGroup> groups, Locale locale)
	{
		if (groups == null)
			return null;

		if (groups.isEmpty())
			return Collections.emptyList();

		List<FormPropertyGroup> re = new ArrayList<FormPropertyGroup>(groups.size());

		for (FormPropertyGroup group : groups)
			re.add(group.toLocale(locale));

		return re;
	}
}
