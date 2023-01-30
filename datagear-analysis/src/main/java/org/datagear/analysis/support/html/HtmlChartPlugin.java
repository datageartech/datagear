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

import java.io.IOException;
import java.io.Writer;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.util.i18n.Label;

/**
 * HTML图表插件。
 * <p>
 * 注意：此类的{@linkplain #renderChart(ChartDefinition, RenderContext)}参数必须为{@linkplain HtmlChartRenderContext}。
 * </p>
 * <p>
 * 输出格式为：
 * </p>
 * <code>
 * <pre>
 * &lt;div id="[图表HTML元素ID]"&gt;&lt;/div&gt;
 * &lt;script type="text/javascript"&gt;
 * var [图表插件变量名]={..., renderer: {...}};
 * var [渲染上下文变量名]={...};
 * var [图表变量名]=
 * {
 * 	id : "...",
 * 	elementId : "[图表HTML元素ID]",
 * 	varName : "[图表变量名]",
 * 	plugin : [图表插件变量名],
 * 	renderContext : [渲染上下文变量名],
 * 	chartDataSets : [{...}, ...]
 * };
 * [图表插件变量名].renderer.render([图表变量名]);
 * &lt;/script&gt;
 * </pre>
 * </code>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPlugin extends AbstractChartPlugin
{
	/** 图表渲染器属性名 */
	public static final String PROPERTY_RENDERER = "renderer";
	
	/**
	 * 旧版本（4.0.0及以前版本）图表渲染器属性名，将在未来版本移除。
	 */
	public static final String PROPERTY_RENDERER_OLD = "chartRenderer";
	
	/** HTML换行符 */
	public static final String HTML_NEW_LINE = "\n";

	protected static final HtmlChartPluginScriptObjectWriter HTML_CHART_PLUGIN_SCRIPT_OBJECT_WRITER = new HtmlChartPluginScriptObjectWriter();
	protected static final HtmlRenderContextScriptObjectWriter HTML_RENDER_CONTEXT_SCRIPT_OBJECT_WRITER = new HtmlRenderContextScriptObjectWriter();
	protected static final HtmlChartScriptObjectWriter HTML_CHART_SCRIPT_OBJECT_WRITER = new HtmlChartScriptObjectWriter();

	/** JS图表渲染器 */
	private JsChartRenderer renderer;

	/** 图表HTML元素标签名 */
	private String elementTagName = "div";

	/** 上次修改时间 */
	private long lastModified = -1;

	/** 图表脚本换行符 */
	private String newLine = HTML_NEW_LINE;

	public HtmlChartPlugin()
	{
		super();
	}

	public HtmlChartPlugin(String id, Label nameLabel, JsChartRenderer renderer)
	{
		super(id, nameLabel);
		this.renderer = renderer;
	}

	public JsChartRenderer getRenderer()
	{
		return renderer;
	}

	public void setRenderer(JsChartRenderer renderer)
	{
		this.renderer = renderer;
	}

	public String getElementTagName()
	{
		return elementTagName;
	}

	public void setElementTagName(String elementTagName)
	{
		this.elementTagName = elementTagName;
	}

	/**
	 * 获取上次修改时间。
	 * 
	 * @return -1 表示未修改
	 */
	public long getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}

	public String getNewLine()
	{
		return newLine;
	}

	public void setNewLine(String newLine)
	{
		this.newLine = newLine;
	}

	@Override
	public HtmlChart renderChart(ChartDefinition chartDefinition, RenderContext renderContext) throws RenderException
	{
		if(!(renderContext instanceof HtmlChartRenderContext))
			throw new IllegalArgumentException("[renderContext] must be instance of " + HtmlChartRenderContext.class.getSimpleName());
		
		HtmlChartRenderContext htmlChartRenderContext = (HtmlChartRenderContext)renderContext;

		HtmlChart chart = createHtmlChart(chartDefinition, htmlChartRenderContext);

		try
		{
			writeChartElement(chartDefinition, htmlChartRenderContext, chart);
			writeScript(chartDefinition, htmlChartRenderContext, chart);
		}
		catch (IOException e)
		{
			throw new RenderException(e);
		}

		return chart;
	}

	protected HtmlChart createHtmlChart(ChartDefinition chartDefinition, HtmlChartRenderContext renderContext)
	{
		return new HtmlChart(chartDefinition, this, renderContext, renderContext.getChartElementId(),
				renderContext.getChartVarName());
	}

	protected boolean writeChartElement(ChartDefinition chartDefinition, HtmlChartRenderContext renderContext, HtmlChart chart) throws IOException
	{
		if (renderContext.isNotWriteChartElement())
			return false;
		
		Writer out = renderContext.getWriter();
		
		out.write("<" + this.elementTagName + " id=\"" + renderContext.getChartElementId() + "\">");
		out.write("</" + this.elementTagName + ">");
		writeNewLine(out);

		return true;
	}

	protected void writeScript(ChartDefinition chartDefinition, HtmlChartRenderContext renderContext, HtmlChart chart) throws IOException
	{
		Writer out = renderContext.getWriter();
		
		if (!renderContext.isNotWriteScriptTag())
		{
			writeScriptStartTag(renderContext, out);
			writeNewLine(out);
		}

		writePluginJsObject(chartDefinition, renderContext, chart);
		writeRenderContextJsObject(chartDefinition, renderContext, chart);
		writeChartJsObject(chartDefinition, renderContext, chart);

		if (!renderContext.isNotWriteScriptTag())
		{
			writeScriptEndTag(renderContext, out);
			writeNewLine(out);
		}
	}

	protected boolean writePluginJsObject(ChartDefinition chartDefinition, HtmlChartRenderContext renderContext, HtmlChart chart) throws IOException
	{
		if (renderContext.isNotWritePluginObject())
			return false;

		Writer out = renderContext.getWriter();
		HtmlChartPlugin plugin = chart.getPlugin();

		getHtmlChartPluginScriptObjectWriter().write(out, plugin, renderContext.getPluginVarName(),
				renderContext.getLocale());

		return true;
	}

	protected boolean writeRenderContextJsObject(ChartDefinition chartDefinition, HtmlChartRenderContext renderContext, HtmlChart chart) throws IOException
	{
		if (renderContext.isNotWriteRenderContextObject())
			return false;

		Writer out = renderContext.getWriter();
		
		getHtmlRenderContextScriptObjectWriter().write(out, renderContext, renderContext.getRenderContextVarName());

		return true;
	}

	protected void writeChartJsObject(ChartDefinition chartDefinition, HtmlChartRenderContext renderContext, HtmlChart chart) throws IOException
	{
		Writer out = renderContext.getWriter();
		
		if (renderContext.isWriteChartJson())
			getHtmlChartScriptObjectWriter().writeJson(out, chart, renderContext.getRenderContextVarName(),
					renderContext.getPluginVarName());
		else
			getHtmlChartScriptObjectWriter().write(out, chart, renderContext.getRenderContextVarName(),
					renderContext.getPluginVarName());

		if (!renderContext.isNotWriteInvoke())
		{
			out.write(renderContext.getPluginVarName() + "." + PROPERTY_RENDERER + "."
					+ JsChartRenderer.RENDER_FUNCTION_NAME + "(" + chart.getVarName() + ");");
			writeNewLine(out);
		}
	}

	protected HtmlChartPluginScriptObjectWriter getHtmlChartPluginScriptObjectWriter()
	{
		return HTML_CHART_PLUGIN_SCRIPT_OBJECT_WRITER;
	}

	protected HtmlRenderContextScriptObjectWriter getHtmlRenderContextScriptObjectWriter()
	{
		return HTML_RENDER_CONTEXT_SCRIPT_OBJECT_WRITER;
	}

	protected HtmlChartScriptObjectWriter getHtmlChartScriptObjectWriter()
	{
		return HTML_CHART_SCRIPT_OBJECT_WRITER;
	}

	protected void writeScriptStartTag(RenderContext renderContext, Writer out) throws IOException
	{
		out.write("<script type=\"text/javascript\">");
	}

	protected void writeScriptEndTag(RenderContext renderContext, Writer out) throws IOException
	{
		out.write("</script>");
	}

	protected void writeNewLine(Writer out) throws IOException
	{
		out.write(getNewLine());
	}
}
