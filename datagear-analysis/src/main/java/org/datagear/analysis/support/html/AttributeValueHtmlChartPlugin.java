/*
 * Copyright 2018-2024 datagear.tech
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
 * 它从{@linkplain ChartDefinition#getAttrValues()}获取{@linkplain #getAttrName()}的值，并将其作为图表内容渲染。
 * </p>
 * <p>
 * 注意：此插件的JS渲染器要求运行环境（浏览器）如下：
 * </p>
 * <p>
 * jQuery 库 <br>
 * chart.elementJquery() 函数：获取图表元素jQuery对象 <br>
 * chart.attrValue(name) 函数：获取图表指定名称的属性值
 * </p>
 * <p>
 * jQuery
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

	public AttributeValueHtmlChartPlugin(String id, String attrName, HtmlChartPluginScriptObjectWriter pluginWriter,
			HtmlRenderContextScriptObjectWriter renderContextWriter, HtmlChartScriptObjectWriter chartWriter)
	{
		super(id, new Label(AttributeValueHtmlChartPlugin.class.getSimpleName()), null, pluginWriter,
				renderContextWriter, chartWriter);
		super.setRenderer(buildJsChartRenderer(attrName));
		this.attrName = attrName;
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
		return new StringJsChartRenderer(JsChartRenderer.CODE_TYPE_OBJECT, "{" + HtmlChartPlugin.HTML_NEW_LINE
		//
				+ "	render : function(chart)" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	{" + HtmlChartPlugin.HTML_NEW_LINE +
				//
				"		var element = chart.elementJquery();" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "		var value = chart.attrValue(" + StringUtil.toJavaScriptString(attrName) + ");"
				+ HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "		element.html(value);" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	}," + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	update : function(){}" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "}");
	}
}
