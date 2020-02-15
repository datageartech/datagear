/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.StringWriter;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.FileTemplateDashboardWidgetResManager;
import org.datagear.analysis.support.SimpleChartWidgetSource;
import org.datagear.analysis.support.html.HtmlRenderContext.WebContext;
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
				"chart-widget-01", ChartDefinition.EMPTY_CHART_DATA_SET, chartPlugin);

		FileTemplateDashboardWidgetResManager resManager = new FileTemplateDashboardWidgetResManager(
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
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(new WebContext("", ""), stringWriter);
		HtmlRenderAttributes.setRenderStyle(renderContext, RenderStyle.DARK);
		HtmlDashboard dashboard = dashboardWidget.render(renderContext);

		getHtmlWithPrint(stringWriter);

		Assert.assertEquals(6, dashboard.getCharts().size());
	}

	protected String getHtmlWithPrint(StringWriter out)
	{
		String html = out.toString();

		System.out.println(html);
		System.out.println("");
		System.out.println("-----------------------");
		System.out.println("");

		return html;
	}
}
