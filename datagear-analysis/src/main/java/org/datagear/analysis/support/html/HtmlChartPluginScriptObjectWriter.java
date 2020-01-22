/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
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
	public static final String JS_CHART_RENDER_NAME = "jsChartRender";

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
	 * [varName].jsChartRender=
	 * {...};
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param plugin
	 * @param varName
	 * @throws IOException
	 */
	public void write(Writer out, HtmlChartPlugin<?> plugin, String varName) throws IOException
	{
		JsonChartPlugin jsonPlugin = new JsonChartPlugin(plugin);

		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonPlugin);
		out.write(";");
		writeNewLine(out);
		writeJsChartRender(out, plugin, varName);
	}

	/**
	 * 写JS渲染器内容。
	 * 
	 * @param renderContext
	 * @param chart
	 * @throws IOException
	 */
	protected void writeJsChartRender(Writer out, HtmlChartPlugin<?> plugin, String varName) throws IOException
	{
		out.write(varName + "." + JS_CHART_RENDER_NAME + "=");
		writeNewLine(out);

		JsChartRenderer jsChartRenderer = plugin.getJsChartRenderer();

		Reader reader = jsChartRenderer.getReader();

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
	 * 可输出为JSON的{@linkplain ChartPlugin}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class JsonChartPlugin extends AbstractChartPlugin<RenderContext>
	{
		public JsonChartPlugin(HtmlChartPlugin<?> plugin)
		{
			super(plugin.getId(), plugin.getNameLabel());
			setDescLabel(plugin.getDescLabel());
			setManualLabel(plugin.getManualLabel());
			setChartProperties(plugin.getChartProperties());
			setDataSigns(plugin.getDataSigns());
			setVersion(plugin.getVersion());
			setOrder(plugin.getOrder());
		}

		@Override
		public Chart renderChart(RenderContext renderContext, Map<String, ?> chartPropertyValues,
				ChartDataSet... chartDataSets) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
