/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.ChartDefinition;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * 仅渲染指定内容值的{@linkplain HtmlChartPlugin}。
 * <p>
 * 它使用{@linkplain ChartDefinition#getChartParamValue(String)}获取{@linkplain #getPropertyName()}的值，并将其作为图表内容渲染。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ValueHtmlChartPlugin<T extends HtmlRenderContext> extends HtmlChartPlugin<T>
{
	private String chartParamName;

	public ValueHtmlChartPlugin()
	{
		super();
	}

	public ValueHtmlChartPlugin(String id, String chartParamName)
	{
		super();
		super.setId(id);
		super.setNameLabel(new Label("ValueHtmlChartPlugin"));
		super.setChartRenderer(buildJsChartRenderer(chartParamName));
		this.chartParamName = chartParamName;
	}

	public String getChartParamName()
	{
		return chartParamName;
	}

	public void setChartParamName(String chartParamName)
	{
		this.chartParamName = chartParamName;
		super.setChartRenderer(buildJsChartRenderer(chartParamName));
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
				+ "		var pvs = (chart." + HtmlChart.PROPERTY_CHART_PARAM_VALUES + " || {});"
				+ HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "		element.innerHTML=pvs['" + StringUtil.escapeJavaScriptStringValue(valueChartParamName) + "'];"
				+ HtmlChartPlugin.HTML_NEW_LINE

				//
				+ "	}," + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	update : function(){}" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "}");
	}
}
