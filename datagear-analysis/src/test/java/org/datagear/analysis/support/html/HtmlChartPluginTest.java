/*
 * Copyright 2018-2023 datagear.tech
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
