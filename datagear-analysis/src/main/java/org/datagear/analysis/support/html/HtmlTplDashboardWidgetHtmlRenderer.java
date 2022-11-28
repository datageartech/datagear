/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datagear.analysis.Chart;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.html.HtmlChartRenderAttr.HtmlChartRenderOption;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.HtmlTitleHandler;
import org.datagear.util.StringUtil;
import org.datagear.util.html.CopyWriter;
import org.datagear.util.html.HeadBodyAwareFilterHandler;

/**
 * 使用原生HTML网页作为模板的{@linkplain HtmlTplDashboardWidget}渲染器。
 * <p>
 * 支持的模板格式如下：
 * </p>
 * <code>
 * <pre>
 * ...
 * &lt;html dg-dashboard-factory="..." dg-dashboard-var="..."
 * 	dg-dashboard-unimport="..." dg-loadable-chart-widgets="..."
 * 	dg-dashboard-auto-render="..."&gt;
 * ...
 * &lt;head&gt;
 * ...
 * &lt;/head&gt;
 * ...
 * &lt;body&gt;
 * ...
 * &lt;div id="..." dg-chart-widget="..."&gt;&lt;/div&gt;
 * ...
 * &lt;script dg-dashboard-code="..."&gt;&lt;/script&gt;
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
 * <code>html dg-dashboard-unimport</code>：选填，定义看板网页不加载的内置库（{@linkplain HtmlTplDashboardRenderAttr#getImportList(RenderContext)}），多个以“,”隔开
 * </p>
 * <p>
 * <code>html dg-loadable-charts</code>：选填，定义看板网页允许在页面端通过JS异步加载的{@linkplain ChartWidget}模式（{@linkplain LoadableChartWidgets}），多个以“,”隔开
 * </p>
 * <p>
 * <code>html dg-dashboard-auto-render</code>：选填，定义看板网页是否自动执行渲染函数，可选值：{@code "true"} 是；{@code "false"} 否。默认值为：{@code "true"}
 * </p>
 * <p>
 * <code>div id</code>：选填，定义图表元素ID，如果不填，则会自动生成一个
 * </p>
 * <p>
 * <code>div dg-chart-widget</code>：必填，定义图表部件ID（{@linkplain HtmlChartWidget#getId()}）
 * </p>
 * <p>
 * <code>&lt;script dg-dashboard-code="..."&gt;&lt;/script&gt;</code>：选填，自定义看板脚本写入位置，必须在<code>&lt;body&gt;&lt;/body&gt;</code>标签内，可选值：
 * <br>
 * {@code "init"} ：仅写入看板初始脚本，不写入调用渲染执行脚本；
 * <br>
 * {@code "render"} ：写入看板初始脚本、调用渲染执行脚本；
 * <br>
 * {@code 其他} ：由<code>dg-dashboard-auto-render</code>决定。
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

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_VAR = DASHBOARD_ELEMENT_ATTR_PREFIX + "dashboard-var";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_FACTORY = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "dashboard-factory";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_IMPORT_EXCLUDE = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "dashboard-unimport";

	public static final String DEFAULT_ATTR_NAME_LOADABLE_CHART_WIDGETS = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "loadable-chart-widgets";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_AUTO_RENDER = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "dashboard-auto-render";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_CODE = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "dashboard-code";

	public static final String DEFAULT_ATTR_NAME_CHART_WIDGET = DASHBOARD_ELEMENT_ATTR_PREFIX + "chart-widget";

	public static final String ATTR_NAME_CHART_AUTO_RESIZE = DASHBOARD_ELEMENT_ATTR_PREFIX + "chart-auto-resize";
	
	public static final String DASHBOARD_CODE_ATTR_VALUE_INIT = "init";

	public static final String DASHBOARD_CODE_ATTR_VALUE_RENDER = "render";
	
	/** 看板设置标签名 */
	private String dashboardSetTagName = DEFAULT_DASHBOARD_SET_TAG_NAME;

	/** 属性名：看板JS变量名 */
	private String attrNameDashboardVar = DEFAULT_ATTR_NAME_DASHBOARD_VAR;

	/** 属性名：看板工厂JS变量名 */
	private String attrNameDashboardFactory = DEFAULT_ATTR_NAME_DASHBOARD_FACTORY;

	/** 属性名：不导入内置库的 */
	private String attrNameDashboardImportExclude = DEFAULT_ATTR_NAME_DASHBOARD_IMPORT_EXCLUDE;

	/** 属性名：异步加载图表部件模式 */
	private String attrNameLoadableChartWidgets = DEFAULT_ATTR_NAME_LOADABLE_CHART_WIDGETS;

	/** 属性名：是否自动执行看板渲染函数 */
	private String attrNameDashboardAutoRender = DEFAULT_ATTR_NAME_DASHBOARD_AUTO_RENDER;

	/** 属性名：写入看板脚本标识属性 */
	private String attrNameDashboardCode = DEFAULT_ATTR_NAME_DASHBOARD_CODE;

	/** 图表标签名 */
	private String chartTagName = DEFAULT_CHART_TAG_NAME;

	/** 属性名：图表部件ID */
	private String attrNameChartWidget = DEFAULT_ATTR_NAME_CHART_WIDGET;

	public HtmlTplDashboardWidgetHtmlRenderer()
	{
		super();
	}

	public HtmlTplDashboardWidgetHtmlRenderer(ChartWidgetSource chartWidgetSource)
	{
		super(chartWidgetSource);
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

	public String getAttrNameDashboardFactory()
	{
		return attrNameDashboardFactory;
	}

	public void setAttrNameDashboardFactory(String attrNameDashboardFactory)
	{
		this.attrNameDashboardFactory = attrNameDashboardFactory;
	}

	public String getAttrNameDashboardImportExclude()
	{
		return attrNameDashboardImportExclude;
	}

	public void setAttrNameDashboardImportExclude(String attrNameDashboardImportExclude)
	{
		this.attrNameDashboardImportExclude = attrNameDashboardImportExclude;
	}

	public String getAttrNameLoadableChartWidgets()
	{
		return attrNameLoadableChartWidgets;
	}

	public void setAttrNameLoadableChartWidgets(String attrNameLoadableChartWidgets)
	{
		this.attrNameLoadableChartWidgets = attrNameLoadableChartWidgets;
	}

	public String getAttrNameDashboardAutoRender()
	{
		return attrNameDashboardAutoRender;
	}

	public void setAttrNameDashboardAutoRender(String attrNameDashboardAutoRender)
	{
		this.attrNameDashboardAutoRender = attrNameDashboardAutoRender;
	}

	public String getAttrNameDashboardCode()
	{
		return attrNameDashboardCode;
	}

	public void setAttrNameDashboardCode(String attrNameDashboardCode)
	{
		this.attrNameDashboardCode = attrNameDashboardCode;
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
		return simpleTemplateContent(chartWidgetId, "", htmlCharset, "", "", "", "", "");
	}

	/**
	 * 获取简单模板内容。
	 * 
	 * @param chartWidgetIds
	 * @param htmlAttr
	 *            {@code html}元素属性，允许为{@code null}
	 * @param htmlCharset
	 * @param htmlTitle
	 *            HTML标题名，允许为{@code null}
	 * @param bodyStyleName
	 *            {@code body}元素的样式类名，允许为{@code null}
	 * @param bodyAttr
	 *            {@code body}元素属性，允许为{@code null}
	 * @param chartEleStyleName
	 *            图表元素样式类名，允许为{@code null}
	 * @param chartEleAttr
	 *            图表元素属性，允许为{@code null}
	 * @return
	 */
	public String simpleTemplateContent(String[] chartWidgetIds, String htmlAttr, String htmlCharset, String htmlTitle,
			String bodyStyleName, String bodyAttr, String chartEleStyleName, String chartEleAttr)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<!DOCTYPE html>\n");
		sb.append("<html" + (StringUtil.isEmpty(htmlAttr) ? "" : " " + htmlAttr) + ">\n");
		sb.append("<head>\n");
		sb.append("<meta charset=\"" + htmlCharset + "\">\n");
		sb.append("<title>" + (StringUtil.isEmpty(htmlTitle) ? "" : htmlTitle) + "</title>\n");
		sb.append("</head>\n");
		sb.append("<body" + (StringUtil.isEmpty(bodyStyleName) ? "" : " class=\""+bodyStyleName+"\"")
						+ (StringUtil.isEmpty(bodyAttr) ? "" : " "+bodyAttr)
						+ " " + ATTR_NAME_CHART_AUTO_RESIZE + "=\"true\">\n");
		sb.append("\n");

		for (String cwi : chartWidgetIds)
			sb.append("  <div class=\"" + (StringUtil.isEmpty(chartEleStyleName) ? "" : chartEleStyleName)
					+ "\" " + getAttrNameChartWidget() + "=\"" + cwi + "\" "
					+ (StringUtil.isEmpty(chartEleAttr) ? "" : chartEleAttr) + "></div>\n");

		sb.append("</body>\n");
		sb.append("</html>");

		return sb.toString();
	}

	@Override
	protected HtmlTplDashboard renderDashboard(RenderContext renderContext, HtmlTplDashboardWidget dashboardWidget,
			String template, Reader templateIn, HtmlTplDashboardRenderAttr renderAttr) throws Throwable
	{
		HtmlTplDashboard dashboard = createDashboard(renderContext, dashboardWidget, template);

		renderDashboard(renderContext, dashboard, template, templateIn, renderAttr);

		return dashboard;
	}

	protected DashboardInfo renderDashboard(RenderContext renderContext, HtmlTplDashboard dashboard,
			String template, Reader templateIn, HtmlTplDashboardRenderAttr renderAttr) throws Throwable
	{
		DashboardInfo dashboardInfo = new DashboardInfo();

		DashboardFilterHandler filterHandler = new DashboardFilterHandler(
				renderAttr.getHtmlWriterNonNull(renderContext), renderContext, renderAttr, dashboard, dashboardInfo);
		getHtmlFilter().filter(templateIn, filterHandler);
		
		return dashboardInfo;
	}

	/**
	 * 
	 * @param out
	 * @param renderContext
	 * @param renderAttr
	 * @param dashboard
	 * @param dashboardInfo
	 * @param writeScriptTag
	 * @param writeRenderCode 为{@code null}时由{@code dashboardInfo}的{@linkplain DashboardInfo#isDashboardAutoRender()}决定
	 * @throws IOException
	 */
	protected void writeDashboardScript(Writer out, RenderContext renderContext,
			HtmlTplDashboardRenderAttr renderAttr, HtmlTplDashboard dashboard, DashboardInfo dashboardInfo,
			boolean writeScriptTag, Boolean writeRenderCode) throws IOException
	{
		String globalDashboardVar = dashboardInfo.getDashboardVar();
		if (StringUtil.isEmpty(globalDashboardVar))
			globalDashboardVar = getDefaultDashboardVar();
		
		if(writeRenderCode == null)
			writeRenderCode = dashboardInfo.isDashboardAutoRender();

		String tmp0RenderContextVarName = renderAttr.genRenderContextVarName("Tmp0");
		String tmp1RenderContextVarName = renderAttr.genRenderContextVarName("Tmp1");
		String localDashboardVarName = renderAttr.genDashboardVarName("Tmp");

		dashboard.setVarName(localDashboardVarName);

		if(writeScriptTag)
			writeScriptStartTag(out);
		
		writeNewLine(out);
		out.write("(function(){");
		writeNewLine(out);

		writeDashboardJsVar(renderContext, renderAttr, out, dashboard, tmp0RenderContextVarName);

		writeChartScripts(renderContext, renderAttr, out, dashboard, dashboardInfo);
		writeDashboardJsInit(renderContext, renderAttr, out, dashboard, tmp1RenderContextVarName);
		writeDashboardJsFactoryInit(renderContext, renderAttr, out, dashboard,
				dashboardInfo.getDashboardFactoryVar());
		
		out.write("window." + globalDashboardVar + "=" + localDashboardVarName + ";");
		writeNewLine(out);
		
		if(writeRenderCode)
			writeDashboardJsRender(renderContext, renderAttr, out, dashboard);
		
		out.write("})();");
		writeNewLine(out);
		
		if(writeScriptTag)
		{
			writeScriptEndTag(out);
			writeNewLine(out);
		}
	}

	protected void writeChartScripts(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr, Writer out,
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
			List<HtmlChartWidget> chartWidgets = getChartWidgets(chartInfos);
			List<String> chartPluginVarNames = writeChartPluginScriptsResolveImport(renderContext, renderAttr, out,
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

				HtmlChart chart = writeChart(chartRenderContext, chartWidget);
				charts.add(chart);
			}
		}
	}

	protected List<HtmlChartWidget> getChartWidgets(List<ChartInfo> chartInfos)
	{
		List<HtmlChartWidget> list = new ArrayList<>();

		if (chartInfos == null)
			return list;

		for (ChartInfo chartInfo : chartInfos)
			list.add(getHtmlChartWidgetForRender(chartInfo.getWidgetId()));

		return list;
	}
	
	protected class DashboardFilterHandler extends HeadBodyAwareFilterHandler
	{
		private final RenderContext renderContext;
		private final HtmlTplDashboardRenderAttr renderAttr;
		private final HtmlTplDashboard dashboard;
		private final DashboardInfo dashboardInfo;

		private boolean htmlTagResolved = false;
		private boolean inTitleTag = false;
		private boolean titleTagHandled = false;
		private boolean dashboardImportWritten = false;
		private boolean dashboardScriptWritten = false;

		public DashboardFilterHandler(Writer out, RenderContext renderContext,
				HtmlTplDashboardRenderAttr renderAttr, HtmlTplDashboard dashboard, DashboardInfo dashboardInfo)
		{
			super(new CopyWriter(out, new StringWriter(), false));
			this.renderContext = renderContext;
			this.renderAttr = renderAttr;
			this.dashboard = dashboard;
			this.dashboardInfo = dashboardInfo;
		}

		public RenderContext getRenderContext()
		{
			return renderContext;
		}

		public HtmlTplDashboardRenderAttr getRenderAttr()
		{
			return renderAttr;
		}

		public HtmlTplDashboard getDashboard()
		{
			return dashboard;
		}

		public DashboardInfo getDashboardInfo()
		{
			return dashboardInfo;
		}

		@Override
		public void beforeWriteTagStart(Reader in, String tagName) throws IOException
		{
			// </body>前写看板脚本
			if (!this.dashboardScriptWritten && this.isInBodyTag() && equalsIgnoreCase(tagName, "/body"))
			{
				// 确保看板脚本前已写完看板导入库，比如没有定义<head></head>
				if (!this.dashboardImportWritten)
					writeDashboardImportWithSet();

				writeHtmlTplDashboardScriptWithSet();
			}

			// 处理标题
			if (!this.titleTagHandled && this.isInHeadTag())
			{
				// 优先</title>前插入标题后缀
				if (equalsIgnoreCase(tagName, "/title"))
				{
					HtmlTitleHandler htmlTitleHandler = this.renderAttr.getHtmlTitleHandler(this.renderContext);
					if (htmlTitleHandler != null)
					{
						String titleContent = ((StringWriter) this.getCopyWriter().getCopyOut()).toString();
						titleContent = htmlTitleHandler.suffix(titleContent);
						write(titleContent);
					}
					
					this.titleTagHandled = true;
				}
				// 其次</head>前插入<title></title>
				else if(equalsIgnoreCase(tagName, "/head"))
				{
					HtmlTitleHandler htmlTitleHandler = this.renderAttr.getHtmlTitleHandler(this.renderContext);
					if (htmlTitleHandler != null)
					{
						write("<title>");
						write(htmlTitleHandler.suffix(""));
						write("</title>");
					}

					this.titleTagHandled = true;
				}
			}

			super.beforeWriteTagStart(in, tagName);
			if (this.isInHeadTag() && this.inTitleTag && equalsIgnoreCase(tagName, "/title"))
			{
				this.inTitleTag = false;
				this.getCopyWriter().setCopy(this.inTitleTag);
			}
		}

		@Override
		public boolean isResolveTagAttrs(Reader in, String tagName)
		{
			return (this.isInBodyTag()
					&& (equalsIgnoreCase(tagName, HtmlTplDashboardWidgetHtmlRenderer.this.chartTagName)
							|| equalsIgnoreCase(tagName, TAG_NAME_SCRIPT)))
					|| (!this.htmlTagResolved && equalsIgnoreCase(tagName, "html"));
		}

		@Override
		public void beforeWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
				throws IOException
		{
			// 解析<body></body>内的图表元素
			if (this.isInBodyTag() && equalsIgnoreCase(tagName, HtmlTplDashboardWidgetHtmlRenderer.this.chartTagName))
			{
				resolveChartTagAttr(attrs);
			}
			// 解析<html>标签上的看板属性
			else if (!this.htmlTagResolved && equalsIgnoreCase(tagName, "html"))
			{
				resolveHtmlTagAttr(attrs);
				this.htmlTagResolved = true;
			}
		}

		@Override
		public void afterWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs) throws IOException
		{
			super.afterWriteTagEnd(in, tagName, tagEnd, attrs);
			
			if (this.isInHeadTag() && !this.inTitleTag && equalsIgnoreCase(tagName, "title"))
			{
				this.inTitleTag = !isSelfCloseTagEnd(tagEnd);
				this.getCopyWriter().setCopy(this.inTitleTag);
			}

			// 优先<head>后，其次<body>后（当没有定义<head>时）插入看板导入库
			if (!this.dashboardImportWritten && !isSelfCloseTagEnd(tagEnd)
					&& (equalsIgnoreCase(tagName, "head") || equalsIgnoreCase(tagName, "body")))
			{
				writeDashboardImportWithSet();
			}
			
			// <body>内的<script dg-dashboard-code></script>内写入看板脚本
			if(!this.dashboardScriptWritten && this.isInBodyTag() && equalsIgnoreCase(tagName, "script"))
			{
				// 此处不需要再次校验和writeDashboardImportWithSet()，因为上面已确保写入
				//if (!this.dashboardImportWritten)
				//	writeDashboardImportWithSet();
				
				if(attrs != null && attrs.containsKey(HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardCode))
					writeHtmlTplDashboardScriptCodeWithSet(attrs.get(HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardCode));
			}

			// 如果</body>前没写（没有定义</body>），则在</html>后写看板脚本
			if (!this.dashboardScriptWritten && !this.isInHeadTag() && !this.isInBodyTag()
					&& equalsIgnoreCase(tagName, "/html"))
			{
				// 确保看板脚本前已写完看板导入库，比如没有定义<head></head>、<body></body>
				if (!this.dashboardImportWritten)
					writeDashboardImportWithSet();

				writeHtmlTplDashboardScriptWithSet();
			}
		}

		@Override
		public void afterWrite(Reader in) throws IOException
		{
			if (!this.isAborted())
			{
				// 如果已全部完成而仍没有写看板脚本，则应在此写入，比如原始HTML里没有</body>标签
				if (!this.dashboardScriptWritten)
				{
					// 确保看板脚本前已写完看板导入库
					if (!this.dashboardImportWritten)
						writeDashboardImportWithSet();

					writeHtmlTplDashboardScriptWithSet();
				}
			}
		}

		@Override
		protected void onSetInHeadTag(boolean in)
		{
			if (!in)
				this.getCopyWriter().setCopy(false);
		}

		@Override
		protected void onSetInBodyTag(boolean in)
		{
			if (in)
				this.getCopyWriter().setCopy(false);
		}

		protected CopyWriter getCopyWriter()
		{
			return (CopyWriter)getOut();
		}

		protected void writeDashboardImportWithSet() throws IOException
		{
			writeDashboardImport(getOut(), this.renderContext, this.renderAttr, this.dashboard,
					this.dashboardInfo.getImportExclude());

			this.dashboardImportWritten = true;
		}

		protected void writeHtmlTplDashboardScriptWithSet() throws IOException
		{
			writeDashboardScript(getOut(), this.renderContext, this.renderAttr, this.dashboard,
					this.dashboardInfo, true, null);

			this.dashboardScriptWritten = true;
		}
		
		protected void writeHtmlTplDashboardScriptCodeWithSet(String codeMode) throws IOException
		{
			Boolean writeRenderCode = null;
			
			//只处理合法值，否则应由dg-dashboard-auto-render特性决定
			if(DASHBOARD_CODE_ATTR_VALUE_INIT.equalsIgnoreCase(codeMode))
				writeRenderCode = false;
			else if(DASHBOARD_CODE_ATTR_VALUE_RENDER.equalsIgnoreCase(codeMode))
				writeRenderCode = true;
			else
				writeRenderCode = null;
			
			writeDashboardScript(getOut(), this.renderContext, this.renderAttr, this.dashboard,
					this.dashboardInfo, false, writeRenderCode);

			this.dashboardScriptWritten = true;
		}

		protected void resolveHtmlTagAttr(Map<String, String> attrs)
		{
			for (Map.Entry<String, String> entry : attrs.entrySet())
			{
				String name = entry.getKey();

				if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardVar.equalsIgnoreCase(name))
				{
					this.dashboardInfo.setDashboardVar(trim(entry.getValue()));
				}
				else if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardFactory.equalsIgnoreCase(name))
				{
					this.dashboardInfo.setDashboardFactoryVar(trim(entry.getValue()));
				}
				else if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardImportExclude.equalsIgnoreCase(name))
				{
					this.dashboardInfo.setImportExclude(trim(entry.getValue()));
				}
				else if(HtmlTplDashboardWidgetHtmlRenderer.this.attrNameLoadableChartWidgets.equalsIgnoreCase(name))
				{
					this.dashboard.setLoadableChartWidgets(resolveLoadableChartWidgets(trim(entry.getValue())));
				}
				else if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardAutoRender.equalsIgnoreCase(name))
				{
					this.dashboardInfo.setDashboardAutoRender(trim(entry.getValue()));
				}
			}
		}
		
		protected LoadableChartWidgets resolveLoadableChartWidgets(String str)
		{
			if(StringUtil.isEmpty(str))
			{
				return null;
			}
			else if(LoadableChartWidgets.PATTERN_ALL.equalsIgnoreCase(str))
			{
				return LoadableChartWidgets.all();
			}
			else if(LoadableChartWidgets.PATTERN_NONE.equalsIgnoreCase(str))
			{
				return LoadableChartWidgets.none();
			}
			else if(LoadableChartWidgets.PATTERN_PERMITTED.equalsIgnoreCase(str))
			{
				return LoadableChartWidgets.permitted();
			}
			else
			{
				List<String> widgetIdList = StringUtil.splitWithTrim(str, ",");
				Set<String> widgetIdSet = new HashSet<String>(widgetIdList);
				
				return LoadableChartWidgets.list(widgetIdSet);
			}
		}
		
		protected void resolveChartTagAttr(Map<String, String> attrs) throws IOException
		{
			String chartWidget = null;
			String elementId = null;

			for (Map.Entry<String, String> entry : attrs.entrySet())
			{
				String name = entry.getKey();

				// 浏览器端解析时同名属性会取第一个，所以这里也应如此

				if (chartWidget == null
						&& HtmlTplDashboardWidgetHtmlRenderer.this.attrNameChartWidget.equalsIgnoreCase(name))
				{
					chartWidget = trim(entry.getValue());
				}
				else if (elementId == null && equalsIgnoreCase(name, "id"))
				{
					elementId = trim(entry.getValue());
				}
			}

			if (StringUtil.isEmpty(chartWidget))
				return;

			ChartInfo chartInfo = new ChartInfo(chartWidget, elementId);

			// 元素没有定义“id”属性，应自动插入
			if (StringUtil.isEmpty(chartInfo.getElementId()))
			{
				elementId = renderAttr.genChartElementId(Integer.toString(dashboardInfo.getChartInfos().size()));
				chartInfo.setElementId(elementId);

				write(" id=\"" + elementId + "\" ");
			}

			dashboardInfo.addChartInfo(chartInfo);
		}
	}

	protected static class DashboardInfo
	{
		/** 看板变量名称 */
		private String dashboardVar = null;
		
		/** 看板工厂名称 */
		private String dashboardFactoryVar = null;
		
		/** 内置导入排除项 */
		private String importExclude = null;

		/** 是否自动执行看板渲染函数 */
		private String dashboardAutoRender = null;
		
		/** 图表信息 */
		private List<ChartInfo> chartInfos = new ArrayList<>();

		public DashboardInfo()
		{
			super();
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
		
		public String getDashboardAutoRender()
		{
			return dashboardAutoRender;
		}

		public void setDashboardAutoRender(String dashboardAutoRender)
		{
			this.dashboardAutoRender = dashboardAutoRender;
		}
		
		public boolean isDashboardAutoRender()
		{
			return !Boolean.FALSE.toString().equalsIgnoreCase(this.dashboardAutoRender);
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
			return getClass().getSimpleName() + " [dashboardVar=" + dashboardVar + ", dashboardFactoryVar=" + dashboardFactoryVar
					+ ", importExclude=" + importExclude + ", dashboardAutoRender=" + dashboardAutoRender + ", chartInfos=" + chartInfos + "]";
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
