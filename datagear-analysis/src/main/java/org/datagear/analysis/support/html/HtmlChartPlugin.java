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
import java.util.concurrent.atomic.AtomicInteger;

import org.datagear.analysis.Chart;
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
 * 如果{@linkplain HtmlRenderAttributes#getChartNotRenderScriptTag(RenderContext)}为{@code true}，
 * 那么上述JavaScript部分则不输出“script”开始和结束标签。
 * </p>
 * <p>
 * {@linkplain HtmlRenderAttributes#setChartVarName(RenderContext, String)}可用于自定义“[图表变量名]”。
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
	/** 默认图表对象名占位符 */
	public static final String DEFAULT_CHART_SCRIPT_VAR_NAME_PLACEHOLDER = "$CHART";

	/** 默认图表脚本对象的渲染函数名 */
	public static final String DEFAULT_CHART_SCRIPT_RENDER_FUNCTION_NAME = "render";

	/** 图表脚本内容 */
	private String chartScriptContent;

	/** {@linkplain #chartScriptContent}中的图表对象名占位符，在输出时，占位符会被替换为具体的图表对象名 */
	private String chartScriptVarNamePlaceholder = DEFAULT_CHART_SCRIPT_VAR_NAME_PLACEHOLDER;

	/** 图表脚本对象的渲染函数名 */
	private String chartScriptRenderFunctionName = DEFAULT_CHART_SCRIPT_RENDER_FUNCTION_NAME;

	/** 图表HTML元素标签名 */
	private String chartElementTagName = "div";

	/** 图表HTML元素CSS样式名 */
	private String chartElementStyleName = "chart";

	/** 图表脚本资源文件的编码 */
	private String chartScriptResourceEncoding = "UTF-8";

	/** 图表脚本换行符 */
	private String scriptNewLine = "\r\n";

	private HtmlChartScriptObjectWriter htmlChartScriptObjectWriter = new HtmlChartScriptObjectWriter();

	private AtomicInteger chartElementIdSequence = new AtomicInteger(1);

	private AtomicInteger chartVarNameSequence = new AtomicInteger(1);

	public HtmlChartPlugin()
	{
		super();
	}

	public HtmlChartPlugin(String id, Label nameLabel, String chartScriptContent)
	{
		super(id, nameLabel);
		this.chartScriptContent = chartScriptContent;
	}

	public String getChartScriptContent()
	{
		return chartScriptContent;
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
	public void setChartScriptContent(String chartScriptContent)
	{
		this.chartScriptContent = chartScriptContent;
	}

	public String getChartScriptVarNamePlaceholder()
	{
		return chartScriptVarNamePlaceholder;
	}

	public void setChartScriptVarNamePlaceholder(String chartScriptVarNamePlaceholder)
	{
		this.chartScriptVarNamePlaceholder = chartScriptVarNamePlaceholder;
	}

	public String getChartScriptRenderFunctionName()
	{
		return chartScriptRenderFunctionName;
	}

	public void setChartScriptRenderFunctionName(String chartScriptRenderFunctionName)
	{
		this.chartScriptRenderFunctionName = chartScriptRenderFunctionName;
	}

	public String getChartElementTagName()
	{
		return chartElementTagName;
	}

	public void setChartElementTagName(String chartElementTagName)
	{
		this.chartElementTagName = chartElementTagName;
	}

	public String getChartElementStyleName()
	{
		return chartElementStyleName;
	}

	public void setChartElementStyleName(String chartElementStyleName)
	{
		this.chartElementStyleName = chartElementStyleName;
	}

	public String getChartScriptResourceEncoding()
	{
		return chartScriptResourceEncoding;
	}

	public void setChartScriptResourceEncoding(String chartScriptResourceEncoding)
	{
		this.chartScriptResourceEncoding = chartScriptResourceEncoding;
	}

	public String getScriptNewLine()
	{
		return scriptNewLine;
	}

	public void setScriptNewLine(String scriptNewLine)
	{
		this.scriptNewLine = scriptNewLine;
	}

	public HtmlChartScriptObjectWriter getHtmlChartScriptObjectWriter()
	{
		return htmlChartScriptObjectWriter;
	}

	public void setHtmlChartScriptObjectWriter(HtmlChartScriptObjectWriter htmlChartScriptObjectWriter)
	{
		this.htmlChartScriptObjectWriter = htmlChartScriptObjectWriter;
	}

	public AtomicInteger getChartElementIdSequence()
	{
		return chartElementIdSequence;
	}

	public void setChartElementIdSequence(AtomicInteger chartElementIdSequence)
	{
		this.chartElementIdSequence = chartElementIdSequence;
	}

	public AtomicInteger getChartVarNameSequence()
	{
		return chartVarNameSequence;
	}

	public void setChartVarNameSequence(AtomicInteger chartVarNameSequence)
	{
		this.chartVarNameSequence = chartVarNameSequence;
	}

	@Override
	public Chart renderChart(T renderContext, ChartPropertyValues chartPropertyValues,
			DataSetFactory... dataSetFactories) throws RenderException
	{
		boolean notRenderElement = HtmlRenderAttributes.getChartNotRenderElement(renderContext);
		String chartElementId = HtmlRenderAttributes.getChartElementId(renderContext);

		if (notRenderElement)
		{
			if (StringUtil.isEmpty(chartElementId))
				throw new RenderException("[" + HtmlRenderAttributes.CHART_ELEMENT_ID + "] attribute must be set");
		}
		else
		{
			if (StringUtil.isEmpty(chartElementId))
				chartElementId = HtmlRenderAttributes.generateChartElementId(chartElementIdSequence.getAndIncrement());

			writeChartElement(renderContext, chartElementId);
		}

		String chartVarName = HtmlRenderAttributes.getChartVarName(renderContext);
		if (StringUtil.isEmpty(chartVarName))
			chartVarName = HtmlRenderAttributes.generateChartVarName(chartVarNameSequence.getAndIncrement());

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
			writer.write("<" + this.chartElementTagName + " id=\"" + chartElementId + "\" class=\""
					+ this.chartElementStyleName + "\">");
			writer.write("</" + this.chartElementTagName + ">");
			writer.write(this.scriptNewLine);
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
			out.write(this.scriptNewLine);
		}

		out.write("var ");
		out.write(chart.getChartVarName());
		out.write("=");
		out.write(this.scriptNewLine);
		writeChartScriptObject(renderContext, chart);
		out.write(";");
		out.write(this.scriptNewLine);
		writeChartScriptContent(renderContext, chart);
		out.write(this.scriptNewLine);
		out.write(chart.getChartVarName() + "." + this.chartScriptRenderFunctionName + "();");
		out.write(this.scriptNewLine);

		if (!inScriptContext)
		{
			writeScriptEndTag(renderContext);
			out.write(this.scriptNewLine);
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
		String chartVarName = chart.getChartVarName();

		if (LocationResource.isFileLocation(this.chartScriptContent)
				|| LocationResource.isClasspathLocation(this.chartScriptContent))
		{
			LocationResource resource = new LocationResource(this.chartScriptContent);
			BufferedReader reader = IOUtil.getReader(resource.getInputStream(), this.chartScriptResourceEncoding);

			try
			{
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					out.write(replaceChartVarNameRefPlaceholder(line, chartVarName));
					out.write(this.scriptNewLine);
				}
			}
			finally
			{
				IOUtil.close(reader);
			}
		}
		else
		{
			out.write(replaceChartVarNameRefPlaceholder(this.chartScriptContent, chartVarName));
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
	 * @param chartVarName
	 * @return
	 */
	protected String replaceChartVarNameRefPlaceholder(String str, String chartVarName)
	{
		if (StringUtil.isEmpty(str))
			return str;

		return str.replace(this.chartScriptVarNamePlaceholder, chartVarName);
	}
}
