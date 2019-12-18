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

import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.Theme;
import org.datagear.analysis.support.JsonChartPluginPropertiesResolver;
import org.datagear.analysis.support.LocationResource;
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
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(stringWriter);
		HtmlRenderAttributes.setRenderStyle(renderContext, RenderStyle.DARK);
		HtmlRenderAttributes.setChartTheme(renderContext, new ChartTheme("dark", "white", "gray",
				new String[] { "red", "green" }, new Theme("dark", "white", "gray")));
		HtmlRenderAttributes.setLocale(renderContext, Locale.getDefault());

		htmlChartPlugin.renderChart(renderContext, null, (DataSetFactory[]) null);
		htmlChartPlugin.renderChart(renderContext, null, (DataSetFactory[]) null);

		String html = stringWriter.toString();

		Assert.assertTrue(html
				.contains("<div id=\"" + HtmlRenderAttributes.generateChartElementId(1) + "\" class=\"chart\"></div>"));
		Assert.assertTrue(html.contains("(" + HtmlRenderAttributes.generateChartVarName(1) + ");"));

		Assert.assertTrue(html
				.contains("<div id=\"" + HtmlRenderAttributes.generateChartElementId(2) + "\" class=\"chart\"></div>"));
		Assert.assertTrue(html.contains("(" + HtmlRenderAttributes.generateChartVarName(2) + ");"));

		System.out.println(html);
	}

	@Test
	public void renderChartTest_setChartRenderContextVarName() throws Throwable
	{
		HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin = createHtmlChartPlugin();

		StringWriter stringWriter = new StringWriter();
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(stringWriter);
		HtmlRenderAttributes.setChartRenderContextVarName(renderContext, "chartRenderContext");

		htmlChartPlugin.renderChart(renderContext, null, (DataSetFactory[]) null);

		String html = stringWriter.toString();

		Assert.assertTrue(html.contains("\"renderContext\":chartRenderContext"));

		System.out.println(html);
	}

	public static HtmlChartPlugin<HtmlRenderContext> createHtmlChartPlugin() throws Exception
	{
		InputStream jsonInputStream = HtmlChartPluginTest.class.getClassLoader()
				.getResourceAsStream("org/datagear/analysis/support/html/HtmlChartPlugin.config.json");
		Map<String, Object> properties = jsonChartPluginPropertiesResolver.resolveChartPluginProperties(jsonInputStream, "UTF-8");

		HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin = new HtmlChartPlugin<HtmlRenderContext>();
		jsonChartPluginPropertiesResolver.setChartPluginProperties(htmlChartPlugin, properties);

		htmlChartPlugin.setScriptContent(
				LocationResource.toClasspathLocation("org/datagear/analysis/support/html/HtmlChartPlugin.chart.js"));

		return htmlChartPlugin;
	}
}
