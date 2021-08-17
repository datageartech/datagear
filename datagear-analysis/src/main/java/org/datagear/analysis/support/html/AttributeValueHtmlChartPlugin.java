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
 * 它从{@linkplain ChartDefinition#getAttributes()}获取{@linkplain #getChartAttributeName()}的值，并将其作为图表内容渲染。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AttributeValueHtmlChartPlugin extends HtmlChartPlugin
{
	private static final long serialVersionUID = 1L;

	private String chartAttributeName;

	public AttributeValueHtmlChartPlugin()
	{
		super();
	}

	public AttributeValueHtmlChartPlugin(String id, String chartAttributeName)
	{
		super();
		super.setId(id);
		super.setNameLabel(new Label(AttributeValueHtmlChartPlugin.class.getSimpleName()));
		super.setChartRenderer(buildJsChartRenderer(chartAttributeName));
		this.chartAttributeName = chartAttributeName;
	}

	public String getChartAttributeName()
	{
		return chartAttributeName;
	}

	public void setChartAttributeName(String chartAttributeName)
	{
		this.chartAttributeName = chartAttributeName;
		super.setChartRenderer(buildJsChartRenderer(chartAttributeName));
	}

	protected StringJsChartRenderer buildJsChartRenderer(String valueChartParamName)
	{
		return new StringJsChartRenderer("{" + HtmlChartPlugin.HTML_NEW_LINE
		//
				+ "	render : function(chart)" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	{" + HtmlChartPlugin.HTML_NEW_LINE +
				//
				"		var element = document.getElementById(chart.elementId);" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "		var attributes = (chart." + HtmlChart.PROPERTY_CHART_ATTRIBUTES + " || {});"
				+ HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "		element.innerHTML=attributes['" + StringUtil.escapeJavaScriptStringValue(valueChartParamName)
				+ "'];" + HtmlChartPlugin.HTML_NEW_LINE

				//
				+ "	}," + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	update : function(){}" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "}");
	}
}
