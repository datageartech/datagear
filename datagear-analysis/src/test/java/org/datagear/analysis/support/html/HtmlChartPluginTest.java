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
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.html.HtmlChartRenderAttr.HtmlChartRenderOption;
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

		DefaultRenderContext renderContext = new DefaultRenderContext();
		HtmlChartRenderAttr renderAttr = new HtmlChartRenderAttr();
		StringWriter stringWriter = new StringWriter();
		HtmlChartRenderOption renderOption = new HtmlChartRenderOption(renderAttr);
		renderAttr.inflate(renderContext, stringWriter, renderOption);

		htmlChartPlugin.renderChart(renderContext, new ChartDefinition());

		String html = getHtmlWithPrint(stringWriter);

		Assert.assertTrue(html.contains("<div id=\"" + renderOption.getChartElementId() + "\"></div>"));
		Assert.assertTrue(html.contains("(" + renderOption.getChartVarName() + ");"));
	}

	@Test
	public void renderChartTest_setChartRenderContextVarName() throws Throwable
	{
		HtmlChartPlugin htmlChartPlugin = createHtmlChartPlugin();

		DefaultRenderContext renderContext = new DefaultRenderContext();
		HtmlChartRenderAttr renderAttr = new HtmlChartRenderAttr();
		StringWriter stringWriter = new StringWriter();
		HtmlChartRenderOption renderOption = new HtmlChartRenderOption(renderAttr);
		renderOption.setRenderContextVarName("chartRenderContext");
		renderAttr.inflate(renderContext, stringWriter, renderOption);

		htmlChartPlugin.renderChart(renderContext, new ChartDefinition());

		String html = getHtmlWithPrint(stringWriter);

		Assert.assertTrue(html.contains("\"renderContext\":chartRenderContext"));
	}

	public static HtmlChartPlugin createHtmlChartPlugin() throws Exception
	{
		File directory = new File("src/test/resources/org/datagear/analysis/support/html/HtmlChartPluginTest");

		HtmlChartPlugin htmlChartPlugin = new HtmlChartPluginLoader().load(directory);

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
