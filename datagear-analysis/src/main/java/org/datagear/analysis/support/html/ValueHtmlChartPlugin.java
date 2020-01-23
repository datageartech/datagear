/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.util.HashMap;
import java.util.Map;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.RenderException;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * 仅渲染指定内容值的{@linkplain HtmlChartPlugin}。
 * <p>
 * 它从{@code chartPropertyValues}中获取{@linkplain #getValueChartPropertyName()}的属性值，并将其作为图表内容渲染。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ValueHtmlChartPlugin<T extends HtmlRenderContext> extends HtmlChartPlugin<T>
{
	private String valueChartPropertyName;

	public ValueHtmlChartPlugin()
	{
		super();
	}

	public ValueHtmlChartPlugin(String id, String valueChartPropertyName)
	{
		super();
		super.setId(id);
		super.setNameLabel(new Label("ValueHtmlChartPlugin"));
		super.setChartRenderer(buildJsChartRenderer(valueChartPropertyName));
		this.valueChartPropertyName = valueChartPropertyName;
	}

	public String getValueChartPropertyName()
	{
		return valueChartPropertyName;
	}

	public void setValueChartPropertyName(String valueChartPropertyName)
	{
		this.valueChartPropertyName = valueChartPropertyName;
		super.setChartRenderer(buildJsChartRenderer(valueChartPropertyName));
	}

	@Override
	public JsChartRenderer getChartRenderer()
	{
		return super.getChartRenderer();
	}

	@Override
	public void setChartRenderer(JsChartRenderer chartRenderer)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public HtmlChart renderChart(T renderContext, Map<String, ?> chartPropertyValues, ChartDataSet... chartDataSets)
			throws RenderException
	{
		@SuppressWarnings("unchecked")
		Map<String, Object> myChartPropertyValues = (Map<String, Object>) chartPropertyValues;
		if (myChartPropertyValues == null)
			myChartPropertyValues = new HashMap<String, Object>();

		return super.renderChart(renderContext, myChartPropertyValues, chartDataSets);
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
				+ "		var propertyValues = (chart.propertyValues || {});" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "		element.innerHTML=propertyValues['"
				+ StringUtil.escapeJavaScriptStringValue(valueChartPropertyName) + "'];" + HtmlChartPlugin.HTML_NEW_LINE

				//
				+ "	}," + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "	update : function(){}" + HtmlChartPlugin.HTML_NEW_LINE
				//
				+ "}");
	}
}
