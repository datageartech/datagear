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
import org.datagear.analysis.support.SimpleChartWidgetSource;
import org.datagear.analysis.support.TemplateDashboardWidgetResManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain HtmlTplDashboardWidget}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidgetTest
{
	private HtmlTplDashboardWidget<HtmlRenderContext> dashboardWidget;

	public HtmlTplDashboardWidgetTest() throws Exception
	{
		super();

		HtmlChartPlugin<HtmlRenderContext> chartPlugin = HtmlChartPluginTest.createHtmlChartPlugin();

		HtmlChartWidget<HtmlRenderContext> htmlChartWidget = new HtmlChartWidget<HtmlRenderContext>("chart-widget-01",
				chartPlugin, (DataSetFactory[]) null);

		TemplateDashboardWidgetResManager resManager = new TemplateDashboardWidgetResManager(
				"src/test/resources/org/datagear/analysis/support/html/htmlTplDashboardWidgets");

		SimpleChartWidgetSource chartWidgetSource = new SimpleChartWidgetSource(htmlChartWidget);

		HtmlTplDashboardWidgetRenderer<HtmlRenderContext> renderer = new HtmlTplDashboardWidgetRenderer<HtmlRenderContext>(
				"<script type='text/javascript' src='static/js/jquery.js'></script>", "/analysis/resource", resManager,
				chartWidgetSource);
		renderer.init();

		HtmlTplDashboardWidget<HtmlRenderContext> dashboardWidget = new HtmlTplDashboardWidget<HtmlRenderContext>(
				"widget01", "dashboard.ftl", renderer);

		this.dashboardWidget = dashboardWidget;
	}

	@Test
	public void renderTest()
	{
		StringWriter stringWriter = new StringWriter();
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(stringWriter);
		HtmlRenderAttributes.setRenderStyle(renderContext, RenderStyle.DARK);
		HtmlDashboard dashboard = this.dashboardWidget.render(renderContext);

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
