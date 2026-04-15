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

import org.datagear.analysis.ChartDefinition;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * 仅渲染图表指定配置值的{@linkplain HtmlChartPlugin}。
 * <p>
 * 它从{@linkplain ChartDefinition#getConfigValues()}获取{@linkplain #getConfigName()}对应的值，并将其作为图表内容渲染。
 * </p>
 * <p>
 * 注意：此插件的页面端要求如下：
 * </p>
 * <p>
 * {@code chart.element()}函数，用于获取图表HTML元素 <br>
 * {@code chart.configValue(name)}函数，用于获取图表指定名称的图表配置值
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConfigValueHtmlChartPlugin extends HtmlChartPlugin
{
	private static final long serialVersionUID = 1L;

	private String configName;

	public ConfigValueHtmlChartPlugin()
	{
		super();
	}

	public ConfigValueHtmlChartPlugin(String id, Label nameLabel, String configName)
	{
		super(id, nameLabel, null, HtmlChartPluginScriptObjectWriter.INSTANCE,
				HtmlRenderContextScriptObjectWriter.INSTANCE, HtmlChartScriptObjectWriter.INSTANCE);
		this.configName = configName;
		super.setRenderer(buildJsChartRenderer(configName));
	}

	public String getConfigName()
	{
		return configName;
	}

	public void setConfigName(String configName)
	{
		this.configName = configName;
		super.setRenderer(buildJsChartRenderer(configName));
	}

	protected StringJsChartRenderer buildJsChartRenderer(String attrName)
	{
		String newLine = getNewLine();
		// 这样使用<div>包裹可以避免直接设置文本导致图表元素竖向错位
		String valueExp = "\"<div style='position:absolute;'>\"+value+\"</div>\"";
		return new StringJsChartRenderer(JsChartRenderer.CODE_TYPE_OBJECT, "{" + newLine //
				+ "	render : function(chart)" + newLine //
				+ "	{" + newLine + //
				"		var element = chart.element();" + newLine //
				+ "		var value = chart.configValue(" + StringUtil.toJavaScriptString(attrName) + ");" + newLine //
				+ "		element.innerHTML = " + valueExp + ";" + newLine //
				+ "	}," + newLine //
				+ "	update : function(){}" + newLine //
				+ "}");
	}
}
