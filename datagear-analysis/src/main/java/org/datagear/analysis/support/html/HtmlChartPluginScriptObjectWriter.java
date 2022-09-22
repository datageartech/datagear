/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.util.IOUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	 * [varName].renderer=
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
		out.write(varName + "." + HtmlChartPlugin.PROPERTY_RENDERER + "=");
		writeNewLine(out);

		JsChartRenderer renderer = plugin.getRenderer();

		Reader reader = null;
		try
		{
			reader = renderer.getReader();
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
		public HtmlChartPluginJson(HtmlChartPlugin plugin)
		{
			super(plugin.getId(), plugin.getNameLabel());
			setDescLabel(plugin.getDescLabel());
			setResources(ChartPluginResourceJson.valuesOf(plugin.getResources()));
			setIconResourceNames(plugin.getIconResourceNames());
			setChartParams(plugin.getChartParams());
			setDataSigns(plugin.getDataSigns());
			setVersion(plugin.getVersion());
			setOrder(plugin.getOrder());
			setCategories(plugin.getCategories());
			setCategoryOrders(plugin.getCategoryOrders());
		}

		@JsonIgnore
		@Override
		public Chart renderChart(RenderContext renderContext, ChartDefinition chartDefinition) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}

	protected static class ChartPluginResourceJson implements ChartPluginResource
	{
		private String name;

		public ChartPluginResourceJson()
		{
			super();
		}

		public ChartPluginResourceJson(String name)
		{
			super();
			this.name = name;
		}

		public ChartPluginResourceJson(ChartPluginResource resource)
		{
			super();
			this.name = (resource == null ? null : resource.getName());
		}

		@Override
		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		@JsonIgnore
		@Override
		public InputStream getInputStream() throws IOException
		{
			return null;
		}

		@JsonIgnore
		@Override
		public long getLastModified()
		{
			return 0;
		}

		public static List<ChartPluginResourceJson> valuesOf(List<? extends ChartPluginResource> resources)
		{
			if (resources == null)
				return null;
			else if (resources.isEmpty())
				return Collections.emptyList();

			List<ChartPluginResourceJson> resJsons = new ArrayList<ChartPluginResourceJson>(resources.size());

			for (ChartPluginResource resource : resources)
				resJsons.add(new ChartPluginResourceJson(resource));

			return resJsons;
		}
	}
}
