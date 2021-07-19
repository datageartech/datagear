/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.FileTemplateDashboardWidgetResManager;
import org.datagear.analysis.support.SimpleChartWidgetSource;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.analysis.support.html.HtmlTplDashboardImport.ImportItem;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer.ChartInfo;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer.DashboardInfo;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.ExtContentHtmlTitleHandler;
import org.datagear.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain HtmlTplDashboardWidgetHtmlRenderer}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidgetHtmlRendererTest
{
	private HtmlTplDashboardWidgetHtmlRenderer renderer;

	protected static final String IMPORT_CONTENT_JQUERY = "<script type=\"text/javascript\" src=\"jquery.js\"></script>";

	protected static final String IMPORT_CONTENT_UTIL = "<script type=\"text/javascript\" src=\"util.js\"></script>";

	protected static final String IMPORT_CONTENT_THEME = "<link rel=\"stylesheet\" type=\"text/css\" href=\"theme.css\">";

	protected static final String IMPORT_CONTENT_STYLE = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">";

	public HtmlTplDashboardWidgetHtmlRendererTest() throws Exception
	{
		super();

		HtmlChartPlugin chartPlugin = HtmlChartPluginTest.createHtmlChartPlugin();

		HtmlChartWidget htmlChartWidget = new HtmlChartWidget("chart-widget-01", "chart-widget-01",
				ChartDefinition.EMPTY_CHART_DATA_SET, chartPlugin);

		TemplateDashboardWidgetResManager resManager = new FileTemplateDashboardWidgetResManager(
				"src/test/resources/org/datagear/analysis/support/html/htmlTplDashboardWidgets/html");

		SimpleChartWidgetSource chartWidgetSource = new SimpleChartWidgetSource(htmlChartWidget);

		List<ImportItem> dashboardImports = new ArrayList<>();
		dashboardImports.add(new ImportItem("jquery", IMPORT_CONTENT_JQUERY));
		dashboardImports.add(new ImportItem("util", IMPORT_CONTENT_UTIL));
		dashboardImports.add(new ImportItem("theme", IMPORT_CONTENT_THEME));
		dashboardImports.add(new ImportItem("style", IMPORT_CONTENT_STYLE));

		this.renderer = new HtmlTplDashboardWidgetHtmlRenderer(resManager, chartWidgetSource);
		this.renderer.setHtmlTplDashboardImport(new SimpleHtmlTplDashboardImport(dashboardImports));
	}

	@Test
	public void renderTest() throws Exception
	{
		HtmlTplDashboardWidget dashboardWidget = createHtmlTplDashboardWidget();

		RenderContext renderContext = new DefaultRenderContext();
		HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
		StringWriter out = new StringWriter();
		renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

		HtmlTplDashboard dashboard = dashboardWidget.render(renderContext);

		getHtmlWithPrint(out);

		Assert.assertEquals(6, dashboard.getCharts().size());
	}

	@Test
	public void renderHtmlTplDashboardTest() throws Exception
	{
		HtmlTplDashboardWidget dashboardWidget = createHtmlTplDashboardWidget();

		// 看板属性，双引号
		{
			String template = "<html dg-dashboard-var=\"myDashboard\" dg-dashboard-factory=\"myDashboardFactory\" "
					+ " dg-dashboard-import-exclude=\"jquery\"><head></head><body></body></html>";

			RenderContext renderContext = new DefaultRenderContext();
			HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
			StringWriter out = new StringWriter();
			renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

			HtmlTplDashboard dashboard = this.renderer.createHtmlTplDashboard(renderContext, dashboardWidget, template);

			DashboardInfo dashboardInfo = this.renderer.renderHtmlTplDashboard(renderContext, renderAttr, dashboard,
					IOUtil.getReader(template));

			String html = getHtmlWithPrint(out);

			Assert.assertEquals("myDashboard", dashboardInfo.getDashboardVar());
			Assert.assertEquals("myDashboardFactory", dashboardInfo.getDashboardFactoryVar());
			Assert.assertEquals("jquery", dashboardInfo.getImportExclude());
			Assert.assertFalse(html.contains(IMPORT_CONTENT_JQUERY));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_THEME));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_STYLE));
			Assert.assertTrue(html.contains("var DataGearDashboardTmp"));
			Assert.assertTrue(html.contains("myDashboardFactory.init(DataGearDashboardTmp);"));
			Assert.assertTrue(html.contains("window.myDashboard=DataGearDashboardTmp;"));
		}

		// 看板属性，无引号
		{
			String template = "<html dg-dashboard-var=myDashboard dg-dashboard-factory=myDashboardFactory "
					+ " dg-dashboard-import-exclude=jquery><head></head><body></body></html>";

			RenderContext renderContext = new DefaultRenderContext();
			HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
			StringWriter out = new StringWriter();
			renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

			HtmlTplDashboard dashboard = this.renderer.createHtmlTplDashboard(renderContext, dashboardWidget, template);

			DashboardInfo dashboardInfo = this.renderer.renderHtmlTplDashboard(renderContext, renderAttr, dashboard,
					IOUtil.getReader(template));

			String html = getHtmlWithPrint(out);

			Assert.assertEquals("myDashboard", dashboardInfo.getDashboardVar());
			Assert.assertEquals("myDashboardFactory", dashboardInfo.getDashboardFactoryVar());
			Assert.assertEquals("jquery", dashboardInfo.getImportExclude());
			Assert.assertFalse(html.contains(IMPORT_CONTENT_JQUERY));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_THEME));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_STYLE));
			Assert.assertTrue(html.contains("var DataGearDashboardTmp"));
			Assert.assertTrue(html.contains("myDashboardFactory.init(DataGearDashboardTmp);"));
			Assert.assertTrue(html.contains("window.myDashboard=DataGearDashboardTmp;"));
		}

		// 看板属性，单引号
		{
			String template = "<html dg-dashboard-var='myDashboard' dg-dashboard-factory='myDashboardFactory' "
					+ " dg-dashboard-import-exclude='jquery'><head></head><body></body></html>";

			RenderContext renderContext = new DefaultRenderContext();
			HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
			StringWriter out = new StringWriter();
			renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

			HtmlTplDashboard dashboard = this.renderer.createHtmlTplDashboard(renderContext, dashboardWidget, template);

			DashboardInfo dashboardInfo = this.renderer.renderHtmlTplDashboard(renderContext, renderAttr, dashboard,
					IOUtil.getReader(template));

			String html = getHtmlWithPrint(out);

			Assert.assertEquals("myDashboard", dashboardInfo.getDashboardVar());
			Assert.assertEquals("myDashboardFactory", dashboardInfo.getDashboardFactoryVar());
			Assert.assertEquals("jquery", dashboardInfo.getImportExclude());
			Assert.assertFalse(html.contains(IMPORT_CONTENT_JQUERY));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_THEME));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_STYLE));
			Assert.assertTrue(html.contains("var DataGearDashboardTmp"));
			Assert.assertTrue(html.contains("myDashboardFactory.init(DataGearDashboardTmp);"));
			Assert.assertTrue(html.contains("window.myDashboard=DataGearDashboardTmp;"));
		}

		// 看板属性，多个导入排除值
		{
			String template = "<html dg-dashboard-var='myDashboard' dg-dashboard-factory='myDashboardFactory' "
					+ " dg-dashboard-import-exclude='jquery,theme, style'><head></head><body></body></html>";

			RenderContext renderContext = new DefaultRenderContext();
			HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
			StringWriter out = new StringWriter();
			renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

			HtmlTplDashboard dashboard = this.renderer.createHtmlTplDashboard(renderContext, dashboardWidget, template);

			DashboardInfo dashboardInfo = this.renderer.renderHtmlTplDashboard(renderContext, renderAttr, dashboard,
					IOUtil.getReader(template));

			String html = getHtmlWithPrint(out);

			Assert.assertEquals("myDashboard", dashboardInfo.getDashboardVar());
			Assert.assertEquals("myDashboardFactory", dashboardInfo.getDashboardFactoryVar());
			Assert.assertEquals("jquery,theme, style", dashboardInfo.getImportExclude());
			Assert.assertFalse(html.contains(IMPORT_CONTENT_JQUERY));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			Assert.assertFalse(html.contains(IMPORT_CONTENT_THEME));
			Assert.assertFalse(html.contains(IMPORT_CONTENT_STYLE));
			Assert.assertTrue(html.contains("var DataGearDashboardTmp"));
			Assert.assertTrue(html.contains("myDashboardFactory.init(DataGearDashboardTmp);"));
			Assert.assertTrue(html.contains("window.myDashboard=DataGearDashboardTmp;"));
		}

		// 看板属性，默认
		{
			String template = "<html><head></head><body></body></html>";

			RenderContext renderContext = new DefaultRenderContext();
			HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
			StringWriter out = new StringWriter();
			renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

			HtmlTplDashboard dashboard = this.renderer.createHtmlTplDashboard(renderContext, dashboardWidget, template);

			DashboardInfo dashboardInfo = this.renderer.renderHtmlTplDashboard(renderContext, renderAttr, dashboard,
					IOUtil.getReader(template));

			String html = getHtmlWithPrint(out);

			Assert.assertNull(dashboardInfo.getDashboardVar());
			Assert.assertNull(dashboardInfo.getDashboardFactoryVar());
			Assert.assertNull(dashboardInfo.getImportExclude());
			Assert.assertTrue(html.contains(IMPORT_CONTENT_JQUERY));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_THEME));
			Assert.assertTrue(html.contains(IMPORT_CONTENT_STYLE));
			Assert.assertTrue(html.contains("var DataGearDashboardTmp"));
			Assert.assertTrue(html.contains("dashboardFactory.init(DataGearDashboardTmp);"));
			Assert.assertTrue(html.contains("window.dashboard=DataGearDashboardTmp;"));
		}

		// 图表属性
		{
			String template = "<html><head></head><body>" + HtmlChartPlugin.HTML_NEW_LINE
					+ "<div id=\"element_1\" dg-chart-widget=\"chartwidget_1\"></div>" + HtmlChartPlugin.HTML_NEW_LINE
					+ "<div id='element_2' dg-chart-widget='chartwidget_2'></div>" + HtmlChartPlugin.HTML_NEW_LINE
					+ "<div id=element_3 dg-chart-widget=chartwidget_3></div>" + HtmlChartPlugin.HTML_NEW_LINE
					+ "<div sdf abc def 12345677788 // >"
					//
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div   id=element_4    dg-chart-widget=chartwidget_4    ></div>"
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div   id  =  element_5    dg-chart-widget=  chartwidget_5 />"
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div   id=element_6    dg-chart-widget=chartwidget_6  />"
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div   id=element_7    dg-chart-widget=chartwidget_7  /  >"
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div     dg-chart-widget=chartwidget_8    /  >"
					//
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div     dg-chart-widget=chartwidget_9/>"
					//
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div     dg-chart-widget='' />"
					//
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div     dg-chart-widget=\"\"  />"
					//
					+ HtmlChartPlugin.HTML_NEW_LINE + "</body></html>";

			RenderContext renderContext = new DefaultRenderContext();
			HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
			StringWriter out = new StringWriter();
			renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

			HtmlTplDashboard dashboard = this.renderer.createHtmlTplDashboard(renderContext, dashboardWidget, template);

			DashboardInfo dashboardInfo = this.renderer.renderHtmlTplDashboard(renderContext, renderAttr, dashboard,
					IOUtil.getReader(template));

			String html = getHtmlWithPrint(out);

			List<ChartInfo> chartInfos = dashboardInfo.getChartInfos();
			Assert.assertEquals(9, chartInfos.size());

			for (int i = 0; i < 7; i++)
			{
				ChartInfo chartInfo = chartInfos.get(i);
				Assert.assertEquals("element_" + (i + 1), chartInfo.getElementId());
				Assert.assertEquals("chartwidget_" + (i + 1), chartInfo.getWidgetId());
			}

			Assert.assertTrue(html.contains("<html><head>"));
			Assert.assertTrue(html.contains("</head><body>"));
			Assert.assertTrue(html.contains("<div id=\"element_1\" dg-chart-widget=\"chartwidget_1\"></div>"));
			Assert.assertTrue(html.contains("<div id='element_2' dg-chart-widget='chartwidget_2'></div>"));
			Assert.assertTrue(html.contains("<div id=element_3 dg-chart-widget=chartwidget_3></div>"));
			Assert.assertTrue(html.contains("<div sdf abc def 12345677788 // >"));
			Assert.assertTrue(html.contains("<div   id=element_4    dg-chart-widget=chartwidget_4    ></div>"));
			Assert.assertTrue(html.contains("<div   id  =  element_5    dg-chart-widget=  chartwidget_5 />"));
			Assert.assertTrue(html.contains("<div   id=element_6    dg-chart-widget=chartwidget_6  />"));
			Assert.assertTrue(html.contains("<div   id=element_7    dg-chart-widget=chartwidget_7  /  >"));
			Assert.assertTrue(
					html.contains("<div     dg-chart-widget=chartwidget_8 id=\"DataGearChartElement7\"     /  >"));
			Assert.assertTrue(html.contains("<div     dg-chart-widget=chartwidget_9 id=\"DataGearChartElement8\" />"));
			Assert.assertTrue(html.contains("<div     dg-chart-widget='' />"));
			Assert.assertTrue(html.contains("<div     dg-chart-widget=\"\"  />"));
			Assert.assertTrue(html.contains("</body></html>"));
		}

		// 处理标题
		{
			String template = "<html><head></head><body></body></html>";

			RenderContext renderContext = new DefaultRenderContext();
			HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
			StringWriter out = new StringWriter();
			renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

			ExtContentHtmlTitleHandler htmlTitleHandler = new ExtContentHtmlTitleHandler("-suffix");
			renderAttr.setHtmlTitleHandler(renderContext, htmlTitleHandler);

			HtmlTplDashboard dashboard = this.renderer.createHtmlTplDashboard(renderContext, dashboardWidget, template);
			this.renderer.renderHtmlTplDashboard(renderContext, renderAttr, dashboard, IOUtil.getReader(template));

			String html = getHtmlWithPrint(out);

			Assert.assertTrue(html.contains("<title>-suffix</title></head>"));
		}

		{
			String template = "<html><head><title>abc</title></head><body><title>sdf</title></body></html>";

			RenderContext renderContext = new DefaultRenderContext();
			HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
			StringWriter out = new StringWriter();
			renderAttr.inflate(renderContext, out, new WebContext(""), SimpleDashboardThemeSource.THEME_LIGHT);

			ExtContentHtmlTitleHandler htmlTitleHandler = new ExtContentHtmlTitleHandler("-suffix");
			renderAttr.setHtmlTitleHandler(renderContext, htmlTitleHandler);

			HtmlTplDashboard dashboard = this.renderer.createHtmlTplDashboard(renderContext, dashboardWidget, template);
			this.renderer.renderHtmlTplDashboard(renderContext, renderAttr, dashboard, IOUtil.getReader(template));

			String html = getHtmlWithPrint(out);

			Assert.assertTrue(html.contains("<title>abc-suffix</title></head>"));
			Assert.assertTrue(html.contains("<title>sdf</title>"));
		}
	}

	protected HtmlTplDashboardWidget createHtmlTplDashboardWidget()
	{
		HtmlTplDashboardWidget dashboardWidget = new HtmlTplDashboardWidget("widget01", "index.html", this.renderer);

		return dashboardWidget;
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
