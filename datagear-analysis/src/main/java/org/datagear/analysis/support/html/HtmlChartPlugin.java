/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.analysis.support.html.HtmlChartRenderAttr.HtmlChartRenderOption;
import org.datagear.util.i18n.Label;

/**
 * HTML图表插件。
 * <p>
 * 此类将图表代码（HTML、JavaScript）输出至{@linkplain HtmlRenderAttr#getHtmlWriter(RenderContext)}。
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
 * <p>
 * {@linkplain HtmlChartRenderAttr#getRenderOption(RenderContext)}可自定义上述输出内容。
 * </p>
 * <p>
 * 注意：此类{@linkplain #renderChart(RenderContext, ChartDefinition)}的{@linkplain RenderContext}必须符合{@linkplain HtmlChartRenderAttr#inflate(RenderContext, Writer, HtmlChartRenderOption)}规范。
 * </p>
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
	public HtmlChart renderChart(RenderContext renderContext, ChartDefinition chartDefinition) throws RenderException
	{
		HtmlChartRenderAttr renderAttr = getHtmlChartRenderAttrNonNull(renderContext);
		Writer out = renderAttr.getHtmlWriterNonNull(renderContext);
		HtmlChartRenderOption option = renderAttr.getRenderOptionNonNull(renderContext);

		HtmlChart chart = createHtmlChart(renderContext, chartDefinition, option);

		try
		{
			writeChartElement(renderContext, renderAttr, out, option);
			writeScript(renderContext, renderAttr, out, chart, option);
		}
		catch (IOException e)
		{
			throw new RenderException(e);
		}

		return chart;
	}

	protected HtmlChart createHtmlChart(RenderContext renderContext, ChartDefinition chartDefinition,
			HtmlChartRenderOption option)
	{
		return new HtmlChart(chartDefinition, this, renderContext, option.getChartElementId(),
				option.getChartVarName());
	}

	protected boolean writeChartElement(RenderContext renderContext, HtmlChartRenderAttr renderAttr, Writer out,
			HtmlChartRenderOption option) throws IOException
	{
		if (option.isNotWriteChartElement())
			return false;

		out.write("<" + this.elementTagName + " id=\"" + option.getChartElementId() + "\">");
		out.write("</" + this.elementTagName + ">");
		writeNewLine(out);

		return true;
	}

	protected void writeScript(RenderContext renderContext, HtmlChartRenderAttr renderAttr, Writer out, HtmlChart chart,
			HtmlChartRenderOption optionInitialized) throws IOException
	{
		if (!optionInitialized.isNotWriteScriptTag())
		{
			writeScriptStartTag(renderContext, out);
			writeNewLine(out);
		}

		writePluginJsObject(renderContext, renderAttr, out, chart, optionInitialized);
		writeRenderContextJsObject(renderContext, renderAttr, out, chart, optionInitialized);
		writeChartJsObject(renderContext, renderAttr, out, chart, optionInitialized);

		if (!optionInitialized.isNotWriteScriptTag())
		{
			writeScriptEndTag(renderContext, out);
			writeNewLine(out);
		}
	}

	protected boolean writePluginJsObject(RenderContext renderContext, HtmlChartRenderAttr renderAttr, Writer out,
			HtmlChart chart, HtmlChartRenderOption optionInitialized) throws IOException
	{
		if (optionInitialized.isNotWritePluginObject())
			return false;

		HtmlChartPlugin plugin = chart.getPlugin();

		getHtmlChartPluginScriptObjectWriter().write(out, plugin, optionInitialized.getPluginVarName(),
				renderAttr.getLocale(renderContext));

		return true;
	}

	protected boolean writeRenderContextJsObject(RenderContext renderContext, HtmlChartRenderAttr renderAttr,
			Writer out, HtmlChart chart, HtmlChartRenderOption optionInitialized) throws IOException
	{
		if (optionInitialized.isNotWriteRenderContextObject())
			return false;

		getHtmlRenderContextScriptObjectWriter().write(out, renderContext, optionInitialized.getRenderContextVarName(),
				getHtmlRenderContextIgnoreAttrs(renderContext, renderAttr, out, chart));

		return true;
	}

	protected Collection<String> getHtmlRenderContextIgnoreAttrs(RenderContext renderContext,
			HtmlChartRenderAttr renderAttr, Writer out, HtmlChart chart)
	{
		Collection<String> ignores = renderAttr.getIgnoreRenderAttrs(renderContext);
		return (ignores != null ? ignores : Arrays.asList(renderAttr.getHtmlWriterName()));
	}

	protected void writeChartJsObject(RenderContext renderContext, HtmlChartRenderAttr renderAttr, Writer out,
			HtmlChart chart, HtmlChartRenderOption optionInitialized) throws IOException
	{
		if (optionInitialized.isWriteChartJson())
			getHtmlChartScriptObjectWriter().writeJson(out, chart, optionInitialized.getRenderContextVarName(),
					optionInitialized.getPluginVarName());
		else
			getHtmlChartScriptObjectWriter().write(out, chart, optionInitialized.getRenderContextVarName(),
					optionInitialized.getPluginVarName());

		if (!optionInitialized.isNotWriteInvoke())
		{
			out.write(optionInitialized.getPluginVarName() + "." + PROPERTY_RENDERER + "."
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

	protected HtmlChartRenderAttr getHtmlChartRenderAttrNonNull(RenderContext renderContext)
	{
		return HtmlChartRenderAttr.getNonNull(renderContext);
	}
}
