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
 * 图表插件配置表单。
 * <p>
 * 此类描述{@linkplain ChartPlugin}可进行UI交互的配置表单信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginConfigForm extends ObjectFormProperty implements NameAware
{
	private static final long serialVersionUID = 1L;

	/**
	 * 配置值在{@linkplain ChartDefinition#getConfigValues()}中的存储属性名。
	 */
	public static final String CONFIG_VALUE_ATTR_NAME = ChartDefinition.BUILTIN_ATTR_PREFIX + "CONFIG_VALUE";

	public static final String PROPERTY_NAME = "name";

	public ChartPluginConfigForm()
	{
		super();
		super.setArray(false);
	}

	public ChartPluginConfigForm(List<FormProperty> properties)
	{
		super(null, properties);
		super.setArray(false);
	}

	public ChartPluginConfigForm(String name, List<FormProperty> properties)
	{
		super(name, properties);
		super.setArray(false);
	}

	/**
	 * 获取名称。
	 * <p>
	 * 此名称表示表单数据应存储在{@linkplain ChartDefinition#getConfigValues()}中的名称。
	 * </p>
	 * <p>
	 * 如果为{@code null}，表示表单数据以其包含的{@linkplain #getProperties()}的{@linkplain FormProperty#getName()}
	 * 分散存储在{@linkplain ChartDefinition#getConfigValues()}中。
	 * </p>
	 * <p>
	 * 对于{@linkplain DashboardApiVersion#V2}版本的{@linkplain ChartPlugin}，此值会被设为固定的{@linkplain #CONFIG_VALUE_ATTR_NAME}，
	 * 因为存储位置是系统内部逻辑，不应对外开放，详细参考{@linkplain JsonChartPluginPropertiesResolver}相关逻辑。
	 * </p>
	 * <p>
	 * 对于旧的{@linkplain DashboardApiVersion#V1}版本的{@linkplain ChartPlugin}，此值会为{@code null}，已兼容旧版逻辑。
	 * </p>
	 */
	@Override
	public String getName()
	{
		return super.getName();
	}

	/**
	 * 图表插件配置表单不允许数组，应始终返回{@code false}。
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
	public ChartPluginConfigForm toLocale(Locale locale)
	{
		ChartPluginConfigForm target = (ChartPluginConfigForm) super.toLocale(locale);

		target.setName(this.getName());

		return target;
	}

	@Override
	protected ChartPluginConfigForm createEmpty()
	{
		return new ChartPluginConfigForm();
	}

}
