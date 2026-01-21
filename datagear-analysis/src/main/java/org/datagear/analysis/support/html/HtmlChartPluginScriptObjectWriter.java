/*
 * Copyright 2018-present datagear.tech
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
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;

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
			writeNewLine(out);
		}
		else if (JsChartRenderer.CODE_TYPE_INVOKE.equals(codeType))
		{
			String tmpVarName = this.localRendererVarNameForInvode;

			out.write("(function(" + JsChartRenderer.INVOKE_CONTEXT_PLUGIN_VAR + "){");
			writeNewLine(out);
			out.write("try{ ");
			writeNewLine(out);
			out.write("var " + tmpVarName + " =");
			writeNewLine(out);
			writeHtmlChartRendererCodeValue(out, renderer);
			writeNewLine(out);
			out.write("return " + tmpVarName + ";");
			writeNewLine(out);
			out.write("}catch(e){ if(typeof(console) !== \"undefined\"){ if(console.error){ console.error(e); } } }");
			writeNewLine(out);
			out.write("})(" + varName + ");");
			writeNewLine(out);
		}
		else
			throw new IOException("Unsupported JsChartRenderer code type : " + codeType);
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
}
