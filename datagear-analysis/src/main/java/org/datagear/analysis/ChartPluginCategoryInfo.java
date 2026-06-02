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

import org.datagear.util.i18n.Localizable;

/**
 * 图表插件所属类别信息。
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginCategoryInfo implements Localizable, Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_CATEGORY = "category";
	public static final String PROPERTY_ORDER = "order";

	/** 所属类别 */
	private Category category;

	/** 在类别内排序值 */
	private int order = 0;

	public ChartPluginCategoryInfo()
	{
		super();
	}

	public ChartPluginCategoryInfo(Category category)
	{
		super();
		this.category = category;
	}

	public ChartPluginCategoryInfo(Category category, int order)
	{
		super();
		this.category = category;
		this.order = order;
	}

	public Category getCategory()
	{
		return category;
	}

	public void setCategory(Category category)
	{
		this.category = category;
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
	public ChartPluginCategoryInfo toLocale(Locale locale)
	{
		ChartPluginCategoryInfo target = createEmpty();

		target.setCategory(this.category == null ? null : this.category.toLocale(locale));
		target.setOrder(this.order);

		return target;
	}

	protected ChartPluginCategoryInfo createEmpty()
	{
		return new ChartPluginCategoryInfo();
	}
}
