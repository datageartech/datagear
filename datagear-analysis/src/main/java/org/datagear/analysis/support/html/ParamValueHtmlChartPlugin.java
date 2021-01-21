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
 * 仅渲染指定图表参数值的{@linkplain HtmlChartPlugin}。
 * <p>
 * 它从{@linkplain ChartDefinition#getParamValues()}获取{@linkplain #getChartParamName()}的值，并将其作为图表内容渲染。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ParamValueHtmlChartPlugin extends HtmlChartPlugin
{
	private String chartParamName;

	public ParamValueHtmlChartPlugin()
	{
		super();
	}

	public ParamValueHtmlChartPlugin(String id, String chartParamName)
	{
		super();
		super.setId(id);
		super.setNameLabel(new Label(ParamValueHtmlChartPlugin.class.getSimpleName()));
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
