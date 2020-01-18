/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.datagear.analysis.ChartDataSetFactory;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
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
 * <p>
 * 1.
 * HTML部分（{@linkplain HtmlRenderAttributes#getChartNotRenderElement(RenderContext)}为{@code true}则不输出）：
 * </p>
 * <code>
 * <pre>
 * &lt;div id="[图表HTML元素ID]" class="chart"&gt;&lt;/div&gt;
 * </pre>
 * </code>
 * <p>
 * {@linkplain HtmlRenderAttributes#setChartElementId(RenderContext, String)}可用于自定义“[图表HTML元素ID]”；
 * </p>
 * <p>
 * 2. JavaScript部分：
 * </p>
 * <code>
 * <pre>
 * &lt;script type="text/javascript"&gt;
 * var [图表变量名]=
 * {
 * 	id : "...",
 * 	elementId : "[图表HTML元素ID]",
 * 	varName : "[图表变量名]",
 * 	plugin : "插件ID",
 * 	renderContext : { attributes : {...} },
 * 	propertyValues : {...},
 * 	dataSetFactories : [{...}, ...]
 * };
 * [图表脚本内容]
 * [图表变量名].render();
 * &lt;/script&gt;
 * </pre>
 * </code>
 * <p>
 * {@linkplain HtmlRenderAttributes#setChartVarName(RenderContext, String)}可用于自定义“[图表变量名]”。
 * </p>
 * <p>
 * 如果{@linkplain HtmlRenderAttributes#getChartNotRenderScriptTag(RenderContext)}为{@code true}，
 * 那么上述JavaScript部分则不输出“script”开始和结束标签。
 * </p>
 * <p>
 * 如果{@linkplain HtmlRenderAttributes#getChartScriptNotInvokeRender(RenderContext)}为{@code true}，
 * 那么上述JavaScript部分则不输出“[图表变量名].render();”脚本。
 * </p>
 * <p>
 * 如果{@linkplain HtmlRenderAttributes#getChartRenderContextVarName(RenderContext)}不为空，
 * 那么上述JavaScript部分的“renderContext”将不会输出对象内容，而输出这个变量引用。
 * </p>
 * <p>
 * “[图表脚本内容]”格式应为：
 * </p>
 * <code>
 * <pre>
 * (function(chart)
 * {
 * 	chart.render = function(){ ... };
 * 	chart.update = function(dataSets){ ... };
 * })
 * ($CHART);
 * </pre>
 * </code>
 * <p>
 * 或者：
 * </p>
 * <code>
 * <pre>
 * $CHART.render = function(){ ... };
 * $CHART.update = function(dataSets){ ... };
 * </pre>
 * </code>
 * <p>
 * 其中，“<code>$CHART</code>”用作图表脚本对象占位符，会在输出时替换为“[图表变量名]”。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPlugin<T extends HtmlRenderContext> extends AbstractChartPlugin<T>
{
	/** 默认图表对象引用占位符 */
	public static final String DEFAULT_SCRIPT_CHART_REF_PLACEHOLDER = "$CHART";

	/** 默认图表脚本对象的渲染函数名 */
	public static final String SCRIPT_RENDER_FUNCTION_NAME = "render";

	/** 默认图表脚本对象的渲染函数名 */
	public static final String SCRIPT_UPDATE_FUNCTION_NAME = "update";

	/** 内置图表元素样式名，所有图标元素都要加此样式名 */
	public static final String BUILTIN_CHART_ELEMENT_STYLE_NAME = "chart";

	/** 图表脚本内容 */
	private ScriptContent scriptContent;

	/** {@linkplain #scriptContent}中的图表对象引用占位符，在输出时，占位符会被替换为具体的图表对象名 */
	private String scriptChartRefPlaceholder = DEFAULT_SCRIPT_CHART_REF_PLACEHOLDER;

	/** 图表HTML元素标签名 */
	private String elementTagName = "div";

	/** 图表HTML元素CSS样式名 */
	private String elementStyleName = "";

	/** 图表脚本换行符 */
	private String newLine = "\r\n";

	private HtmlChartScriptObjectWriter htmlChartScriptObjectWriter = new HtmlChartScriptObjectWriter();

	public HtmlChartPlugin()
	{
		super();
	}

	public HtmlChartPlugin(String id, Label nameLabel, ScriptContent scriptContent)
	{
		super(id, nameLabel);
		this.scriptContent = scriptContent;
	}

	public ScriptContent getScriptContent()
	{
		return scriptContent;
	}

	public void setScriptContent(ScriptContent scriptContent)
	{
		this.scriptContent = scriptContent;
	}

	public String getScriptChartRefPlaceholder()
	{
		return scriptChartRefPlaceholder;
	}

	public void setScriptChartRefPlaceholder(String scriptChartRefPlaceholder)
	{
		this.scriptChartRefPlaceholder = scriptChartRefPlaceholder;
	}

	public String getElementTagName()
	{
		return elementTagName;
	}

	public void setElementTagName(String elementTagName)
	{
		this.elementTagName = elementTagName;
	}

	public String getElementStyleName()
	{
		return elementStyleName;
	}

	public void setElementStyleName(String elementStyleName)
	{
		this.elementStyleName = elementStyleName;
	}

	public String getNewLine()
	{
		return newLine;
	}

	public void setNewLine(String newLine)
	{
		this.newLine = newLine;
	}

	public HtmlChartScriptObjectWriter getHtmlChartScriptObjectWriter()
	{
		return htmlChartScriptObjectWriter;
	}

	public void setHtmlChartScriptObjectWriter(HtmlChartScriptObjectWriter htmlChartScriptObjectWriter)
	{
		this.htmlChartScriptObjectWriter = htmlChartScriptObjectWriter;
	}

	@Override
	public HtmlChart renderChart(T renderContext, Map<String, ?> chartPropertyValues,
			ChartDataSetFactory... chartDataSetFactories) throws RenderException
	{
		boolean notRenderElement = HtmlRenderAttributes.getChartNotRenderElement(renderContext);
		String chartElementId = HtmlRenderAttributes.getChartElementId(renderContext);
		int nextSequence = -1;

		if (notRenderElement)
		{
			if (StringUtil.isEmpty(chartElementId))
				throw new RenderException("[" + HtmlRenderAttributes.CHART_ELEMENT_ID + "] attribute must be set");
		}
		else
		{
			if (StringUtil.isEmpty(chartElementId))
			{
				nextSequence = HtmlRenderAttributes.getNextSequenceIfNot(renderContext, nextSequence);
				chartElementId = HtmlRenderAttributes.generateChartElementId(nextSequence);
			}

			writeChartElement(renderContext, chartElementId);
		}

		String chartVarName = HtmlRenderAttributes.getChartVarName(renderContext);
		if (StringUtil.isEmpty(chartVarName))
		{
			nextSequence = HtmlRenderAttributes.getNextSequenceIfNot(renderContext, nextSequence);
			chartVarName = HtmlRenderAttributes.generateChartVarName(nextSequence);
		}

		HtmlChart chart = new HtmlChart(IDUtil.uuid(), renderContext, this, chartPropertyValues, chartDataSetFactories,
				chartElementId, chartVarName);

		try
		{
			writeChartScript(renderContext, chart);
		}
		catch (IOException e)
		{
			throw new RenderException(e);
		}

		return chart;
	}

	/**
	 * 写用于渲染图表的HTML元素。
	 * 
	 * @param renderContext
	 * @param chartElementId
	 * @throws RenderException
	 */
	protected void writeChartElement(T renderContext, String chartElementId) throws RenderException
	{
		Writer writer = renderContext.getWriter();

		try
		{
			writer.write("<" + this.elementTagName + " id=\"" + chartElementId + "\" class=\""
					+ BUILTIN_CHART_ELEMENT_STYLE_NAME
					+ (StringUtil.isEmpty(this.elementStyleName) ? "" : " " + this.elementStyleName) + "\">");
			writer.write("</" + this.elementTagName + ">");
			writeNewLine(writer);
		}
		catch (IOException e)
		{
			throw new RenderException(e);
		}
	}

	/**
	 * 写图表脚本。
	 * 
	 * @param renderContext
	 * @param chart
	 * @throws IOException
	 */
	protected void writeChartScript(T renderContext, HtmlChart chart) throws IOException
	{
		boolean inScriptContext = HtmlRenderAttributes.getChartNotRenderScriptTag(renderContext);

		Writer out = renderContext.getWriter();

		if (!inScriptContext)
		{
			writeScriptStartTag(renderContext);
			writeNewLine(out);
		}

		out.write("var ");
		out.write(chart.getVarName());
		out.write("=");
		writeNewLine(out);
		writeChartScriptObject(renderContext, chart);
		out.write(";");
		writeNewLine(out);
		writeChartScriptContent(renderContext, chart);
		writeNewLine(out);

		if (!HtmlRenderAttributes.getChartScriptNotInvokeRender(renderContext))
		{
			out.write(chart.getVarName() + "." + SCRIPT_RENDER_FUNCTION_NAME + "();");
			writeNewLine(out);
			out.write(chart.getVarName() + "." + SCRIPT_UPDATE_FUNCTION_NAME + "();");
			writeNewLine(out);
		}

		if (!inScriptContext)
		{
			writeScriptEndTag(renderContext);
			writeNewLine(out);
		}
	}

	/**
	 * 写图表脚本对象，格式为：<code>{...}</code>。
	 * 
	 * @param renderContext
	 * @param chart
	 * @throws IOException
	 */
	protected void writeChartScriptObject(T renderContext, HtmlChart chart) throws IOException
	{
		this.htmlChartScriptObjectWriter.write(renderContext.getWriter(), chart,
				HtmlRenderAttributes.getChartRenderContextVarName(renderContext));
	}

	/**
	 * 写图表脚本内容。
	 * 
	 * @param renderContext
	 * @param chart
	 * @throws IOException
	 */
	protected void writeChartScriptContent(T renderContext, HtmlChart chart) throws IOException
	{
		Writer out = renderContext.getWriter();
		String chartVarName = chart.getVarName();

		Reader reader = this.scriptContent.getReader();
		BufferedReader bufferedReader = (reader instanceof BufferedReader ? (BufferedReader) reader
				: new BufferedReader(reader));

		try
		{
			String line = null;
			while ((line = bufferedReader.readLine()) != null)
			{
				out.write(replaceChartRefPlaceholder(line, chartVarName));
				writeNewLine(out);
			}
		}
		finally
		{
			IOUtil.close(reader);
		}
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
	 * 替换字符串中的图表名引用占位符为真是的图表变量名。
	 * 
	 * @param str
	 * @param chartRefName
	 * @return
	 */
	protected String replaceChartRefPlaceholder(String str, String chartRefName)
	{
		if (StringUtil.isEmpty(str))
			return str;

		return str.replace(this.scriptChartRefPlaceholder, chartRefName);
	}
}
