/*
 * Copyright 2018-2024 datagear.tech
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.datagear.analysis.Category;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginAttribute;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.i18n.LabelUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain HtmlChartPlugin} JS脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginScriptObjectWriter extends AbstractHtmlScriptObjectWriter
{
	public static final HtmlChartPluginScriptObjectWriter INSTANCE = new HtmlChartPluginScriptObjectWriter();

	private String localRendererVarNameForInvode = Global.PRODUCT_NAME_EN_LC + "Renderer" + IDUtil.toStringOfMaxRadix();

	public HtmlChartPluginScriptObjectWriter()
	{
		super();
	}

	public String getLocalRendererVarNameForInvode()
	{
		return localRendererVarNameForInvode;
	}

	public void setLocalRendererVarNameForInvode(String localRendererVarNameForInvode)
	{
		this.localRendererVarNameForInvode = localRendererVarNameForInvode;
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
	 * @param locale
	 *            允许为{@code null}
	 * @throws IOException
	 */
	public void write(Writer out, HtmlChartPlugin plugin, String varName, Locale locale) throws IOException
	{
		HtmlChartPluginJson jsonPlugin = new HtmlChartPluginJson(plugin, locale);

		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonPlugin);
		out.write(";");
		writeNewLine(out);

		// 这里必须在写入插件基本信息后再写入渲染器代码，使得渲染器代码内可以使用插件基本信息
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
		JsChartRenderer renderer = plugin.getRenderer();
		String codeType = renderer.getCodeType();

		out.write(varName + "." + HtmlChartPlugin.PROPERTY_RENDERER + "=");
		writeNewLine(out);
		
		if (JsChartRenderer.CODE_TYPE_OBJECT.equals(codeType))
		{
			writeHtmlChartRendererCodeValue(out, renderer);
			out.write(";");
		}
		else if (JsChartRenderer.CODE_TYPE_INVOKE.equals(codeType))
		{
			String tmpVarName = this.localRendererVarNameForInvode;

			out.write("(function(" + JsChartRenderer.INVOKE_CONTEXT_PLUGIN_VAR + "){");
			writeNewLine(out);
			out.write("var " + tmpVarName + " =");
			writeNewLine(out);
			writeHtmlChartRendererCodeValue(out, renderer);
			writeNewLine(out);
			out.write("return " + tmpVarName + ";");
			writeNewLine(out);
			out.write("})(" + varName + ");");
			writeNewLine(out);
		}
		else
			throw new IOException("Unsupported JsChartRenderer code type : " + codeType);

		writeNewLine(out);
	}

	protected void writeHtmlChartRendererCodeValue(Writer out, JsChartRenderer renderer) throws IOException
	{
		Reader reader = null;
		try
		{
			reader = renderer.getCodeReader();
			IOUtil.write(reader, out);
		}
		finally
		{
			IOUtil.close(reader);
		}
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

		public HtmlChartPluginJson(HtmlChartPlugin plugin, Locale locale)
		{
			super(plugin.getId(), plugin.getNameLabel());
			LabelUtil.concrete(plugin, this, locale);
			setResources(ChartPluginResourceJson.valuesOf(plugin.getResources()));
			setIconResourceNames(plugin.getIconResourceNames());
			setAttributes(ChartPluginAttribute.clone(plugin.getAttributes(), locale));
			setDataSigns(DataSign.clone(plugin.getDataSigns(), locale));
			setDataSetRange(plugin.getDataSetRange());
			setVersion(plugin.getVersion());
			setOrder(plugin.getOrder());
			setCategories(Category.clone(plugin.getCategories(), locale));
			setCategoryOrders(plugin.getCategoryOrders());
		}

		@JsonIgnore
		@Override
		public Chart renderChart(ChartDefinition chartDefinition, RenderContext renderContext) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}

	protected static class ChartPluginResourceJson implements ChartPluginResource, Serializable
	{
		private static final long serialVersionUID = 1L;

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
