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

package org.datagear.management.domain;

import java.util.Locale;

import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginJson;
import org.datagear.util.i18n.Label;
import org.datagear.util.i18n.LabelUtil;

/**
 * {@linkplain HtmlChartPlugin} 值类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginVo extends HtmlChartPluginJson
{
	private static final long serialVersionUID = 1L;

	/** 是否有手册文件 */
	private boolean hasManual = false;

	public HtmlChartPluginVo()
	{
		super();
	}

	public HtmlChartPluginVo(String id)
	{
		super(id, null);
	}

	public HtmlChartPluginVo(String id, Label nameLabel)
	{
		super(id, nameLabel);
	}

	public HtmlChartPluginVo(HtmlChartPlugin plugin)
	{
		this(plugin, false);
	}

	public HtmlChartPluginVo(HtmlChartPlugin plugin, boolean detail)
	{
		super(plugin);

		if (!detail)
		{
			setResources(null);
			setConfigForm(null);
			setDataSignSpec(null);
			setDataSetRange(null);
			setCategories(null);
			setCategoryOrders(null);
			setAdditions(null);
		}
	}

	public HtmlChartPluginVo(HtmlChartPlugin plugin, Locale locale)
	{
		this(plugin);
		LabelUtil.concrete(plugin, this, locale);
	}

	public HtmlChartPluginVo(HtmlChartPlugin plugin, boolean detail, Locale locale)
	{
		this(plugin, detail);

		if (detail)
			initLocalized(plugin, locale);
		else
			LabelUtil.concrete(plugin, this, locale);
	}

	public boolean isHasManual()
	{
		return hasManual;
	}

	public void setHasManual(boolean hasManual)
	{
		this.hasManual = hasManual;
	}
}
