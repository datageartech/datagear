/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.DashboardWidgetResManager;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetFmkRenderer.HtmlDashboardRenderDataModel;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 抽象{@linkplain HtmlTplDashboardWidget}渲染器。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class HtmlTplDashboardWidgetRenderer<T extends HtmlRenderContext>
{
	public static final String DEFAULT_CONTEXT_PATH_PLACE_HOLDER = "$CONTEXTPATH";

	protected static final String KEY_HTML_DASHBOARD_RENDER_DATA_MODEL = HtmlDashboardRenderDataModel.class
			.getSimpleName();

	protected static final String RENDER_ATTR_NAME_FOR_NOT_FOUND_SCRIPT = StringUtil
			.firstLowerCase(Global.PRODUCT_NAME_EN) + "RenderValueForNotFound";

	/** 内置导入内容 */
	private List<HtmlDashboardImport> htmlDashboardImports;

	/** 上下文路径占位符 */
	private String contextPathPlaceholder = DEFAULT_CONTEXT_PATH_PLACE_HOLDER;

	private DashboardWidgetResManager dashboardWidgetResManager;

	private ChartWidgetSource chartWidgetSource;

	private DashboardThemeSource dashboardThemeSource = new SimpleDashboardThemeSource();

	private HtmlDashboardScriptObjectWriter htmlDashboardScriptObjectWriter = new HtmlDashboardScriptObjectWriter();

	private HtmlChartWidget<HtmlRenderContext> htmlChartWidgetForNotFound = new HtmlChartWidget<HtmlRenderContext>(
			StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "HtmlChartWidgetForNotFound",
			new ValueHtmlChartPlugin<HtmlRenderContext>(
					StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "HtmlChartPluginForNotFound",
					RENDER_ATTR_NAME_FOR_NOT_FOUND_SCRIPT));

	private String templateEncoding = "UTF-8";

	private String writerEncoding = "UTF-8";

	/** 换行符 */
	private String newLine = "\r\n";

	public HtmlTplDashboardWidgetRenderer()
	{
		super();
	}

	public HtmlTplDashboardWidgetRenderer(DashboardWidgetResManager dashboardWidgetResManager,
			ChartWidgetSource chartWidgetSource)
	{
		super();
		this.dashboardWidgetResManager = dashboardWidgetResManager;
		this.chartWidgetSource = chartWidgetSource;
	}

	public List<HtmlDashboardImport> getHtmlDashboardImports()
	{
		return htmlDashboardImports;
	}

	public void setHtmlDashboardImports(List<HtmlDashboardImport> htmlDashboardImports)
	{
		this.htmlDashboardImports = htmlDashboardImports;
	}

	public String getContextPathPlaceholder()
	{
		return contextPathPlaceholder;
	}

	public void setContextPathPlaceholder(String contextPathPlaceholder)
	{
		this.contextPathPlaceholder = contextPathPlaceholder;
	}

	public DashboardWidgetResManager getDashboardWidgetResManager()
	{
		return dashboardWidgetResManager;
	}

	public void setDashboardWidgetResManager(DashboardWidgetResManager dashboardWidgetResManager)
	{
		this.dashboardWidgetResManager = dashboardWidgetResManager;
	}

	public ChartWidgetSource getChartWidgetSource()
	{
		return chartWidgetSource;
	}

	public void setChartWidgetSource(ChartWidgetSource chartWidgetSource)
	{
		this.chartWidgetSource = chartWidgetSource;
	}

	public DashboardThemeSource getDashboardThemeSource()
	{
		return dashboardThemeSource;
	}

	public void setDashboardThemeSource(DashboardThemeSource dashboardThemeSource)
	{
		this.dashboardThemeSource = dashboardThemeSource;
	}

	public HtmlDashboardScriptObjectWriter getHtmlDashboardScriptObjectWriter()
	{
		return htmlDashboardScriptObjectWriter;
	}

	public void setHtmlDashboardScriptObjectWriter(HtmlDashboardScriptObjectWriter htmlDashboardScriptObjectWriter)
	{
		this.htmlDashboardScriptObjectWriter = htmlDashboardScriptObjectWriter;
	}

	public HtmlChartWidget<HtmlRenderContext> getHtmlChartWidgetForNotFound()
	{
		return htmlChartWidgetForNotFound;
	}

	public void setHtmlChartWidgetForNotFound(HtmlChartWidget<HtmlRenderContext> htmlChartWidgetForNotFound)
	{
		this.htmlChartWidgetForNotFound = htmlChartWidgetForNotFound;
	}

	public String getTemplateEncoding()
	{
		return templateEncoding;
	}

	public void setTemplateEncoding(String templateEncoding)
	{
		this.templateEncoding = templateEncoding;
	}

	public String getWriterEncoding()
	{
		return writerEncoding;
	}

	public void setWriterEncoding(String writerEncoding)
	{
		this.writerEncoding = writerEncoding;
	}

	public String getNewLine()
	{
		return newLine;
	}

	public void setNewLine(String newLine)
	{
		this.newLine = newLine;
	}

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @param dashboardWidget
	 * @return
	 * @throws RenderException
	 */
	public HtmlDashboard render(T renderContext, HtmlTplDashboardWidget<T> dashboardWidget) throws RenderException
	{
		inflateThemes(renderContext);

		HtmlDashboard dashboard = createHtmlDashboard(renderContext, dashboardWidget);

		try
		{
			renderHtmlDashboard(renderContext, dashboard);
		}
		catch (RenderException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new RenderException(t);
		}

		return dashboard;
	}

	/**
	 * 读取指定{@linkplain HtmlTplDashboardWidget}的模板内容。
	 * 
	 * @param dashboardWidget
	 * @return
	 * @throws IOException
	 */
	public String readTemplateContent(HtmlTplDashboardWidget<T> dashboardWidget) throws IOException
	{
		File templateFile = this.dashboardWidgetResManager.getFile(dashboardWidget.getId(),
				dashboardWidget.getTemplate());

		if (!templateFile.exists())
			return "";

		String templateContent = "";

		Reader reader = null;

		try
		{
			reader = IOUtil.getReader(templateFile, this.templateEncoding);
			templateContent = IOUtil.readString(reader, false);
		}
		finally
		{
			IOUtil.close(reader);
		}

		return templateContent;
	}

	/**
	 * 保存指定指定{@linkplain HtmlTplDashboardWidget}的模板内容。
	 * 
	 * @param dashboardWidget
	 * @param templateContent
	 * @throws IOException
	 */
	public void saveTemplateContent(HtmlTplDashboardWidget<T> dashboardWidget, String templateContent)
			throws IOException
	{
		File templateFile = this.dashboardWidgetResManager.getFile(dashboardWidget.getId(),
				dashboardWidget.getTemplate());

		Writer writer = null;

		try
		{
			writer = IOUtil.getWriter(templateFile, this.templateEncoding);
			writer.write(templateContent);
		}
		finally
		{
			IOUtil.close(writer);
		}
	}

	/**
	 * 创建{@linkplain HtmlDashboard}。
	 * 
	 * @param renderContext
	 * @param dashboardWidget
	 * @return
	 */
	protected HtmlDashboard createHtmlDashboard(T renderContext, HtmlTplDashboardWidget<T> dashboardWidget)
	{
		HtmlDashboard dashboard = new HtmlDashboard();

		dashboard.setId(IDUtil.uuid());
		dashboard.setWidget(dashboardWidget);
		dashboard.setRenderContext(renderContext);
		dashboard.setCharts(new ArrayList<Chart>());

		return dashboard;
	}

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @throws Exception
	 */
	protected abstract void renderHtmlDashboard(T renderContext, HtmlDashboard dashboard) throws Exception;

	/**
	 * 获取{@linkplain HtmlTplDashboardWidget#getId()}的指定模板对象。
	 * 
	 * @param dashboardWidget
	 * @return
	 * @throws Exception
	 */
	protected Reader getTemplateReader(HtmlTplDashboardWidget<?> dashboardWidget) throws Exception
	{
		File templateFile = this.dashboardWidgetResManager.getFile(dashboardWidget.getId(),
				dashboardWidget.getTemplate());

		if (!templateFile.exists())
			throw new RenderException("Dashboard template file not found");

		return IOUtil.getReader(templateFile, getTemplateEncoding());
	}

	protected void inflateThemes(HtmlRenderContext renderContext)
	{
		RenderStyle renderStyle = HtmlRenderAttributes.getRenderStyle(renderContext);

		if (renderStyle == null)
		{
			renderStyle = RenderStyle.LIGHT;
			HtmlRenderAttributes.setRenderStyle(renderContext, renderStyle);
		}

		DashboardTheme dashboardTheme = HtmlRenderAttributes.getDashboardTheme(renderContext);

		if (dashboardTheme == null)
		{
			dashboardTheme = this.dashboardThemeSource.getDashboardTheme(renderStyle);

			if (dashboardTheme == null)
				dashboardTheme = SimpleDashboardThemeSource.THEME_LIGHT;

			HtmlRenderAttributes.setDashboardTheme(renderContext, dashboardTheme);
		}

		ChartTheme chartTheme = HtmlRenderAttributes.getChartTheme(renderContext);

		if (chartTheme == null)
		{
			chartTheme = dashboardTheme.getChartTheme();
			HtmlRenderAttributes.setChartTheme(renderContext, chartTheme);
		}
	}

	/**
	 * 获取用于渲染指定ID图表的{@linkplain ChartWidget}。
	 * <p>
	 * 此方法不会返回{@code null}，如果找不到指定ID的{@linkplain ChartWidget}，它将返回。
	 * </p>
	 * 
	 * @param renderContext
	 * @param id
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected HtmlChartWidget<HtmlRenderContext> getHtmlChartWidgetForRender(HtmlRenderContext renderContext, String id)
	{
		ChartWidget chartWidget = (StringUtil.isEmpty(id) ? null : this.chartWidgetSource.getChartWidget(id));

		if (chartWidget == null)
		{
			chartWidget = this.htmlChartWidgetForNotFound;
			renderContext.setAttribute(RENDER_ATTR_NAME_FOR_NOT_FOUND_SCRIPT,
					"Chart '" + (id == null ? "" : id) + "' Not Found");
		}

		return (HtmlChartWidget<HtmlRenderContext>) chartWidget;
	}

	protected void writeHtmlDashboardScriptObject(Writer out, HtmlDashboard dashboard, boolean renderContextEmpty)
			throws IOException
	{
		getHtmlDashboardScriptObjectWriter().write(out, dashboard, renderContextEmpty);
	}

	protected void writeRenderContextScriptObject(Writer out, RenderContext renderContext) throws IOException
	{
		getHtmlDashboardScriptObjectWriter().writeRenderContext(out, renderContext, true);
	}

	/**
	 * 写看板导入项。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @param importExclude
	 * @throws IOException
	 */
	protected void writeDashboardImport(HtmlRenderContext renderContext, HtmlDashboard dashboard, String importExclude)
			throws IOException
	{
		if (this.htmlDashboardImports == null || this.htmlDashboardImports.isEmpty())
			return;

		Writer out = renderContext.getWriter();

		List<String> excludes = StringUtil.splitWithTrim(importExclude, ",");

		for (HtmlDashboardImport impt : this.htmlDashboardImports)
		{
			String name = impt.getName();

			if (excludes.contains(name))
				continue;

			String content = replaceContextPathPlaceholder(impt.getContent(), renderContext.getContextPath());

			writeNewLine(out);
			out.write(content);
		}
	}

	/**
	 * 替换字符串中的上下文占位符为真实的上下文。
	 * 
	 * @param str
	 * @param contextPath
	 * @return
	 */
	protected String replaceContextPathPlaceholder(String str, String contextPath)
	{
		if (StringUtil.isEmpty(str))
			return str;

		if (contextPath == null)
			contextPath = "";

		return str.replace(getContextPathPlaceholder(), contextPath);
	}

	/**
	 * 写脚本开始标签。
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void writeScriptStartTag(Writer out) throws IOException
	{
		out.write("<script type=\"text/javascript\">");
	}

	/**
	 * 写脚本结束标签。
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void writeScriptEndTag(Writer out) throws IOException
	{
		out.write("</script>");
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
}
