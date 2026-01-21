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

import org.datagear.analysis.form.FormProperty;
import org.datagear.analysis.form.ObjectFormProperty;

/**
 * 图表插件属性表单。
 * <p>
 * 此类描述{@linkplain ChartPlugin#renderChart(ChartDefinition, RenderContext)}的{@linkplain ChartDefinition#getAttrValues()}的UI交互操作表单信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginAttributeForm extends ObjectFormProperty implements NameAware
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_NAME = "name";

	private String name = null;

	public ChartPluginAttributeForm()
	{
		super();
	}

	public ChartPluginAttributeForm(List<FormProperty> properties)
	{
		super(null, properties);
	}

	public ChartPluginAttributeForm(String name, List<FormProperty> properties)
	{
		super(name, properties);
		this.name = name;
	}

	/**
	 * 获取名称。
	 * <p>
	 * 此名称表示表单数据应存储在{@linkplain ChartDefinition#getAttrValues()}中的名称。
	 * </p>
	 * <p>
	 * 如果为{@code null}，表示表单数据以其包含的{@linkplain #getProperties()}的{@linkplain FormProperty#getName()}
	 * 分散存储在{@linkplain ChartDefinition#getAttrValues()}中。
	 * </p>
	 */
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public ChartPluginAttributeForm toLocale(Locale locale)
	{
		ChartPluginAttributeForm target = (ChartPluginAttributeForm) super.toLocale(locale);

		target.setName(this.name);

		return target;
	}

	@Override
	protected ChartPluginAttributeForm createEmpty()
	{
		return new ChartPluginAttributeForm();
	}

}
