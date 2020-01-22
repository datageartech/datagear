/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.JsonChartPluginPropertiesResolver;
import org.datagear.analysis.support.LocationResource;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.analysis.support.html.HtmlRenderContext.WebContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginTest
{
	private static JsonChartPluginPropertiesResolver jsonChartPluginPropertiesResolver = new JsonChartPluginPropertiesResolver();

	public HtmlChartPluginTest() throws Throwable
	{
		super();
	}

	@Test
	public void renderChartTest() throws Throwable
	{
		HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin = createHtmlChartPlugin();

		StringWriter stringWriter = new StringWriter();
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(new WebContext("", ""), stringWriter);
		HtmlRenderAttributes.setRenderStyle(renderContext, RenderStyle.DARK);
		HtmlRenderAttributes.setChartTheme(renderContext, SimpleDashboardThemeSource.THEME_LIGHT.getChartTheme());
		HtmlRenderAttributes.setLocale(renderContext, Locale.getDefault());

		htmlChartPlugin.renderChart(renderContext, null, (ChartDataSet[]) null);

		String html = getHtmlWithPrint(stringWriter);

		Assert.assertTrue(html.contains("<div id=\"" + HtmlRenderAttributes.generateChartElementId("1") + "\"></div>"));
		Assert.assertTrue(html.contains("(" + HtmlRenderAttributes.generateChartVarName("4") + ");"));
	}

	@Test
	public void renderChartTest_setChartRenderContextVarName() throws Throwable
	{
		HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin = createHtmlChartPlugin();

		StringWriter stringWriter = new StringWriter();
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(new WebContext("", ""), stringWriter);

		HtmlChartPluginRenderOption option = new HtmlChartPluginRenderOption();
		option.setRenderContextVarName("chartRenderContext");
		HtmlChartPluginRenderOption.setOption(renderContext, option);

		htmlChartPlugin.renderChart(renderContext, null, (ChartDataSet[]) null);

		String html = getHtmlWithPrint(stringWriter);

		Assert.assertTrue(html.contains("\"renderContext\":chartRenderContext"));
	}

	public static HtmlChartPlugin<HtmlRenderContext> createHtmlChartPlugin() throws Exception
	{
		InputStream jsonInputStream = HtmlChartPluginTest.class.getClassLoader()
				.getResourceAsStream("org/datagear/analysis/support/html/HtmlChartPlugin.config.json");
		Map<String, Object> properties = jsonChartPluginPropertiesResolver.resolveChartPluginProperties(jsonInputStream,
				"UTF-8");

		HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin = new HtmlChartPlugin<HtmlRenderContext>();
		jsonChartPluginPropertiesResolver.setChartPluginProperties(htmlChartPlugin, properties);

		htmlChartPlugin.setJsChartRenderer(new LocationJsChartRenderer(
				LocationResource.toClasspathLocation("org/datagear/analysis/support/html/HtmlChartPlugin.chart.js")));

		return htmlChartPlugin;
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
