/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.StringWriter;

import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.DashboardWidgetResManager;
import org.datagear.analysis.support.SimpleChartWidgetSource;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain HtmlTplDashboardWidgetFmkRenderer}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidgetFmkRendererTest
{
	private HtmlTplDashboardWidgetFmkRenderer<HtmlRenderContext> renderer;

	public HtmlTplDashboardWidgetFmkRendererTest() throws Exception
	{
		super();

		HtmlChartPlugin<HtmlRenderContext> chartPlugin = HtmlChartPluginTest.createHtmlChartPlugin();

		HtmlChartWidget<HtmlRenderContext> htmlChartWidget = new HtmlChartWidget<HtmlRenderContext>("chart-widget-01",
				chartPlugin, (DataSetFactory[]) null);

		DashboardWidgetResManager resManager = new DashboardWidgetResManager(
				"src/test/resources/org/datagear/analysis/support/html/htmlTplDashboardWidgets/freemarker");

		SimpleChartWidgetSource chartWidgetSource = new SimpleChartWidgetSource(htmlChartWidget);

		this.renderer = new HtmlTplDashboardWidgetFmkRenderer<HtmlRenderContext>(resManager, chartWidgetSource);
		this.renderer.init();
	}

	@Test
	public void renderTest()
	{
		HtmlTplDashboardWidget<HtmlRenderContext> dashboardWidget = new HtmlTplDashboardWidget<HtmlRenderContext>(
				"widget01", "index.html", this.renderer);

		StringWriter stringWriter = new StringWriter();
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(stringWriter);
		HtmlRenderAttributes.setRenderStyle(renderContext, RenderStyle.DARK);
		HtmlDashboard dashboard = dashboardWidget.render(renderContext);

		String html = stringWriter.toString();

		Assert.assertEquals(6, dashboard.getCharts().size());
		Assert.assertTrue(html.contains(HtmlRenderAttributes.generateDashboardVarName(1)));
		Assert.assertTrue(html.contains(HtmlRenderAttributes.generateChartVarName(2)));
		Assert.assertTrue(html.contains(HtmlRenderAttributes.generateChartVarName(3)));
		Assert.assertTrue(html.contains("chart01"));
		Assert.assertTrue(html.contains(HtmlRenderAttributes.generateChartVarName(4)));
		Assert.assertTrue(html.contains("chart02"));
		Assert.assertTrue(html.contains(HtmlRenderAttributes.generateChartVarName(6)));

		System.out.println(stringWriter.toString());
	}
}
