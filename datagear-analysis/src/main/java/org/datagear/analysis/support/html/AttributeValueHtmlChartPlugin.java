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
 * 仅渲染图表指定属性值的{@linkplain HtmlChartPlugin}。
 * <p>
 * 它从{@linkplain ChartDefinition#getAttrValues()}获取{@linkplain #getAttrName()}对应的值，并将其作为图表内容渲染。
 * </p>
 * <p>
 * 注意：此插件的页面端要求如下：
 * </p>
 * <p>
 * {@code chart.element()} 函数，用于获取图表HTML元素 <br>
 * {@code chart.attrValue(name)} 函数，用于获取图表指定名称的图表属性值
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AttributeValueHtmlChartPlugin extends HtmlChartPlugin
{
	private static final long serialVersionUID = 1L;

	private String attrName;

	public AttributeValueHtmlChartPlugin()
	{
		super();
	}

	public AttributeValueHtmlChartPlugin(String id, Label nameLabel, String attrName)
	{
		super(id, nameLabel, null, HtmlChartPluginScriptObjectWriter.INSTANCE,
				HtmlRenderContextScriptObjectWriter.INSTANCE, HtmlChartScriptObjectWriter.INSTANCE);
		this.attrName = attrName;
		super.setRenderer(buildJsChartRenderer(attrName));
	}

	public String getAttrName()
	{
		return attrName;
	}

	public void setAttrName(String attrName)
	{
		this.attrName = attrName;
		super.setRenderer(buildJsChartRenderer(attrName));
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
				+ "		var value = chart.attrValue(" + StringUtil.toJavaScriptString(attrName) + ");" + newLine //
				+ "		element.innerHTML = " + valueExp + ";" + newLine //
				+ "	}," + newLine //
				+ "	update : function(){}" + newLine //
				+ "}");
	}
}
