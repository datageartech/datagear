/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	private String attrName;

	public AttributeValueHtmlChartPlugin()
	{
		super();
	}

	public AttributeValueHtmlChartPlugin(String id, String attrName)
	{
		super();
		super.setId(id);
		super.setNameLabel(new Label(AttributeValueHtmlChartPlugin.class.getSimpleName()));
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
