/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.Chart;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.html.HtmlChartRenderAttr.HtmlChartRenderOption;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 使用原生HTML网页作为模板的{@linkplain HtmlTplDashboardWidget}渲染器。
 * <p>
 * 此类可渲染由{@linkplain TemplateDashboardWidgetResManager}管理模板资源的{@linkplain HtmlTplDashboardWidget}。
 * </p>
 * <p>
 * 支持的模板格式如下：
 * </p>
 * <code>
 * <pre>
 * ...
 * &lt;html dg-dashboard-factory="..." dg-dashboard-var="..." dg-dashboard-import-exclude="..."&gt;
 * ...
 * &lt;head&gt;
 * ...
 * &lt;/head&gt;
 * ...
 * &lt;body&gt;
 * ...
 * &lt;div id="..." dg-chart-widget="..."&gt;&lt;/div&gt;
 * ...
 * &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * </code>
 * <p>
 * <code>html dg-dashboard-factory</code>：选填，定义看板工厂JS对象的变量名，默认为{@linkplain HtmlTplDashboardWidgetRenderer#getDefaultDashboardFactoryVar()}
 * </p>
 * <p>
 * <code>html dg-dashboard-var</code>：选填，定义看板JS对象的变量名，默认为{@linkplain #getDefaultDashboardVar()}
 * </p>
 * <p>
 * <code>html dg-dashboard-import-exclude</code>：选填，定义看板网页不加载的内置库（{@linkplain HtmlTplDashboardWidgetRenderer#getDashboardImports()}），多个以“,”隔开
 * </p>
 * <p>
 * <code>div id</code>：选填，定义图表元素ID，如果不填，则会自动生成一个
 * </p>
 * <p>
 * <code>div dg-chart-widget</code>：必填，定义图表部件ID（{@linkplain HtmlChartWidget#getId()}）
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class HtmlTplDashboardWidgetHtmlRenderer extends HtmlTplDashboardWidgetRenderer
{
	public static final String DEFAULT_DASHBOARD_SET_TAG_NAME = "html";

	public static final String DEFAULT_CHART_TAG_NAME = "div";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_VAR = "dg-dashboard-var";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_FACTORY = "dg-dashboard-factory";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_IMPORT_EXCLUDE = "dg-dashboard-import-exclude";

	public static final String DEFAULT_ATTR_NAME_CHART_WIDGET = "dg-chart-widget";

	/** 看板设置标签名 */
	private String dashboardSetTagName = DEFAULT_DASHBOARD_SET_TAG_NAME;

	/** 属性名：看板JS变量名 */
	private String attrNameDashboardVar = DEFAULT_ATTR_NAME_DASHBOARD_VAR;

	/** 属性名：看板工厂JS变量名 */
	private String attrNameDashboardFactory = DEFAULT_ATTR_NAME_DASHBOARD_FACTORY;

	/** 属性名：不导入内置库的 */
	private String attrNameDashboardImportExclude = DEFAULT_ATTR_NAME_DASHBOARD_IMPORT_EXCLUDE;

	/** 图表标签名 */
	private String chartTagName = DEFAULT_CHART_TAG_NAME;

	/** 属性名：图表部件ID */
	private String attrNameChartWidget = DEFAULT_ATTR_NAME_CHART_WIDGET;

	public HtmlTplDashboardWidgetHtmlRenderer()
	{
		super();
	}

	public HtmlTplDashboardWidgetHtmlRenderer(TemplateDashboardWidgetResManager templateDashboardWidgetResManager,
			ChartWidgetSource chartWidgetSource)
	{
		super(templateDashboardWidgetResManager, chartWidgetSource);
	}

	public String getDashboardSetTagName()
	{
		return dashboardSetTagName;
	}

	public void setDashboardSetTagName(String dashboardSetTagName)
	{
		this.dashboardSetTagName = dashboardSetTagName;
	}

	public String getAttrNameDashboardVar()
	{
		return attrNameDashboardVar;
	}

	public void setAttrNameDashboardVar(String attrNameDashboardVar)
	{
		this.attrNameDashboardVar = attrNameDashboardVar;
	}

	public String getAttrNameDashboardRenderer()
	{
		return attrNameDashboardFactory;
	}

	public void setAttrNameDashboardRenderer(String attrNameDashboardRenderer)
	{
		this.attrNameDashboardFactory = attrNameDashboardRenderer;
	}

	public String getAttrNameDashboardImportExclude()
	{
		return attrNameDashboardImportExclude;
	}

	public void setAttrNameDashboardImportExclude(String attrNameDashboardImportExclude)
	{
		this.attrNameDashboardImportExclude = attrNameDashboardImportExclude;
	}

	public String getChartTagName()
	{
		return chartTagName;
	}

	public void setChartTagName(String chartTagName)
	{
		this.chartTagName = chartTagName;
	}

	public String getAttrNameChartWidget()
	{
		return attrNameChartWidget;
	}

	public void setAttrNameChartWidget(String attrNameChartWidget)
	{
		this.attrNameChartWidget = attrNameChartWidget;
	}

	@Override
	public String simpleTemplateContent(String htmlCharset, String... chartWidgetId)
	{
		return simpleTemplateContent(htmlCharset, "", "", chartWidgetId, "");
	}

	/**
	 * 获取简单模板内容。
	 * 
	 * @param htmlCharset
	 * @param htmlTitle
	 * @param customChartCssAttrs
	 *            自定义图表样式属性，允许为{@code null}
	 * @param chartWidgetId
	 * @param chartEleAttrs
	 *            图表元素属性，允许为{@code null}
	 * @return
	 */
	public String simpleTemplateContent(String htmlCharset, String htmlTitle, String customChartCssAttrs,
			String[] chartWidgetId, String chartEleAttrs)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<!DOCTYPE html>\n");
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<meta charset=\"" + htmlCharset + "\">\n");
		sb.append("<title>" + htmlTitle + "</title>\n");
		sb.append("<style type=\"text/css\">\n");
		sb.append("." + getDashboardStyleName() + "{\n");
		sb.append("  position: absolute;\n");
		sb.append("  left: 0px;\n");
		sb.append("  right: 0px;\n");
		sb.append("  top: 0px;\n");
		sb.append("  bottom: 0px;\n");
		sb.append("}\n");
		sb.append("." + getChartStyleName() + "{\n");
		sb.append("  display: inline-block;\n");
		sb.append("  width: 300px;\n");
		sb.append("  height: 300px;\n");

		if (!StringUtil.isEmpty(customChartCssAttrs))
			sb.append(customChartCssAttrs);

		sb.append("}\n");
		sb.append("</style>\n");
		sb.append("</head>\n");
		sb.append("<body class=\"" + getDashboardStyleName() + "\">\n");
		sb.append("\n");

		for (String cwi : chartWidgetId)
			sb.append("  <div class=\"" + getChartStyleName() + "\" " + getAttrNameChartWidget() + "=\"" + cwi + "\" "
					+ (StringUtil.isEmpty(chartEleAttrs) ? "" : chartEleAttrs) + "></div>\n");

		sb.append("</body>\n");
		sb.append("</html>");

		return sb.toString();
	}

	@Override
	protected void renderHtmlTplDashboard(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			HtmlTplDashboard dashboard) throws Throwable
	{
		Reader in = getResourceReaderNonNull(dashboard.getWidget(), dashboard.getTemplate());

		try
		{
			renderHtmlTplDashboard(renderContext, renderAttr, dashboard, in);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	protected DashboardInfo renderHtmlTplDashboard(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			HtmlTplDashboard dashboard, Reader in) throws Exception
	{
		Writer out = renderAttr.getHtmlWriterNonNull(renderContext);

		HtmlTitleHandler htmlTitleHandler = renderAttr.getHtmlTitleHandler(renderContext);

		boolean resolvedDashboardInfo = false;
		boolean wroteDashboardImport = false;
		boolean wroteDashboardScript = false;
		boolean inHeadTag = false;
		boolean handledTitle = false;
		boolean inBodyTag = false;

		DashboardInfo dashboardInfo = new DashboardInfo();

		StringBuilder nameCache = createStringBuilder();
		StringBuilder valueCache = createStringBuilder();
		StringBuilder tagContentCache = createStringBuilder();

		int c = -1;
		while ((c = in.read()) > -1)
		{
			if (c == '<')
			{
				clear(nameCache);

				int last = readHtmlTagName(in, nameCache);
				String tagName = nameCache.toString();

				// <html
				if (this.dashboardSetTagName.equalsIgnoreCase(tagName))
				{
					appendResolvedTagInfo(out, c, tagName, last);

					if (last == '>' || resolvedDashboardInfo)
						;
					else
					{
						clear(nameCache);
						clear(valueCache);
						clear(tagContentCache);

						last = resolveDashboardInfo(in, last, tagContentCache, nameCache, valueCache, dashboardInfo);

						resolvedDashboardInfo = true;

						append(out, tagContentCache);
					}
				}
				// <head
				else if ("head".equalsIgnoreCase(tagName))
				{
					inHeadTag = true;

					appendResolvedTagInfo(out, c, tagName, last);
					if (last != '>')
						readToTagEnd(in, out);

					if (!wroteDashboardImport)
						writeDashboardImport(renderContext, renderAttr, out, dashboard, dashboardInfo);
				}
				// <title
				else if (inHeadTag && "title".equalsIgnoreCase(tagName))
				{
					appendResolvedTagInfo(out, c, tagName, last);

					if (last != '>')
						readToTagEnd(in, out);

					clear(nameCache);
					last = readToTagStart(in, nameCache);

					if (htmlTitleHandler != null)
					{
						String titleContent = htmlTitleHandler.handle(nameCache.toString());

						out.write(titleContent);
						appendIfValid(out, last);
					}
					else
					{
						append(out, nameCache);
						appendIfValid(out, last);
					}

					handledTitle = true;
				}
				// </head
				else if ("/head".equalsIgnoreCase(tagName))
				{
					if (!handledTitle)
					{
						if (htmlTitleHandler != null)
						{
							String titleContent = htmlTitleHandler.handle("");

							out.write("<title>");
							out.write(titleContent);
							out.write("</title>");
						}

						handledTitle = true;
					}

					appendResolvedTagInfo(out, c, tagName, last);
					inHeadTag = false;
				}
				// <body
				else if ("body".equalsIgnoreCase(tagName))
				{
					inBodyTag = true;

					appendResolvedTagInfo(out, c, tagName, last);
				}
				// <div
				else if (inBodyTag && this.chartTagName.equalsIgnoreCase(tagName))
				{
					appendResolvedTagInfo(out, c, tagName, last);

					if (last == '>')
						;
					else
					{
						clear(nameCache);
						clear(valueCache);
						clear(tagContentCache);

						last = resolveDashboardChartInfo(renderContext, renderAttr, dashboard, in, last,
								tagContentCache, nameCache, valueCache, dashboardInfo);

						append(out, tagContentCache);
					}
				}
				// </body
				else if ("/body".equalsIgnoreCase(tagName))
				{
					inBodyTag = false;

					if (!wroteDashboardScript)
					{
						writeHtmlTplDashboardScript(renderContext, renderAttr, out, dashboard, dashboardInfo);
						wroteDashboardScript = true;
					}

					appendResolvedTagInfo(out, c, tagName, last);
				}
				// <!--
				else if (tagName.startsWith("!--"))
				{
					appendResolvedTagInfo(out, c, tagName, last);

					// 空注释
					if (isReadHtmlTagEmptyComment(tagName, last))
						;
					else
						skipHtmlComment(in, out);
				}
				else
				{
					appendResolvedTagInfo(out, c, tagName, last);
				}
			}
			else
				out.write(c);
		}

		return dashboardInfo;
	}

	protected void writeDashboardImport(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr, Writer out,
			HtmlTplDashboard dashboard, DashboardInfo dashboardInfo) throws IOException
	{
		writeDashboardImport(renderContext, renderAttr, out, dashboard, dashboardInfo.getImportExclude());
	}

	protected void writeHtmlTplDashboardScript(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			Writer out, HtmlTplDashboard dashboard, DashboardInfo dashboardInfo) throws IOException
	{
		String globalDashboardVar = dashboardInfo.getDashboardVar();
		if (StringUtil.isEmpty(globalDashboardVar))
			globalDashboardVar = getDefaultDashboardVar();

		String tmp0RenderContextVarName = renderAttr.genRenderContextVarName("Tmp0");
		String tmp1RenderContextVarName = renderAttr.genRenderContextVarName("Tmp1");
		String localDashboardVarName = renderAttr.genDashboardVarName("Tmp");

		dashboard.setVarName(localDashboardVarName);

		writeScriptStartTag(out);
		writeNewLine(out);

		out.write("(function(){");
		writeNewLine(out);

		writeHtmlTplDashboardJSVar(renderContext, renderAttr, out, dashboard, tmp0RenderContextVarName);

		writeHtmlChartScripts(renderContext, renderAttr, out, dashboard, dashboardInfo);
		writeHtmlTplDashboardJSInit(renderContext, renderAttr, out, dashboard, tmp1RenderContextVarName);
		writeHtmlTplDashboardJSFactoryInit(renderContext, renderAttr, out, dashboard,
				dashboardInfo.getDashboardFactoryVar());

		out.write("window." + globalDashboardVar + "=" + localDashboardVarName + ";");
		writeNewLine(out);

		writeHtmlTplDashboardJSRender(renderContext, renderAttr, out, dashboard);

		out.write("})();");
		writeNewLine(out);

		writeScriptEndTag(out);
		writeNewLine(out);
	}

	protected void writeHtmlChartScripts(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr, Writer out,
			HtmlTplDashboard dashboard, DashboardInfo dashboardInfo) throws IOException
	{
		List<Chart> charts = dashboard.getCharts();
		if (charts == null)
		{
			charts = new ArrayList<>();
			dashboard.setCharts(charts);
		}

		List<ChartInfo> chartInfos = dashboardInfo.getChartInfos();
		if (chartInfos != null)
		{
			List<HtmlChartWidget> chartWidgets = getHtmlChartWidgets(chartInfos);
			List<String> chartPluginVarNames = writeHtmlChartPluginScriptsResolveImport(renderContext, renderAttr, out,
					chartWidgets);

			RenderContext chartRenderContext = new DefaultRenderContext(renderContext);
			HtmlChartRenderAttr chartRenderAttr = new HtmlChartRenderAttr();

			HtmlChartRenderOption chartRenderOption = new HtmlChartRenderOption(chartRenderAttr);
			chartRenderOption.setNotWriteChartElement(true);
			chartRenderOption.setNotWriteScriptTag(true);
			chartRenderOption.setNotWriteInvoke(true);
			chartRenderOption.setNotWritePluginObject(true);
			chartRenderOption.setNotWriteRenderContextObject(true);
			chartRenderOption.setRenderContextVarName(dashboard.getVarName() + "." + Dashboard.PROPERTY_RENDER_CONTEXT);

			chartRenderAttr.inflate(chartRenderContext, out, chartRenderOption);

			for (int i = 0; i < chartInfos.size(); i++)
			{
				ChartInfo chartInfo = chartInfos.get(i);
				HtmlChartWidget chartWidget = chartWidgets.get(i);

				chartRenderOption.setChartElementId(chartInfo.getElementId());
				chartRenderOption.setPluginVarName(chartPluginVarNames.get(i));
				chartRenderOption.setChartVarName(chartRenderAttr.genChartVarName(Integer.toString(i)));

				HtmlChart chart = writeHtmlChart(chartRenderContext, chartWidget);
				charts.add(chart);
			}
		}
	}

	protected List<HtmlChartWidget> getHtmlChartWidgets(List<ChartInfo> chartInfos)
	{
		List<HtmlChartWidget> list = new ArrayList<>();

		if (chartInfos == null)
			return list;

		for (ChartInfo chartInfo : chartInfos)
			list.add(getHtmlChartWidgetForRender(chartInfo.getWidgetId()));

		return list;
	}

	/**
	 * 解析{@linkplain DashboardInfo}。
	 * 
	 * @param in
	 * @param last
	 * @param cache
	 * @param attrName
	 * @param attrValue
	 * @param dashboardInfo
	 * @return
	 * @throws IOException
	 */
	protected int resolveDashboardInfo(Reader in, int last, StringBuilder cache, StringBuilder attrName,
			StringBuilder attrValue, DashboardInfo dashboardInfo) throws IOException
	{
		int c = -1;
		for (;;)
		{
			c = resolveHtmlTagAttr(in, last, cache, attrName, attrValue);

			String attrNameStr = toString(attrName);
			String attrValueStr = toString(attrValue);

			if (this.attrNameDashboardVar.equalsIgnoreCase(attrNameStr))
			{
				dashboardInfo.setDashboardVar(attrValueStr);
			}
			else if (this.attrNameDashboardFactory.equalsIgnoreCase(attrNameStr))
			{
				dashboardInfo.setDashboardFactoryVar(attrValueStr);
			}
			else if (this.attrNameDashboardImportExclude.equalsIgnoreCase(attrNameStr))
			{
				dashboardInfo.setImportExclude(attrValueStr);
			}

			clear(attrName);
			clear(attrValue);

			if (isHtmlTagEnd(c))
			{
				break;
			}
		}

		return c;
	}

	protected int resolveDashboardChartInfo(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			HtmlTplDashboard dashboard, Reader in, int last, StringBuilder cache, StringBuilder attrName,
			StringBuilder attrValue, DashboardInfo dashboardInfo) throws IOException
	{
		ChartInfo chartInfo = null;

		for (;;)
		{
			last = resolveHtmlTagAttr(in, last, cache, attrName, attrValue);

			String attrNameStr = attrName.toString();

			if (this.attrNameChartWidget.equalsIgnoreCase(attrNameStr))
			{
				if (chartInfo == null)
					chartInfo = new ChartInfo();

				chartInfo.setWidgetId(attrValue.toString().trim());
			}
			else if ("id".equalsIgnoreCase(attrNameStr))
			{
				if (chartInfo == null)
					chartInfo = new ChartInfo();

				chartInfo.setElementId(attrValue.toString().trim());
			}

			clear(attrName);
			clear(attrValue);

			if (isHtmlTagEnd(last))
				break;
		}

		if (chartInfo != null && StringUtil.isEmpty(chartInfo.getWidgetId()))
			chartInfo = null;

		// 元素没有定义“id”属性
		if (chartInfo != null && StringUtil.isEmpty(chartInfo.getElementId()))
		{
			int chartIndex = dashboardInfo.getChartInfos().size();
			String elementId = renderAttr.genChartElementId(Integer.toString(chartIndex));

			chartInfo.setElementId(elementId);

			int insertIdx = findInsertAttrIndex(cache);
			cache.insert(insertIdx, " id=\"" + elementId + "\" ");
		}

		if (chartInfo != null)
			dashboardInfo.addChartInfo(chartInfo);

		return last;
	}

	/**
	 * 查找可以插入属性的位置。
	 * 
	 * @param sb
	 * @return
	 */
	protected int findInsertAttrIndex(StringBuilder sb)
	{
		int i = sb.length() - 1;
		for (; i >= 0; i--)
		{
			int c = sb.charAt(i);

			if (isWhitespace(c))
				continue;
			else if (c != '>' && c != '/')
			{
				i = i + 1;
				break;
			}
		}

		return i;
	}

	/**
	 * 读取到“{@code <}”。
	 * 
	 * @param in
	 * @param out
	 * @return &lt;、-1，这个字符不会写入{@code out}
	 * @throws IOException
	 */
	protected int readToTagStart(Reader in, StringBuilder out) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			if (c == '<')
				break;

			appendChar(out, c);
		}

		return c;
	}

	/**
	 * 读取到“{@code >}”。
	 * 
	 * @param in
	 * @param out
	 * @return >、-1
	 * @throws IOException
	 */
	protected int readToTagEnd(Reader in, Writer out) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (c == '>')
				break;
		}

		return c;
	}

	/**
	 * 读取到“{@code >}”。
	 * 
	 * @param in
	 * @param out
	 * @return &gt;、-1
	 * @throws IOException
	 */
	protected int readToTagEnd(Reader in, StringBuilder out) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			appendChar(out, c);

			if (c == '>')
				break;
		}

		return c;
	}

	/**
	 * 写入HTML标签属性。
	 * 
	 * @param out
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	protected void appendHtmlAttr(Writer out, StringBuilder name, StringBuilder value) throws IOException
	{
		appendHtmlAttr(out, name.toString(), value.toString());
	}

	/**
	 * 写入HTML标签属性。
	 * 
	 * @param out
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	protected void appendHtmlAttr(Writer out, String name, String value) throws IOException
	{
		if (name.isEmpty() && value.isEmpty())
			return;

		out.write(' ');
		out.write(name);

		if (!StringUtil.isEmpty(value))
		{
			out.write('=');
			out.write(value);
		}
	}

	protected void appendResolvedTagInfo(Writer out, int tagChar, String tagName, int last) throws IOException
	{
		out.write(tagChar);
		out.write(tagName);

		if (isValidReadChar(last))
			out.write(last);
	}

	protected static class DashboardInfo
	{
		/** 看板变量名称 */
		private String dashboardVar;
		/** 看板工厂名称 */
		private String dashboardFactoryVar;
		/** 内置导入排除项 */
		private String importExclude;
		/** 图表信息 */
		private List<ChartInfo> chartInfos = new ArrayList<>();

		public DashboardInfo()
		{
			super();
		}

		public DashboardInfo(String dashboardVar)
		{
			super();
			this.dashboardVar = dashboardVar;
		}

		public String getDashboardVar()
		{
			return dashboardVar;
		}

		public void setDashboardVar(String dashboardVar)
		{
			this.dashboardVar = dashboardVar;
		}

		public String getDashboardFactoryVar()
		{
			return dashboardFactoryVar;
		}

		public void setDashboardFactoryVar(String dashboardFactoryVar)
		{
			this.dashboardFactoryVar = dashboardFactoryVar;
		}

		public String getImportExclude()
		{
			return importExclude;
		}

		public void setImportExclude(String importExclude)
		{
			this.importExclude = importExclude;
		}

		public List<ChartInfo> getChartInfos()
		{
			return chartInfos;
		}

		public void addChartInfo(ChartInfo chartInfo)
		{
			this.chartInfos.add(chartInfo);
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [dashboardVar=" + dashboardVar + ", rendererVar="
					+ dashboardFactoryVar + ", importExclude=" + importExclude + ", chartInfos=" + chartInfos + "]";
		}
	}

	protected static class ChartInfo
	{
		/** 图表部件ID */
		private String widgetId;
		/** 图标元素ID */
		private String elementId;

		public ChartInfo()
		{
			super();
		}

		public ChartInfo(String widgetId, String elementId)
		{
			super();
			this.widgetId = widgetId;
			this.elementId = elementId;
		}

		public String getWidgetId()
		{
			return widgetId;
		}

		public void setWidgetId(String widgetId)
		{
			this.widgetId = widgetId;
		}

		public String getElementId()
		{
			return elementId;
		}

		public void setElementId(String elementId)
		{
			this.elementId = elementId;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [widgetId=" + widgetId + ", elementId=" + elementId + "]";
		}
	}
}
