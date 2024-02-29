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
