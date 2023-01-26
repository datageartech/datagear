/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.File;
import java.io.StringWriter;

import org.datagear.analysis.ChartDefinition;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginTest
{
	public HtmlChartPluginTest() throws Throwable
	{
		super();
	}

	@Test
	public void renderChartTest() throws Throwable
	{
		HtmlChartPlugin htmlChartPlugin = createHtmlChartPlugin();

		HtmlChartRenderContext renderContext = createHtmlChartRenderContext();
		htmlChartPlugin.renderChart(new ChartDefinition(), renderContext);

		String html = getHtmlWithPrint(renderContext);
		JsChartRenderer renderer = htmlChartPlugin.getRenderer();
		String rendererStr = IOUtil.readString(renderer.getCodeReader(), true);

		Assert.assertTrue(html.contains("<div id=\"" + renderContext.getChartElementId() + "\"></div>"));
		Assert.assertTrue(html.contains("(" + renderContext.getChartVarName() + ");"));
		Assert.assertTrue(rendererStr.contains("this is render function"));
		Assert.assertTrue(rendererStr.contains("this is update function"));
	}

	@Test
	public void renderChartTest_rendererFile() throws Throwable
	{
		HtmlChartPlugin htmlChartPlugin = createHtmlChartPluginForRendererFile();

		HtmlChartRenderContext renderContext = createHtmlChartRenderContext();
		renderContext.setPluginVarName("myChartPlugin");
		htmlChartPlugin.renderChart(new ChartDefinition(), renderContext);

		String html = getHtmlWithPrint(renderContext);
		JsChartRenderer renderer = htmlChartPlugin.getRenderer();
		String rendererStr = IOUtil.readString(renderer.getCodeReader(), true);

		Assert.assertTrue(html.contains("<div id=\"" + renderContext.getChartElementId() + "\"></div>"));
		Assert.assertTrue(html.contains("(" + renderContext.getChartVarName() + ");"));
		Assert.assertTrue(rendererStr.contains("(function(localPlugin)"));
		Assert.assertTrue(rendererStr.contains("})(plugin);"));
		Assert.assertTrue(html.contains("})(" + renderContext.getPluginVarName() + ");"));
	}

	@Test
	public void renderChartTest_setChartRenderContextVarName() throws Throwable
	{
		HtmlChartPlugin htmlChartPlugin = createHtmlChartPlugin();

		HtmlChartRenderContext renderContext = createHtmlChartRenderContext();
		renderContext.setRenderContextVarName("chartRenderContext");
		htmlChartPlugin.renderChart(new ChartDefinition(), renderContext);

		String html = getHtmlWithPrint(renderContext);

		Assert.assertTrue(html.contains("\"renderContext\":chartRenderContext"));
	}

	@Test
	public void renderChartOldTest() throws Throwable
	{
		HtmlChartPlugin htmlChartPlugin = createHtmlChartPluginOld();

		HtmlChartRenderContext renderContext = createHtmlChartRenderContext();
		htmlChartPlugin.renderChart(new ChartDefinition(), renderContext);

		String html = getHtmlWithPrint(renderContext);
		JsChartRenderer renderer = htmlChartPlugin.getRenderer();
		String rendererStr = IOUtil.readString(renderer.getCodeReader(), true);

		Assert.assertTrue(html.contains("<div id=\"" + renderContext.getChartElementId() + "\"></div>"));
		Assert.assertTrue(html.contains("(" + renderContext.getChartVarName() + ");"));
		Assert.assertTrue(rendererStr.contains("this is render function"));
		Assert.assertTrue(rendererStr.contains("this is update function"));
	}
	
	protected HtmlChartRenderContext createHtmlChartRenderContext()
	{
		HtmlChartRenderContext renderContext = new HtmlChartRenderContext(new StringWriter());
		return renderContext;
	}

	public static HtmlChartPlugin createHtmlChartPlugin() throws Exception
	{
		File directory = FileUtil.getFile("src/test/resources/org/datagear/analysis/support/html/htmlChartPluginTest");
		HtmlChartPlugin htmlChartPlugin = new HtmlChartPluginLoader().load(directory);

		return htmlChartPlugin;
	}

	public static HtmlChartPlugin createHtmlChartPluginForRendererFile() throws Exception
	{
		File directory = FileUtil
				.getFile("src/test/resources/org/datagear/analysis/support/html/htmlChartPluginTestRendererFile");
		HtmlChartPlugin htmlChartPlugin = new HtmlChartPluginLoader().load(directory);

		return htmlChartPlugin;
	}

	public static HtmlChartPlugin createHtmlChartPluginOld() throws Exception
	{
		File directory = FileUtil
				.getFile("src/test/resources/org/datagear/analysis/support/html/htmlChartPluginTestOld");
		HtmlChartPlugin htmlChartPlugin = new HtmlChartPluginLoader().load(directory);

		return htmlChartPlugin;
	}
	
	protected String getHtmlWithPrint(HtmlChartRenderContext renderContext)
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
}
