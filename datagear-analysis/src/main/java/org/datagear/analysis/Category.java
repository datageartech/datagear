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

package org.datagear.analysis;

import java.io.Serializable;
import java.util.Locale;

import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.LabelUtil;
import org.datagear.util.i18n.Labeled;
import org.datagear.util.i18n.Localizable;

/**
 * 类别。
 * <p>
 * 用于描述实体所属的类别信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class Category extends AbstractLabeled implements NameAware, Localizable, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	public static final String PROPERTY_ORDER = "order";

	private String name;

	private int order = 0;

	public Category()
	{
		super();
	}

	public Category(String name)
	{
		super();
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	@Override
	public Category toLocale(Locale locale)
	{
		Category target = createEmpty();

		target.setName(this.name);
		target.setOrder(this.order);
		LabelUtil.concrete(this, target, locale);

		return target;
	}

	protected Category createEmpty()
	{
		return new Category();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", nameLabel=" + getNameLabel() + ", descLabel="
				+ getDescLabel() + ", order=" + order + "]";
	}
}
