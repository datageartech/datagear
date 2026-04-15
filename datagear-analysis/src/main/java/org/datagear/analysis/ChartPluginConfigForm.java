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

import java.util.List;
import java.util.Locale;

import org.datagear.analysis.form.Form;
import org.datagear.analysis.form.FormProperty;
import org.datagear.analysis.form.FormPropertyGroup;
import org.datagear.util.i18n.Label;

/**
 * 图表插件配置表单。
 * <p>
 * 此类描述{@linkplain ChartPlugin}可进行UI交互的配置表单信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginConfigForm extends Form
{
	private static final long serialVersionUID = 1L;

	public ChartPluginConfigForm()
	{
		super();
	}

	public ChartPluginConfigForm(List<FormProperty> properties)
	{
		super(properties);
	}

	public ChartPluginConfigForm(Label nameLabel, List<FormProperty> properties)
	{
		super(nameLabel, properties);
	}

	public ChartPluginConfigForm(List<FormProperty> properties, List<FormPropertyGroup> groups)
	{
		super(properties, groups);
	}

	public ChartPluginConfigForm(Label nameLabel, List<FormProperty> properties, List<FormPropertyGroup> groups)
	{
		super(nameLabel, properties, groups);
	}

	@Override
	public ChartPluginConfigForm toLocale(Locale locale)
	{
		ChartPluginConfigForm target = (ChartPluginConfigForm) super.toLocale(locale);
		return target;
	}

	@Override
	protected ChartPluginConfigForm createEmpty()
	{
		return new ChartPluginConfigForm();
	}
}
