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
import org.datagear.analysis.support.JsonChartPluginPropertiesResolver;
import org.datagear.analysis.support.html.DashboardApiVersion;

/**
 * 图表插件表单。
 * <p>
 * 此类描述{@linkplain ChartPlugin}的UI交互操作表单信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginForm extends ObjectFormProperty implements NameAware
{
	private static final long serialVersionUID = 1L;

	/**
	 * 表单值存储在{@linkplain ChartDefinition#getAttrValues()}中时的名称。
	 */
	public static final String FORM_VALUE_ATTR_NAME = ChartDefinition.BUILTIN_ATTR_PREFIX + "FORM_VALUE";

	public static final String PROPERTY_NAME = "name";

	public ChartPluginForm()
	{
		super();
		super.setArray(false);
	}

	public ChartPluginForm(List<FormProperty> properties)
	{
		super(null, properties);
		super.setArray(false);
	}

	public ChartPluginForm(String name, List<FormProperty> properties)
	{
		super(name, properties);
		super.setArray(false);
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
	 * <p>
	 * 对于{@linkplain DashboardApiVersion#V2}版本的{@linkplain ChartPlugin}，始终会设置为{@linkplain #FORM_VALUE_ATTR_NAME}，
	 * 因为表单值存储在何处是系统内部逻辑，不应开放定义，详细参考{@linkplain JsonChartPluginPropertiesResolver}中的相关逻辑。
	 * </p>
	 * <p>
	 * 对于旧的{@linkplain DashboardApiVersion#V1}版的{@linkplain ChartPlugin}，为了兼容旧版逻辑，此值会为{@code null}。
	 * </p>
	 */
	@Override
	public String getName()
	{
		return super.getName();
	}

	/**
	 * 图表插件表单不允许数组，始终会返回{@code false}。
	 */
	@Override
	public boolean isArray()
	{
		return super.isArray();
	}

	@Override
	public void setArray(boolean array)
	{
		// 不抛出异常，避免相关反射库操作异常
		super.setArray(false);
	}

	@Override
	public ChartPluginForm toLocale(Locale locale)
	{
		ChartPluginForm target = (ChartPluginForm) super.toLocale(locale);

		target.setName(this.getName());

		return target;
	}

	@Override
	protected ChartPluginForm createEmpty()
	{
		return new ChartPluginForm();
	}

}
