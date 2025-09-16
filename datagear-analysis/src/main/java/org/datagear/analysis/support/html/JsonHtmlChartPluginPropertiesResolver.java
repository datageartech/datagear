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

package org.datagear.analysis.support.html;

import java.util.Map;

import org.datagear.analysis.support.JsonChartPluginPropertiesResolver;

/**
 * JSON {@linkplain HtmlChartPlugin}属性解析器。
 * <p>
 * 此类支持的JSON格式继承自{@linkplain JsonChartPluginPropertiesResolver}，额外包括：
 * </p>
 * <code>
 * <pre>
 * {
 *   ...,
 *   platformVersion: "...",
 *   apiVersion: "..."
 * }
 * </pre>
 * </code>
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class JsonHtmlChartPluginPropertiesResolver<T extends HtmlChartPlugin>
		extends JsonChartPluginPropertiesResolver<T>
{
	public static final String JSON_PROPERTY_PLATFORM_VERSION = HtmlChartPlugin.PROPERTY_PLATFORM_VERSION;
	public static final String JSON_PROPERTY_API_VERSION = HtmlChartPlugin.PROPERTY_API_VERSION;

	public JsonHtmlChartPluginPropertiesResolver()
	{
		super();
	}

	@Override
	public void resolveChartPluginProperties(T chartPlugin, Map<String, ?> properties)
	{
		super.resolveChartPluginProperties(chartPlugin, properties);
		chartPlugin.setPlatformVersion(convertToString(properties.get(JSON_PROPERTY_PLATFORM_VERSION)));
		chartPlugin.setApiVersion(convertToString(properties.get(JSON_PROPERTY_API_VERSION)));
	}
}
