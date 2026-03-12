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

import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.DataSign;
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

	/**
	 * {@linkplain DataSign#getName()}命名规范。
	 */
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

	@Override
	protected DataSign convertToDataSign(Object obj, DataSign parent)
	{
		DataSign dataSign = super.convertToDataSign(obj, parent);
		checkDataSignName(dataSign);

		return dataSign;
	}

	/**
	 * 校验{@linkplain DataSign#getName()}。
	 * <p>
	 * 对于{@linkplain DashboardApiVersion#V2}起的插件，
	 * 必须以{@code [a-z]}、{@code [A-Z]}、{@code '_'}、{@code '$'}之一开头，且后续只能额外包含{@code [0-9]}字符，
	 * 因为{@linkplain DataSetBind#getFieldSigns()}中需要以分隔符层级拼接{@linkplain DataSign#getName()}，必须限制命名规范以避免歧义。
	 * </p>
	 * 
	 * @param dataSign
	 */
	protected void checkDataSignName(DataSign dataSign)
	{
		if (dataSign == null)
			return;

		String apiVersion = getChartPlugin().getApiVersion();

		// 旧的1.0版本时的插件没有限制，这里需要忽略处理，以兼容旧版
		if (DashboardApiVersion.isV1(apiVersion))
			return;
		
		if(!DATA_SIGN_NAME_PATTERN.matcher(dataSign.getName()).matches())
			throw new IllegalArgumentException("DataSign name '" + dataSign.getName()
					+ "' illegal, it must matches pattern \"" + DATA_SIGN_NAME_PATTERN.toString() + "\"");
	}
}
