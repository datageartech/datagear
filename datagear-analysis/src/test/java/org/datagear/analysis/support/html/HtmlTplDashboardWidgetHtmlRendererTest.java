/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.DefaultRenderContext;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.FileTplDashboardWidgetResManager;
import org.datagear.analysis.support.SimpleChartWidgetSource;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer.DashboardFilterContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer.TplChartMeta;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer.TplDashboardMeta;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.junit.Test;

/**
 * {@linkplain HtmlTplDashboardWidgetHtmlRenderer}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
@SuppressWarnings("unused")
public class HtmlTplDashboardWidgetHtmlRendererTest
{
	protected static final String TEMPLATE_NAME = "index.html";

	protected static final String IMPORT_CONTENT_JQUERY = "<script type=\"text/javascript\" src=\"jquery.js\"></script>";

	protected static final String IMPORT_CONTENT_UTIL = "<script type=\"text/javascript\" src=\"util.js\"></script>";

	protected static final String IMPORT_CONTENT_THEME = "<link rel=\"stylesheet\" type=\"text/css\" href=\"theme.css\">";

	protected static final String IMPORT_CONTENT_STYLE = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">";

	protected static final String DASHBOARD_ID = IDUtil.uuid();

	protected static final String HTML_CHART_WIDGET_ID_01 = "chart-widget-01";

	protected static final String HTML_CHART_WIDGET_ID_02 = "chart-widget-01";
	
	private HtmlChartWidget htmlChartWidget01;
	private HtmlChartWidget htmlChartWidget02;
	private TestFixedIdHtmlTplDashboardWidgetHtmlRenderer renderer;
	private TplDashboardWidgetResManager resManager;
	private HtmlTitleHandler htmlTitleHandler = new DefaultHtmlTitleHandler("-suffix", "generated");
	
	public HtmlTplDashboardWidgetHtmlRendererTest() throws Exception
	{
		super();

		HtmlChartPlugin chartPlugin = HtmlChartPluginTest.createHtmlChartPlugin();

		this.htmlChartWidget01 = new HtmlChartWidget(HTML_CHART_WIDGET_ID_01, "chart-widget-01",
				ChartDefinition.EMPTY_CHART_DATA_SET, chartPlugin);

		this.htmlChartWidget02 = new HtmlChartWidget(HTML_CHART_WIDGET_ID_02, "chart-widget-02",
				ChartDefinition.EMPTY_CHART_DATA_SET, chartPlugin);

		this.resManager = new FileTplDashboardWidgetResManager(
				"src/test/resources/org/datagear/analysis/support/html/htmlTplDashboardWidgets/html");

		this.renderer = new TestFixedIdHtmlTplDashboardWidgetHtmlRenderer(new SimpleChartWidgetSource(this.htmlChartWidget01, this.htmlChartWidget02));
	}

	@Test
	public void renderTest() throws Exception
	{
		HtmlTplDashboardWidgetHtmlRenderer renderer = new HtmlTplDashboardWidgetHtmlRenderer(
				new SimpleChartWidgetSource(this.htmlChartWidget01, this.htmlChartWidget02));
		HtmlTplDashboardWidget dashboardWidget = createHtmlTplDashboardWidget(renderer);

		HtmlTplDashboard dashboard1 = null;
		HtmlTplDashboard dashboard2 = null;
		
		{
			HtmlTplDashboardRenderContext renderContext = buildRenderContext();
			dashboard1 = dashboardWidget.render(renderContext);
	
			assertEquals(6, dashboard1.getCharts().size());
		}
		
		{
			HtmlTplDashboardRenderContext renderContext = buildRenderContext();
			dashboard2 = dashboardWidget.render(renderContext);
			
			assertEquals(6, dashboard2.getCharts().size());
		}
		
		assertNotEquals(dashboard1.getId(), dashboard2.getId());
	}

	@Test
	public void renderDashboardTest() throws Throwable
	{
		HtmlTplDashboardWidget dashboardWidget = createHtmlTplDashboardWidget();

		// 看板属性，双引号
		{
			String template = "<html dg-dashboard-var=\"myDashboard\" dg-dashboard-factory=\"myDashboardFactory\" "
					+ " dg-dashboard-unimport=\"jquery\"><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertEquals("myDashboard", dashboardMeta.getDashboardVar());
			assertEquals("myDashboardFactory", dashboardMeta.getDashboardFactoryVar());
			assertEquals("jquery", dashboardMeta.getDashboardUnimport());
			assertFalse(html.contains(IMPORT_CONTENT_JQUERY));
			assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			assertTrue(html.contains(IMPORT_CONTENT_THEME));
			assertTrue(html.contains(IMPORT_CONTENT_STYLE));
			assertTrue(html.contains("var datagearDashboardTmp="));
			assertTrue(html.contains("myDashboardFactory.init(datagearDashboardTmp);"));
			assertTrue(html.contains(this.renderer.getLocalGlobalVarName() + ".myDashboard=datagearDashboardTmp;"));
			assertTrue(html.contains("datagearDashboardTmp.init();"));
			assertTrue(html.contains("datagearDashboardTmp.render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 看板属性，无引号
		{
			String template = "<html dg-dashboard-var=myDashboard dg-dashboard-factory=myDashboardFactory "
					+ " dg-dashboard-unimport=jquery><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertEquals("myDashboard", dashboardMeta.getDashboardVar());
			assertEquals("myDashboardFactory", dashboardMeta.getDashboardFactoryVar());
			assertEquals("jquery", dashboardMeta.getDashboardUnimport());
			assertFalse(html.contains(IMPORT_CONTENT_JQUERY));
			assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			assertTrue(html.contains(IMPORT_CONTENT_THEME));
			assertTrue(html.contains(IMPORT_CONTENT_STYLE));
			assertTrue(html.contains("var datagearDashboardTmp"));
			assertTrue(html.contains("myDashboardFactory.init(datagearDashboardTmp);"));
			assertTrue(html.contains(this.renderer.getLocalGlobalVarName() + ".myDashboard=datagearDashboardTmp;"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 看板属性，单引号
		{
			String template = "<html dg-dashboard-var='myDashboard' dg-dashboard-factory='myDashboardFactory' "
					+ " dg-dashboard-unimport='jquery'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertEquals("myDashboard", dashboardMeta.getDashboardVar());
			assertEquals("myDashboardFactory", dashboardMeta.getDashboardFactoryVar());
			assertEquals("jquery", dashboardMeta.getDashboardUnimport());
			assertFalse(html.contains(IMPORT_CONTENT_JQUERY));
			assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			assertTrue(html.contains(IMPORT_CONTENT_THEME));
			assertTrue(html.contains(IMPORT_CONTENT_STYLE));
			assertTrue(html.contains("var datagearDashboardTmp"));
			assertTrue(html.contains("myDashboardFactory.init(datagearDashboardTmp);"));
			assertTrue(html.contains(this.renderer.getLocalGlobalVarName() + ".myDashboard=datagearDashboardTmp;"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 看板属性，多个导入排除值
		{
			String template = "<html dg-dashboard-var='myDashboard' dg-dashboard-factory='myDashboardFactory' "
					+ " dg-dashboard-unimport='jquery,theme, style'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertEquals("myDashboard", dashboardMeta.getDashboardVar());
			assertEquals("myDashboardFactory", dashboardMeta.getDashboardFactoryVar());
			assertEquals("jquery,theme, style", dashboardMeta.getDashboardUnimport());
			assertFalse(html.contains(IMPORT_CONTENT_JQUERY));
			assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			assertFalse(html.contains(IMPORT_CONTENT_THEME));
			assertFalse(html.contains(IMPORT_CONTENT_STYLE));
			assertTrue(html.contains("var datagearDashboardTmp"));
			assertTrue(html.contains("myDashboardFactory.init(datagearDashboardTmp);"));
			assertTrue(html.contains(this.renderer.getLocalGlobalVarName() + ".myDashboard=datagearDashboardTmp;"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 看板属性，默认
		{
			String template = "<html><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertNull(dashboardMeta.getDashboardVar());
			assertNull(dashboardMeta.getDashboardFactoryVar());
			assertNull(dashboardMeta.getDashboardUnimport());
			assertTrue(html.contains(IMPORT_CONTENT_JQUERY));
			assertTrue(html.contains(IMPORT_CONTENT_UTIL));
			assertTrue(html.contains(IMPORT_CONTENT_THEME));
			assertTrue(html.contains(IMPORT_CONTENT_STYLE));
			assertTrue(html.contains("var datagearDashboardTmp"));
			assertTrue(html.contains("dashboardFactory.init(datagearDashboardTmp);"));
			assertTrue(html.contains(this.renderer.getLocalGlobalVarName() + ".dashboard=datagearDashboardTmp;"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
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
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div     dg-chart-widget=\""+HTML_CHART_WIDGET_ID_01+"\"  />"
					//
					+ HtmlChartPlugin.HTML_NEW_LINE + "<div     dg-chart-widget=\""+HTML_CHART_WIDGET_ID_02+"\"  />"
					//
					+ HtmlChartPlugin.HTML_NEW_LINE + "</body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			List<TplChartMeta> chartMetas = dashboardMeta.getTplChartMetas();
			assertEquals(11, chartMetas.size());

			for (int i = 0; i < 7; i++)
			{
				TplChartMeta chartMeta = chartMetas.get(i);
				assertEquals("element_" + (i + 1), chartMeta.getElementId());
				assertEquals("chartwidget_" + (i + 1), chartMeta.getWidgetId());
			}

			assertTrue(html.contains("<html><head>"));
			assertTrue(html.contains("</head><body>"));
			assertTrue(html.contains("<div id=\"element_1\" dg-chart-widget=\"chartwidget_1\"></div>"));
			assertTrue(html.contains("<div id='element_2' dg-chart-widget='chartwidget_2'></div>"));
			assertTrue(html.contains("<div id=element_3 dg-chart-widget=chartwidget_3></div>"));
			assertTrue(html.contains("<div sdf abc def 12345677788 // >"));
			assertTrue(html.contains("<div   id=element_4    dg-chart-widget=chartwidget_4    ></div>"));
			assertTrue(html.contains("<div   id  =  element_5    dg-chart-widget=  chartwidget_5 />"));
			assertTrue(html.contains("<div   id=element_6    dg-chart-widget=chartwidget_6  />"));
			assertTrue(html.contains("<div   id=element_7    dg-chart-widget=chartwidget_7  /  >"));
			assertTrue(
					html.contains("<div     dg-chart-widget=chartwidget_8    /   id=\"datagearchartele7\" >"));
			assertTrue(html.contains("<div     dg-chart-widget=chartwidget_9 id=\"datagearchartele8\" />"));
			assertTrue(html.contains("<div     dg-chart-widget='' />"));
			assertTrue(html.contains("<div     dg-chart-widget=\"\"  />"));
			assertTrue(html.contains("</body></html>"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 处理标题
		{
			String template = "<html><head><title>abc</title></head><body><title>sdf</title></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			renderContext.setHtmlTitleHandler(this.htmlTitleHandler);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains("<title>abc-suffix</title></head>"));
			assertTrue(html.contains("<title>sdf</title>"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 处理标题：没有<title></title>标签
		{
			String template = "<html><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			renderContext.setHtmlTitleHandler(this.htmlTitleHandler);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains("<title>generated</title></head>"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 处理标题：没有<title></title>标签
		{
			String template = "<html><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			renderContext.setHtmlTitleHandler(this.htmlTitleHandler);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains("<title>generated</title></head>"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 处理标题：<title/>
		{
			String template = "<html><head><title/></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			renderContext.setHtmlTitleHandler(this.htmlTitleHandler);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains("<title/><title>generated</title></head>"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 处理标题：没有</title>
		{
			String template = "<html><head><title></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			renderContext.setHtmlTitleHandler(this.htmlTitleHandler);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains("<title><title>generated</title></head>"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 没有<head></head>
		{
			String template = "<html><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains("<html><body>" + this.renderer.getNewLine() + "<style"));
			assertTrue(html.contains("</script>" + this.renderer.getNewLine() + "</body></html>"));
			assertEquals(1, countOf(html, "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">"));
			assertEquals(1, countOf(html, "datagearDashboardTmp.render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// 没有<body></body>
		{
			String template = "<html><head></head></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains("<html><head>" + this.renderer.getNewLine() + "<style"));
			assertTrue(html.contains(
					"<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"></head></html><script type=\"text/javascript\">"));
			assertEquals(1, countOf(html, "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">"));
			assertEquals(1, countOf(html, "datagearDashboardTmp.render();"));
			assertTrue(html.endsWith("</script>" + renderer.getNewLine()));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		// <html></html>
		{
			String template = "<html></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains("</html>" + this.renderer.getNewLine() + "<style"));
			assertTrue(html.contains(
					"<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"><script type=\"text/javascript\">"));
			assertEquals(1, countOf(html, "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">"));
			assertEquals(1, countOf(html, "datagearDashboardTmp.render();"));
			assertTrue(html.endsWith("</script>" + renderer.getNewLine()));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
	}
	
	@Test
	public void renderDashboardTestForLoadableChartWidgets() throws Throwable
	{
		HtmlTplDashboardWidget dashboardWidget = createHtmlTplDashboardWidget();

		{
			String template = "<html><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertNull(dashboard.getLoadableChartWidgets());
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		{
			String template = "<html dg-loadable-chart-widgets=\"all\"><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboard.getLoadableChartWidgets().isPatternAll());
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		{
			String template = "<html dg-loadable-chart-widgets='none'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboard.getLoadableChartWidgets().isPatternNone());
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		{
			String template = "<html dg-loadable-chart-widgets='permitted'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboard.getLoadableChartWidgets().isPatternPermitted());
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		{
			String template = "<html dg-loadable-chart-widgets='a-widget-id'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboard.getLoadableChartWidgets().isPatternList());
			assertTrue(dashboard.getLoadableChartWidgets().getChartWidgetIds().size() == 1);
			assertTrue(dashboard.getLoadableChartWidgets().inList("a-widget-id"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		{
			String template = "<html dg-loadable-chart-widgets='widget-id-0,widget-id-1'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboard.getLoadableChartWidgets().isPatternList());
			assertTrue(dashboard.getLoadableChartWidgets().getChartWidgetIds().size() == 2);
			assertTrue(dashboard.getLoadableChartWidgets().inList("widget-id-0"));
			assertTrue(dashboard.getLoadableChartWidgets().inList("widget-id-1"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		{
			String template = "<html dg-loadable-chart-widgets='widget-id-0, widget-id-1 , widget-id-2 '><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboard.getLoadableChartWidgets().isPatternList());
			assertTrue(dashboard.getLoadableChartWidgets().getChartWidgetIds().size() == 3);
			assertTrue(dashboard.getLoadableChartWidgets().inList("widget-id-0"));
			assertTrue(dashboard.getLoadableChartWidgets().inList("widget-id-1"));
			assertTrue(dashboard.getLoadableChartWidgets().inList("widget-id-2"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void renderDashboard_dg_dashboard_auto_render() throws Throwable
	{
		HtmlTplDashboardWidget dashboardWidget = createHtmlTplDashboardWidget();

		{
			String template = "<html><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboardMeta.isDashboardAutoRender());
			assertTrue(html.contains(dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html dg-dashboard-auto-render='true'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboardMeta.isDashboardAutoRender());
			assertTrue(html.contains(dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html dg-dashboard-auto-render=''><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboardMeta.isDashboardAutoRender());
			assertTrue(html.contains(dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html dg-dashboard-auto-render><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboardMeta.isDashboardAutoRender());
			assertTrue(html.contains(dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html dg-dashboard-auto-render='a'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(dashboardMeta.isDashboardAutoRender());
			assertTrue(html.contains(dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html dg-dashboard-auto-render='false'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertFalse(dashboardMeta.isDashboardAutoRender());
			assertFalse(html.contains(dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html dg-dashboard-auto-render='FaLse'><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertFalse(dashboardMeta.isDashboardAutoRender());
			assertFalse(html.contains(dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
	}

	@Test
	public void renderDashboard_dg_dashboard_code() throws Throwable
	{
		HtmlTplDashboardWidget dashboardWidget = createHtmlTplDashboardWidget();

		{
			String template = "<html dg-dashboard-code><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertTrue(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		{
			String template = "<html dg-dashboard-code=\"instance\"><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertFalse(html.contains(dashboard.getVarName() + ".init();"));
			assertFalse(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		{
			String template = "<html dg-dashboard-code=\"init\"><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertFalse(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		{
			String template = "<html dg-dashboard-code=\"render\"><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertTrue(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		{
			String template = "<html dg-dashboard-code=\"abc\"><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertTrue(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		{
			String template = "<html dg-dashboard-code=\"render\" dg-dashboard-auto-render=\"false\"><head></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertTrue(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		{
			String template = "<html dg-dashboard-code=\"instance\"><head></head><body><script dg-dashboard-code=\"render\"></script></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertTrue(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		{
			String template = "<html><head></head><body><script dg-dashboard-code></script></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
			
			int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code>") + "<script dg-dashboard-code>".length();
			int scriptCloseTagIdx = html.indexOf("</script></body>");
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);
			
			assertTrue(scriptStartTagEndIdx > 0);
			assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
			assertTrue(dashboardRenderCodeIdx > scriptStartTagEndIdx);
			assertTrue(dashboardRenderCodeIdx < scriptCloseTagIdx);
			assertTrue(html.indexOf("<script>", scriptStartTagEndIdx) < 0);
			assertEquals(scriptCloseTagIdx, html.indexOf("</script>", scriptStartTagEndIdx));

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertTrue(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html><head></head><body><script dg-dashboard-code=\"render\"></script></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
			
			int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"render\">") + "<script dg-dashboard-code=\"render\">".length();
			int scriptCloseTagIdx = html.indexOf("</script></body>");
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

			assertTrue(scriptStartTagEndIdx > 0);
			assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
			assertTrue(dashboardRenderCodeIdx > scriptStartTagEndIdx);
			assertTrue(dashboardRenderCodeIdx < scriptCloseTagIdx);
			assertTrue(html.indexOf("<script>", scriptStartTagEndIdx) < 0);
			assertEquals(scriptCloseTagIdx, html.indexOf("</script>", scriptStartTagEndIdx));

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertTrue(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html><head></head><body><script dg-dashboard-code=\"init\"></script></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardInitCode = this.renderer.getDashboardFactoryVarElseDft(dashboardMeta.getDashboardFactoryVar()) + ".init("+dashboard.getVarName()+");";
			String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
			
			int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"init\">") + "<script dg-dashboard-code=\"init\">".length();
			int scriptCloseTagIdx = html.indexOf("</script></body>");
			int dashboardInitCodeIdx = html.indexOf(dashboardInitCode);
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

			assertTrue(scriptStartTagEndIdx > 0);
			assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
			assertTrue(dashboardInitCodeIdx > scriptStartTagEndIdx);
			assertTrue(dashboardInitCodeIdx < scriptCloseTagIdx);
			assertTrue(dashboardRenderCodeIdx < 0);
			assertTrue(html.indexOf("<script>", scriptStartTagEndIdx) < 0);
			assertEquals(scriptCloseTagIdx, html.indexOf("</script>", scriptStartTagEndIdx));

			assertTrue(html.contains(dashboard.getVarName() + ".init();"));
			assertFalse(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html><head></head><body><script dg-dashboard-code=\"instance\"></script></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardInitCode = this.renderer.getDashboardFactoryVarElseDft(
					dashboardMeta.getDashboardFactoryVar()) + ".init(" + dashboard.getVarName() + ");";
			String dashboardRenderCode = dashboard.getVarName() + "." + this.renderer.getDashboardRenderFuncName()
					+ "();";

			int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"instance\">")
					+ "<script dg-dashboard-code=\"instance\">".length();
			int scriptCloseTagIdx = html.indexOf("</script></body>");
			int dashboardInitCodeIdx = html.indexOf(dashboardInitCode);
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

			assertTrue(scriptStartTagEndIdx > 0);
			assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
			assertTrue(dashboardInitCodeIdx > scriptStartTagEndIdx);
			assertTrue(dashboardInitCodeIdx < scriptCloseTagIdx);
			assertTrue(dashboardRenderCodeIdx < 0);
			assertTrue(html.indexOf("<script>", scriptStartTagEndIdx) < 0);
			assertEquals(scriptCloseTagIdx, html.indexOf("</script>", scriptStartTagEndIdx));

			assertFalse(html.contains(dashboard.getVarName() + ".init();"));
			assertFalse(html.contains(dashboard.getVarName() + ".render();"));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}

		{
			String template = "<html><head></head><body><script dg-dashboard-code=\"abc\"></script></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
			
			int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"abc\">") + "<script dg-dashboard-code=\"abc\">".length();
			int scriptCloseTagIdx = html.indexOf("</script></body>");
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

			assertTrue(scriptStartTagEndIdx > 0);
			assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
			assertTrue(dashboardRenderCodeIdx > scriptStartTagEndIdx);
			assertTrue(dashboardRenderCodeIdx < scriptCloseTagIdx);
			assertTrue(html.indexOf("<script>", scriptStartTagEndIdx) < 0);
			assertEquals(scriptCloseTagIdx, html.indexOf("</script>", scriptStartTagEndIdx));
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html><head></head><body><script dg-dashboard-code=\"abc\" /></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
			
			int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"abc\" />") + "<script dg-dashboard-code=\"abc\" />".length();
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);
			int writeScriptTagIdx = html.indexOf("<script type=\"text/javascript\">", scriptStartTagEndIdx);
			int writeScriptTagEndIdx = html.indexOf("</script>", writeScriptTagIdx);

			assertTrue(scriptStartTagEndIdx > 0);
			assertTrue(dashboardRenderCodeIdx > writeScriptTagIdx);
			assertTrue(dashboardRenderCodeIdx < writeScriptTagEndIdx);
			assertTrue(writeScriptTagIdx >= scriptStartTagEndIdx);
			assertTrue(writeScriptTagEndIdx > 0);
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html><head></head><body><script dg-dashboard-code=\"abc\"/></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
			
			int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"abc\"/>") + "<script dg-dashboard-code=\"abc\"/>".length();
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);
			int writeScriptTagIdx = html.indexOf("<script type=\"text/javascript\">", scriptStartTagEndIdx);
			int writeScriptTagEndIdx = html.indexOf("</script>", writeScriptTagIdx);

			assertTrue(scriptStartTagEndIdx > 0);
			assertTrue(dashboardRenderCodeIdx > writeScriptTagIdx);
			assertTrue(dashboardRenderCodeIdx < writeScriptTagEndIdx);
			assertTrue(writeScriptTagIdx >= scriptStartTagEndIdx);
			assertTrue(writeScriptTagEndIdx > 0);
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		{
			String template = "<html><head></head><body><script dg-dashboard-code=\"\"></script></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
			
			int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"\">") + "<script dg-dashboard-code=\"\">".length();
			int scriptCloseTagIdx = html.indexOf("</script></body>");
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

			assertTrue(scriptStartTagEndIdx > 0);
			assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
			assertTrue(dashboardRenderCodeIdx > scriptStartTagEndIdx);
			assertTrue(dashboardRenderCodeIdx < scriptCloseTagIdx);
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		//<head></head>里的<script dg-dashboard-code></script>忽略
		{
			String template = "<html><head><script dg-dashboard-code></script></head><body></body></html>";

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);
			
			String dashboardInitCode = this.renderer.getDashboardFactoryVarElseDft(dashboardMeta.getDashboardFactoryVar()) + ".init("+dashboard.getVarName()+");";
			String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
			
			int scriptCloseTagIdx = html.indexOf("</script></head>");
			int bodyStartTagIdx = html.indexOf("<body>");
			int dashboardInitCodeIdx = html.indexOf(dashboardInitCode);
			int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

			assertTrue(dashboardInitCodeIdx > scriptCloseTagIdx);
			assertTrue(dashboardInitCodeIdx > bodyStartTagIdx);
			assertTrue(dashboardRenderCodeIdx > dashboardInitCodeIdx);
			
			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		//与dg-dashboard-auto-render组合
		{
			{
				String template = "<html dg-dashboard-auto-render=\"true\"><head></head><body><script dg-dashboard-code></script></body></html>";

				HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
				DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

				HtmlTplDashboard dashboard = filterContext.getDashboard();
				TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
				String html = getHtmlWithPrint(renderContext);
				
				String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
				
				int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code>") + "<script dg-dashboard-code>".length();
				int scriptCloseTagIdx = html.indexOf("</script></body>");
				int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

				assertTrue(scriptStartTagEndIdx > 0);
				assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
				assertTrue(dashboardRenderCodeIdx > scriptStartTagEndIdx);
				assertTrue(dashboardRenderCodeIdx < scriptCloseTagIdx);
				
				//缓存
				{
					HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
					this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
					String html1 = getHtmlWithPrint(renderContext1);
					
					assertEquals(html, html1);
				}
			}
			{
				String template = "<html dg-dashboard-auto-render=\"false\"><head></head><body><script dg-dashboard-code></script></body></html>";

				HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
				DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

				HtmlTplDashboard dashboard = filterContext.getDashboard();
				TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
				String html = getHtmlWithPrint(renderContext);
				
				String dashboardInitCode = this.renderer.getDashboardFactoryVarElseDft(dashboardMeta.getDashboardFactoryVar()) + ".init("+dashboard.getVarName()+");";
				String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
				
				int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code>") + "<script dg-dashboard-code>".length();
				int scriptCloseTagIdx = html.indexOf("</script></body>");
				int dashboardInitCodeIdx = html.indexOf(dashboardInitCode);
				int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

				assertTrue(scriptStartTagEndIdx > 0);
				assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
				assertTrue(dashboardInitCodeIdx > scriptStartTagEndIdx);
				assertTrue(dashboardInitCodeIdx < scriptCloseTagIdx);
				assertTrue(dashboardRenderCodeIdx < 0);
				
				//缓存
				{
					HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
					this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
					String html1 = getHtmlWithPrint(renderContext1);
					
					assertEquals(html, html1);
				}
			}
			
			{
				String template = "<html dg-dashboard-auto-render=\"true\"><head></head><body><script dg-dashboard-code=\"init\"></script></body></html>";

				HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
				DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

				HtmlTplDashboard dashboard = filterContext.getDashboard();
				TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
				String html = getHtmlWithPrint(renderContext);
				
				String dashboardInitCode = this.renderer.getDashboardFactoryVarElseDft(dashboardMeta.getDashboardFactoryVar()) + ".init("+dashboard.getVarName()+");";
				String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
				
				int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"init\">") + "<script dg-dashboard-code=\"init\">".length();
				int scriptCloseTagIdx = html.indexOf("</script></body>");
				int dashboardInitCodeIdx = html.indexOf(dashboardInitCode);
				int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

				assertTrue(scriptStartTagEndIdx > 0);
				assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
				assertTrue(dashboardInitCodeIdx > scriptStartTagEndIdx);
				assertTrue(dashboardInitCodeIdx < scriptCloseTagIdx);
				assertTrue(dashboardRenderCodeIdx < 0);
				
				//缓存
				{
					HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
					this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
					String html1 = getHtmlWithPrint(renderContext1);
					
					assertEquals(html, html1);
				}
			}
			{
				String template = "<html dg-dashboard-auto-render=\"false\"><head></head><body><script dg-dashboard-code=\"init\"></script></body></html>";

				HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
				DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

				HtmlTplDashboard dashboard = filterContext.getDashboard();
				TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
				String html = getHtmlWithPrint(renderContext);

				String dashboardInitCode = this.renderer.getDashboardFactoryVarElseDft(dashboardMeta.getDashboardFactoryVar()) + ".init("+dashboard.getVarName()+");";
				String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
				
				int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"init\">") + "<script dg-dashboard-code=\"init\">".length();
				int scriptCloseTagIdx = html.indexOf("</script></body>");
				int dashboardInitCodeIdx = html.indexOf(dashboardInitCode);
				int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

				assertTrue(scriptStartTagEndIdx > 0);
				assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
				assertTrue(dashboardInitCodeIdx > scriptStartTagEndIdx);
				assertTrue(dashboardInitCodeIdx < scriptCloseTagIdx);
				assertTrue(dashboardRenderCodeIdx < 0);
				
				//缓存
				{
					HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
					this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
					String html1 = getHtmlWithPrint(renderContext1);
					
					assertEquals(html, html1);
				}
			}
			
			{
				String template = "<html dg-dashboard-auto-render=\"true\"><head></head><body><script dg-dashboard-code=\"render\"></script></body></html>";

				HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
				DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

				HtmlTplDashboard dashboard = filterContext.getDashboard();
				TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
				String html = getHtmlWithPrint(renderContext);

				String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
				
				int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"render\">") + "<script dg-dashboard-code=\"render\">".length();
				int scriptCloseTagIdx = html.indexOf("</script></body>");
				int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

				assertTrue(scriptStartTagEndIdx > 0);
				assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
				assertTrue(dashboardRenderCodeIdx > scriptStartTagEndIdx);
				assertTrue(dashboardRenderCodeIdx < scriptCloseTagIdx);
				
				//缓存
				{
					HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
					this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
					String html1 = getHtmlWithPrint(renderContext1);
					
					assertEquals(html, html1);
				}
			}
			{
				String template = "<html dg-dashboard-auto-render=\"false\"><head></head><body><script dg-dashboard-code=\"render\"></script></body></html>";

				HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
				DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

				HtmlTplDashboard dashboard = filterContext.getDashboard();
				TplDashboardMeta dashboardMeta = filterContext.getDashboardMeta();
				String html = getHtmlWithPrint(renderContext);

				String dashboardRenderCode = dashboard.getVarName() + "."+this.renderer.getDashboardRenderFuncName()+"();";
				
				int scriptStartTagEndIdx = html.indexOf("<script dg-dashboard-code=\"render\">") + "<script dg-dashboard-code=\"render\">".length();
				int scriptCloseTagIdx = html.indexOf("</script></body>");
				int dashboardRenderCodeIdx = html.indexOf(dashboardRenderCode);

				assertTrue(scriptStartTagEndIdx > 0);
				assertTrue(scriptCloseTagIdx > scriptStartTagEndIdx);
				assertTrue(dashboardRenderCodeIdx > scriptStartTagEndIdx);
				assertTrue(dashboardRenderCodeIdx < scriptCloseTagIdx);
				
				//缓存
				{
					HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
					this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
					String html1 = getHtmlWithPrint(renderContext1);
					
					assertEquals(html, html1);
				}
			}
		}
	}
	
	@Test
	public void renderDashboardPerformanceTest() throws Throwable
	{
		int loopCount = 5000;
		
		HtmlTplDashboardWidget dashboardWidget = createHtmlTplDashboardWidget();

		String template = "<html>" +
				"<head>" +
				"<title></title>" +
				"<style>" +
				".test{" +
				"	color: red;" +
				"}" +
				"</style>" +
				"<script>" +
				"var a = 3;" +
				"var b = 4;" +
				"</script>" +
				"</head>" +
				"<body>" +
				"<div class=\"main\" style=\"width:100%;height:100vh;\">" +
				"	<div class=\"head\" style=\"width:100%;height:20vh;\">head</div>" +
				"	<div class=\"content\" style=\"width:100%;height:60vh;\">" +
				"		<div dg-chart-widget=\""+HTML_CHART_WIDGET_ID_01+"\"></div>" +
				"		<div dg-chart-widget=\""+HTML_CHART_WIDGET_ID_01+"\"></div>" +
				"		<div dg-chart-widget=\""+HTML_CHART_WIDGET_ID_01+"\"></div>" +
				"		<div dg-chart-widget=\""+HTML_CHART_WIDGET_ID_01+"\"></div>" +
				"		" +
				"		<div dg-chart-widget=\""+HTML_CHART_WIDGET_ID_02+"\"></div>" +
				"		<div dg-chart-widget=\""+HTML_CHART_WIDGET_ID_02+"\"></div>" +
				"		<div dg-chart-widget=\""+HTML_CHART_WIDGET_ID_02+"\"></div>" +
				"		<div dg-chart-widget=\""+HTML_CHART_WIDGET_ID_02+"\"></div>" +
				"		" +
				"		<div dg-chart-widget=\"unknown-0\"></div>" +
				"		<div dg-chart-widget=\"unknown-1\"></div>" +
				"		<div dg-chart-widget=\"unknown-2\" id=\"myelementid\"></div>" +
				"	</div>" +
				"	<div class=\"foot\" style=\"width:100%;height:20vh;\">foot</div>" +
				"</div>" +
				"</body>" +
				"</html>";

		TplDashboardMeta dashboardMeta = null;
		
		{

			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);

			HtmlTplDashboard dashboard = filterContext.getDashboard();
			dashboardMeta = filterContext.getDashboardMeta();
			String html = getHtmlWithPrint(renderContext);

			//缓存
			{
				HtmlTplDashboardRenderContext renderContext1 = buildRenderContext(template);
				this.renderer.doRenderDashboard(dashboardWidget, renderContext1, dashboardMeta);
				String html1 = getHtmlWithPrint(renderContext1);
				
				assertEquals(html, html1);
			}
		}
		
		long rawTimes = 0;
		long enhanceTimes = 0;
		
		for(int i=0; i<loopCount; i++)
		{
			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			
			long beforeTime = System.currentTimeMillis();

			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext);
			
			rawTimes += System.currentTimeMillis() - beforeTime;
		}
		
		for(int i=0; i<loopCount; i++)
		{
			HtmlTplDashboardRenderContext renderContext = buildRenderContext(template);
			
			long beforeTime = System.currentTimeMillis();
			
			DashboardFilterContext filterContext = this.renderer.doRenderDashboard(dashboardWidget, renderContext, dashboardMeta);
			
			enhanceTimes += System.currentTimeMillis() - beforeTime;
		}
		
		double enhance = rawTimes/(double)enhanceTimes;
		
		assertTrue(enhance > 1.1f);
		
		System.out.println("-----------------------");
		System.out.println("test count   : " + loopCount);
		System.out.println("raw time     : " + rawTimes);
		System.out.println("enhance time : " + enhanceTimes);
		System.out.println("enhance ratio : " + new DecimalFormat("0.00").format(enhance));
		System.out.println("-----------------------");
	}
	
	protected HtmlTplDashboardRenderContext buildRenderContext()
	{
		return buildRenderContext(TEMPLATE_NAME, null, new StringWriter());
	}
	
	protected HtmlTplDashboardRenderContext buildRenderContext(String templateContent)
	{
		return buildRenderContext(TEMPLATE_NAME, IOUtil.getReader(templateContent), new StringWriter());
	}
	
	protected HtmlTplDashboardRenderContext buildRenderContext(String template, Reader templateReader, Writer out)
	{
		HtmlTplDashboardRenderContext renderContext = new HtmlTplDashboardRenderContext(template, templateReader, out);
		renderContext.setDashboardTheme(SimpleDashboardThemeSource.THEME_LIGHT);
		renderContext.setImportList(buildImportList());
		
		return renderContext;
	}

	protected List<HtmlTplDashboardImport> buildImportList()
	{
		List<HtmlTplDashboardImport> list = new ArrayList<>();

		list.add(HtmlTplDashboardImport.valueOf("jquery", IMPORT_CONTENT_JQUERY));
		list.add(HtmlTplDashboardImport.valueOf("util", IMPORT_CONTENT_UTIL));
		list.add(HtmlTplDashboardImport.valueOf("theme", IMPORT_CONTENT_THEME));
		list.add(HtmlTplDashboardImport.valueOf("style", IMPORT_CONTENT_STYLE));

		return list;
	}

	protected HtmlTplDashboardWidget createHtmlTplDashboardWidget()
	{
		return createHtmlTplDashboardWidget(this.renderer);
	}

	protected HtmlTplDashboardWidget createHtmlTplDashboardWidget(HtmlTplDashboardWidgetHtmlRenderer renderer)
	{
		return new HtmlTplDashboardWidget("widget01", TEMPLATE_NAME, renderer, this.resManager);
	}

	protected int countOf(String src, String sub)
	{
		int c = 0;

		int idx = -1;
		while (true)
		{
			idx = src.indexOf(sub, (idx < 0 ? 0 : idx + sub.length()));

			if (idx < 0)
				break;
			else
				c++;
		}

		return c;
	}

	protected String getHtmlWithPrint(HtmlTplDashboardRenderContext renderContext)
	{
		return getHtmlWithPrint((StringWriter)renderContext.getWriter());
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
	
	private static class TestFixedIdHtmlTplDashboardWidgetHtmlRenderer extends HtmlTplDashboardWidgetHtmlRenderer
	{
		public TestFixedIdHtmlTplDashboardWidgetHtmlRenderer()
		{
			super();
		}

		public TestFixedIdHtmlTplDashboardWidgetHtmlRenderer(ChartWidgetSource chartWidgetSource)
		{
			super(chartWidgetSource);
		}

		@Override
		protected String nextDashboardId()
		{
			return DASHBOARD_ID;
		}

		@Override
		protected String nextChartId(HtmlTplDashboard dashboard, int chartIndex)
		{
			return "chart-id-" + chartIndex;
		}
	}
}
