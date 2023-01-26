/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.datagear.analysis.ChartDefinition;
import org.junit.Test;

/**
 * {@linkplain HtmlChartWidgetJsonRenderer}单元测试用例。
 * 
 * @author datagear@163.com
 */
public class HtmlChartWidgetJsonRendererTest
{
	protected static final String HTML_CHART_WIDGET_ID_01 = "chart-widget-01";

	protected static final String HTML_CHART_WIDGET_ID_02 = "chart-widget-01";
	
	private HtmlChartWidget htmlChartWidget01;
	private HtmlChartWidget htmlChartWidget02;
	private HtmlChartWidgetJsonRenderer htmlChartWidgetJsonRenderer;
	
	public HtmlChartWidgetJsonRendererTest() throws Exception
	{
		super();

		HtmlChartPlugin chartPlugin = HtmlChartPluginTest.createHtmlChartPlugin();

		this.htmlChartWidget01 = new HtmlChartWidget(HTML_CHART_WIDGET_ID_01, "chart-widget-01-name",
				ChartDefinition.EMPTY_CHART_DATA_SET, chartPlugin);

		this.htmlChartWidget02 = new HtmlChartWidget(HTML_CHART_WIDGET_ID_02, "chart-widget-02-name",
				ChartDefinition.EMPTY_CHART_DATA_SET, chartPlugin);
		
		this.htmlChartWidgetJsonRenderer = new HtmlChartWidgetJsonRenderer();
	}

	@Test
	public void renderTest() throws Exception
	{
		StringWriter out = new StringWriter();
		this.htmlChartWidgetJsonRenderer.render(out, htmlChartWidget01);
		String json = out.toString();
		
		assertTrue(json.startsWith("{"));
		assertTrue(json.contains("\"name\":\"chart-widget-01-name\""));
		assertTrue(json.contains("\"chartDataSets\":[]"));
		assertTrue(json.contains("\"attrValues\":{\"DG_CHART_WIDGET\":{\"id\":\""+HTML_CHART_WIDGET_ID_01+"\"}}"));
		assertTrue(json.contains("\"updateInterval\":-1"));
		assertTrue(json.contains("\"plugin\":{\"id\": \"pie-chart\"}"));
		assertTrue(json.contains("\"renderContext\":{}"));
		assertTrue(json.contains("\"elementId\":\"\""));
		assertTrue(json.contains("\"varName\":\"\""));
		assertTrue(json.endsWith("}"));
	}

	@Test
	public void renderTest_array() throws Exception
	{
		StringWriter out = new StringWriter();
		this.htmlChartWidgetJsonRenderer.render(out, htmlChartWidget01, htmlChartWidget02);
		String json = out.toString();
		
		assertTrue(json.startsWith("["));
		assertTrue(json.contains("\"name\":\"chart-widget-01-name\""));
		assertTrue(json.contains("\"attrValues\":{\"DG_CHART_WIDGET\":{\"id\":\""+HTML_CHART_WIDGET_ID_01+"\"}}"));
		assertTrue(json.contains("\"name\":\"chart-widget-02-name\""));
		assertTrue(json.contains("\"attrValues\":{\"DG_CHART_WIDGET\":{\"id\":\""+HTML_CHART_WIDGET_ID_02+"\"}}"));
		assertTrue(json.endsWith("]"));
	}
}
