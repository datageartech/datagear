/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * 仅渲染指定内容值的{@linkplain HtmlChartPlugin}。
 * <p>
 * 它使用{@code ChartDefinition#getProperty(String)}获取{@linkplain #getPropertyName()}的属性值，并将其作为图表内容渲染。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ValueHtmlChartPlugin<T extends HtmlRenderContext> extends HtmlChartPlugin<T>
{
	private String propertyName;

	public ValueHtmlChartPlugin()
	{
		super();
	}

	public ValueHtmlChartPlugin(String id, String propertyName)
	{
		super();
		super.setId(id);
		super.setNameLabel(new Label("ValueHtmlChartPlugin"));
		super.setChartRenderer(buildJsChartRenderer(propertyName));
		this.propertyName = propertyName;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public void setPropertyName(String propertyName)
	{
		this.propertyName = propertyName;
		super.setChartRenderer(buildJsChartRenderer(propertyName));
	}

	protected StringJsChartRenderer buildJsChartRenderer(String valueChartPropertyName)
	{
		return new StringJsChartRenderer("{" + HtmlChartPlugin.HTML_NEW_LINE
		//
				+ "	render : function(chart)" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	{" + HtmlChartPlugin.HTML_NEW_LINE +
				//
				"		var element = document.getElementById(chart.elementId);" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "		var properties = (chart." + HtmlChart.PROPERTY_PROPERTIES + " || {});"
				+ HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "		element.innerHTML=properties['"
				+ StringUtil.escapeJavaScriptStringValue(valueChartPropertyName) + "'];" + HtmlChartPlugin.HTML_NEW_LINE

				//
				+ "	}," + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	update : function(){}" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "}");
	}
}
