/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * HTML图表插件。
 * <p>
 * 此类将图表代码（HTML、JavaScript）输出至{@linkplain HtmlRenderContext#getWriter()}。
 * </p>
 * <p>
 * 输出格式为：
 * </p>
 * <code>
 * <pre>
 * &lt;div id="[图表HTML元素ID]"&gt;&lt;/div&gt;
 * &lt;script type="text/javascript"&gt;
 * var [图表插件变量名]={..., chartRenderer: {...}};
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
 * [图表插件变量名].chartRenderer.render([图表变量名]);
 * &lt;/script&gt;
 * </pre>
 * </code>
 * <p>
 * {@linkplain HtmlChartPluginRenderOption}可自定义上述输出内容。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPlugin<T extends HtmlRenderContext> extends AbstractChartPlugin<T>
{
	public static final String PROPERTY_CHART_RENDERER = "chartRenderer";

	/** HTML换行符 */
	public static final String HTML_NEW_LINE = "\n";

	protected static final HtmlChartPluginScriptObjectWriter HTML_CHART_PLUGIN_SCRIPT_OBJECT_WRITER = new HtmlChartPluginScriptObjectWriter();
	protected static final HtmlRenderContextScriptObjectWriter HTML_RENDER_CONTEXT_SCRIPT_OBJECT_WRITER = new HtmlRenderContextScriptObjectWriter();
	protected static final HtmlChartScriptObjectWriter HTML_CHART_SCRIPT_OBJECT_WRITER = new HtmlChartScriptObjectWriter();

	/** JS图表渲染器 */
	private JsChartRenderer chartRenderer;

	/** 图表HTML元素标签名 */
	private String elementTagName = "div";

	/** 图表脚本换行符 */
	private String newLine = HTML_NEW_LINE;

	public HtmlChartPlugin()
	{
		super();
	}

	public HtmlChartPlugin(String id, Label nameLabel, JsChartRenderer chartRenderer)
	{
		super(id, nameLabel);
		this.chartRenderer = chartRenderer;
	}

	public JsChartRenderer getChartRenderer()
	{
		return chartRenderer;
	}

	public void setChartRenderer(JsChartRenderer chartRenderer)
	{
		this.chartRenderer = chartRenderer;
	}

	public String getElementTagName()
	{
		return elementTagName;
	}

	public void setElementTagName(String elementTagName)
	{
		this.elementTagName = elementTagName;
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
	public Chart renderChart(T renderContext, ChartDefinition chartDefinition) throws RenderException
	{
		HtmlChartPluginRenderOption option = getOptionInitialized(renderContext);

		HtmlChart chart = createHtmlChart(renderContext, chartDefinition, option);

		try
		{
			writeChartElement(renderContext, option);
			writeScript(renderContext, chart, option);
		}
		catch (IOException e)
		{
			throw new RenderException(e);
		}

		return chart;
	}

	protected HtmlChart createHtmlChart(T renderContext, ChartDefinition chartDefinition,
			HtmlChartPluginRenderOption option)
	{
		return new HtmlChart(chartDefinition, this, renderContext,
				option.getChartElementId(), option.getChartVarName());
	}

	protected boolean writeChartElement(T renderContext, HtmlChartPluginRenderOption option) throws IOException
	{
		if (option.isNotWriteChartElement())
			return false;

		Writer writer = renderContext.getWriter();

		writer.write("<" + this.elementTagName + " id=\"" + option.getChartElementId() + "\">");
		writer.write("</" + this.elementTagName + ">");
		writeNewLine(writer);

		return true;
	}

	protected void writeScript(T renderContext, HtmlChart chart, HtmlChartPluginRenderOption optionInitialized)
			throws IOException
	{
		Writer out = renderContext.getWriter();

		if (!optionInitialized.isNotWriteScriptTag())
		{
			writeScriptStartTag(renderContext);
			writeNewLine(out);
		}

		writePluginJsObject(renderContext, chart, optionInitialized);
		writeRenderContextJsObject(renderContext, chart, optionInitialized);
		writeChartJsObject(renderContext, chart, optionInitialized);

		if (!optionInitialized.isNotWriteScriptTag())
		{
			writeScriptEndTag(renderContext);
			writeNewLine(out);
		}
	}

	protected boolean writePluginJsObject(T renderContext, HtmlChart chart,
			HtmlChartPluginRenderOption optionInitialized) throws IOException
	{
		if (optionInitialized.isNotWritePluginObject())
			return false;

		Writer out = renderContext.getWriter();
		HtmlChartPlugin<?> plugin = chart.getPlugin();

		getHtmlChartPluginScriptObjectWriter().write(out, plugin, optionInitialized.getPluginVarName());

		return true;
	}

	protected boolean writeRenderContextJsObject(T renderContext, HtmlChart chart,
			HtmlChartPluginRenderOption optionInitialized) throws IOException
	{
		if (optionInitialized.isNotWriteRenderContextObject())
			return false;

		Writer out = renderContext.getWriter();
		getHtmlRenderContextScriptObjectWriter().write(out, renderContext, optionInitialized.getRenderContextVarName());

		return true;
	}

	protected void writeChartJsObject(T renderContext, HtmlChart chart, HtmlChartPluginRenderOption optionInitialized)
			throws IOException
	{
		Writer out = renderContext.getWriter();

		getHtmlChartScriptObjectWriter().write(out, chart, optionInitialized.getRenderContextVarName(),
				optionInitialized.getPluginVarName());

		if (!optionInitialized.isNotWriteInvoke())
		{
			out.write(optionInitialized.getPluginVarName() + "." + PROPERTY_CHART_RENDERER + "."
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

	/**
	 * 写脚本开始标签。
	 * 
	 * @param renderContext
	 * @throws IOException
	 */
	protected void writeScriptStartTag(T renderContext) throws IOException
	{
		renderContext.getWriter().write("<script type=\"text/javascript\">");
	}

	/**
	 * 写脚本结束标签。
	 * 
	 * @param renderContext
	 * @throws IOException
	 */
	protected void writeScriptEndTag(T renderContext) throws IOException
	{
		renderContext.getWriter().write("</script>");
	}

	/**
	 * 写换行符。
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void writeNewLine(Writer out) throws IOException
	{
		out.write(getNewLine());
	}

	/**
	 * 获取已初始化的{@linkplain HtmlChartPluginRenderOption}。
	 * <p>
	 * 它的{@linkplain HtmlChartPluginRenderOption#getChartElementId()}、
	 * {@linkplain HtmlChartPluginRenderOption#getPluginVarName()}、
	 * {@linkplain HtmlChartPluginRenderOption#getRenderContextVarName()}、
	 * {@linkplain HtmlChartPluginRenderOption#getChartVarName()}都是已初始化的。
	 * </p>
	 * 
	 * @param renderContext
	 * @return
	 */
	protected HtmlChartPluginRenderOption getOptionInitialized(T renderContext)
	{
		HtmlChartPluginRenderOption option = HtmlChartPluginRenderOption.getOption(renderContext);
		if (option == null)
		{
			option = new HtmlChartPluginRenderOption();
			option.setNotWriteChartElement(false);
			option.setNotWritePluginObject(false);
			option.setNotWriteRenderContextObject(false);
			option.setNotWriteScriptTag(false);
			option.setNotWriteInvoke(false);
		}

		String chartElementId = option.getChartElementId();
		String pluginVarName = option.getPluginVarName();
		String renderContextVarName = option.getRenderContextVarName();
		String chartVarName = option.getChartVarName();

		if (option.isNotWriteChartElement() && StringUtil.isEmpty(chartElementId))
			throw new RenderException(
					"[" + HtmlChartPluginRenderOption.class.getSimpleName() + ".elementId] must be set");

		if (StringUtil.isEmpty(chartElementId))
		{
			chartElementId = HtmlRenderAttributes.generateChartElementId(renderContext);
			option.setChartElementId(chartElementId);
		}

		if (StringUtil.isEmpty(pluginVarName))
		{
			pluginVarName = HtmlRenderAttributes.generateChartPluginVarName(renderContext);
			option.setPluginVarName(pluginVarName);
		}

		if (StringUtil.isEmpty(renderContextVarName))
		{
			renderContextVarName = HtmlRenderAttributes.generateRenderContextVarName(renderContext);
			option.setRenderContextVarName(renderContextVarName);
		}

		if (StringUtil.isEmpty(chartVarName))
		{
			chartVarName = HtmlRenderAttributes.generateChartVarName(renderContext);
			option.setChartVarName(chartVarName);
		}

		return option;
	}
}
