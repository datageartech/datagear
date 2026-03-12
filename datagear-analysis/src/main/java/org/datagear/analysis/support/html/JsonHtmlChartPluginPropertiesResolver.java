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
import java.util.regex.Pattern;

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
 * 
 * @author datagear@163.com
 *
 */
public class JsonHtmlChartPluginPropertiesResolver<T extends HtmlChartPlugin>
		extends JsonChartPluginPropertiesResolver<T>
{
	public static final String JSON_PROPERTY_USAGE = HtmlChartPlugin.PROPERTY_USAGE;
	public static final String JSON_PROPERTY_PLATFORM_VERSION = HtmlChartPlugin.PROPERTY_PLATFORM_VERSION;
	public static final String JSON_PROPERTY_API_VERSION = HtmlChartPlugin.PROPERTY_API_VERSION;

	public static final Pattern DATA_SIGN_NAME_PATTERN = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*$");

	public JsonHtmlChartPluginPropertiesResolver()
	{
		super();
	}

	public JsonHtmlChartPluginPropertiesResolver(T chartPlugin)
	{
		super(chartPlugin);
	}

	@Override
	public T resolveProperties(Map<String, ?> properties)
	{
		T chartPlugin = getChartPlugin();

		String apiVersion = convertToString(properties.get(JSON_PROPERTY_API_VERSION));
		chartPlugin.setApiVersion(DashboardApiVersion.trimVersion(apiVersion));

		String usage = convertToString(properties.get(JSON_PROPERTY_USAGE));
		usage = HtmlChartPluginUsage.normalize(usage);

		chartPlugin.setUsage(usage);
		chartPlugin.setPlatformVersion(convertToString(properties.get(JSON_PROPERTY_PLATFORM_VERSION)));
		return super.resolveProperties(properties);
	}
}
