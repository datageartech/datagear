/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.util.IOUtil;

/**
 * {@linkplain HtmlChartPlugin} JS脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginScriptObjectWriter extends AbstractHtmlScriptObjectWriter
{
	public HtmlChartPluginScriptObjectWriter()
	{
		super();
	}

	/**
	 * 将{@linkplain HtmlChartPlugin}的JS脚本对象写入输出流。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * var [varName]=
	 * { ... };
	 * [varName].chartRenderer=
	 * {...};
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param plugin
	 * @param varName
	 * @throws IOException
	 */
	public void write(Writer out, HtmlChartPlugin plugin, String varName) throws IOException
	{
		HtmlChartPluginJson jsonPlugin = new HtmlChartPluginJson(plugin);

		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonPlugin);
		out.write(";");
		writeNewLine(out);
		writeHtmlChartRenderer(out, plugin, varName);
	}

	/**
	 * 写JS渲染器内容。
	 * 
	 * @param renderContext
	 * @param chart
	 * @throws IOException
	 */
	protected void writeHtmlChartRenderer(Writer out, HtmlChartPlugin plugin, String varName) throws IOException
	{
		out.write(varName + "." + HtmlChartPlugin.PROPERTY_CHART_RENDERER + "=");
		writeNewLine(out);

		JsChartRenderer chartRenderer = plugin.getChartRenderer();

		Reader reader = chartRenderer.getReader();

		try
		{
			IOUtil.write(reader, out);
		}
		finally
		{
			IOUtil.close(reader);
		}

		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 用于输出JSON的{@linkplain ChartPlugin}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class HtmlChartPluginJson extends AbstractChartPlugin
	{
		private static final long serialVersionUID = 1L;

		public HtmlChartPluginJson(HtmlChartPlugin plugin)
		{
			super(plugin.getId(), plugin.getNameLabel());
			setDescLabel(plugin.getDescLabel());
			setManualLabel(plugin.getManualLabel());
			setChartParams(plugin.getChartParams());
			setDataSigns(plugin.getDataSigns());
			setVersion(plugin.getVersion());
			setOrder(plugin.getOrder());
			setCategories(plugin.getCategories());
			setCategoryOrders(plugin.getCategoryOrders());
		}

		@Override
		public Chart renderChart(RenderContext renderContext, ChartDefinition chartDefinition) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
