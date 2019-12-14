/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import org.datagear.analysis.ChartPropertyValues;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.analysis.support.LocationResource;
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
 * 	chartElementId : "[图表HTML元素ID]",
 * 	chartVarName : "[图表变量名]",
 * 	chartPlugin : "插件ID",
 * 	renderContext : { attributes : {...} },
 * 	chartPropertyValues : {...},
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
 * 	chart.updateData = function(dataSets){ ... };
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
 * $CHART.updateData = function(dataSets){ ... };
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
	public static final String DEFAULT_SCRIPT_RENDER_FUNCTION_NAME = "render";

	/** 内置图表元素样式名，所有图标元素都要加此样式名 */
	public static final String BUILTIN_CHART_ELEMENT_STYLE_NAME = "chart";

	/** 图表脚本内容 */
	private String scriptContent;

	/** {@linkplain #scriptContent}中的图表对象引用占位符，在输出时，占位符会被替换为具体的图表对象名 */
	private String scriptChartRefPlaceholder = DEFAULT_SCRIPT_CHART_REF_PLACEHOLDER;

	/** 图表脚本对象的渲染函数名 */
	private String scriptRenderFunctionName = DEFAULT_SCRIPT_RENDER_FUNCTION_NAME;

	/** 图表HTML元素标签名 */
	private String elementTagName = "div";

	/** 图表HTML元素CSS样式名 */
	private String elementStyleName = "";

	/** 图表脚本内容资源文件的编码 */
	private String scriptContentEncoding = "UTF-8";

	/** 图表脚本换行符 */
	private String newLine = "\r\n";

	private HtmlChartScriptObjectWriter htmlChartScriptObjectWriter = new HtmlChartScriptObjectWriter();

	public HtmlChartPlugin()
	{
		super();
	}

	public HtmlChartPlugin(String id, Label nameLabel, String scriptContent)
	{
		super(id, nameLabel);
		this.scriptContent = scriptContent;
	}

	public String getScriptContent()
	{
		return scriptContent;
	}

	/**
	 * 设置图表脚本内容。
	 * <p>
	 * {@code "classpath:..."}：表示类路径资源脚本内容；
	 * </p>
	 * <p>
	 * {@code "file:..."}：表示文件路径资源脚本内容；
	 * </p>
	 * <p>
	 * 其他：表示直接是脚本内容。
	 * </p>
	 * 
	 * @param chartScriptContent
	 */
	public void setScriptContent(String scriptContent)
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

	public String getScriptRenderFunctionName()
	{
		return scriptRenderFunctionName;
	}

	public void setScriptRenderFunctionName(String scriptRenderFunctionName)
	{
		this.scriptRenderFunctionName = scriptRenderFunctionName;
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

	public String getScriptContentEncoding()
	{
		return scriptContentEncoding;
	}

	public void setScriptContentEncoding(String scriptContentEncoding)
	{
		this.scriptContentEncoding = scriptContentEncoding;
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
	public HtmlChart renderChart(T renderContext, ChartPropertyValues chartPropertyValues,
			DataSetFactory... dataSetFactories) throws RenderException
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

		HtmlChart chart = new HtmlChart(IDUtil.uuid(), this, renderContext, chartPropertyValues, dataSetFactories,
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
			writer.write(this.newLine);
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
			out.write(this.newLine);
		}

		out.write("var ");
		out.write(chart.getVarName());
		out.write("=");
		out.write(this.newLine);
		writeChartScriptObject(renderContext, chart);
		out.write(";");
		out.write(this.newLine);
		writeChartScriptContent(renderContext, chart);
		out.write(this.newLine);

		if (!HtmlRenderAttributes.getChartScriptNotInvokeRender(renderContext))
		{
			out.write(chart.getVarName() + "." + this.scriptRenderFunctionName + "();");
			out.write(this.newLine);
		}

		if (!inScriptContext)
		{
			writeScriptEndTag(renderContext);
			out.write(this.newLine);
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

		if (LocationResource.isFileLocation(this.scriptContent)
				|| LocationResource.isClasspathLocation(this.scriptContent))
		{
			LocationResource resource = new LocationResource(this.scriptContent);
			BufferedReader reader = IOUtil.getReader(resource.getInputStream(), this.scriptContentEncoding);

			try
			{
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					out.write(replaceChartRefPlaceholder(line, chartVarName));
					out.write(this.newLine);
				}
			}
			finally
			{
				IOUtil.close(reader);
			}
		}
		else
		{
			out.write(replaceChartRefPlaceholder(this.scriptContent, chartVarName));
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
