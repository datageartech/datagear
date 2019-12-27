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
import java.io.StringReader;
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
import org.datagear.analysis.Theme;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.DashboardWidgetResManager;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
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

	public static final String DEFAULT_THEME_IMPORT_NAME = "dg-theme";

	public static final String DEFAULT_DASHBOARD_STYLE_NAME = "dg-dashboard";

	public static final String DEFAULT_CHART_STYLE_NAME = "dg-chart";

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

	private String themeImportName = DEFAULT_THEME_IMPORT_NAME;

	/** 主题中的看板样式名 */
	private String dashboardStyleName = DEFAULT_DASHBOARD_STYLE_NAME;

	/** 主题中的图表样式名 */
	private String chartStyleName = DEFAULT_CHART_STYLE_NAME;

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

	public String getThemeImportName()
	{
		return themeImportName;
	}

	public void setThemeImportName(String themeImportName)
	{
		this.themeImportName = themeImportName;
	}

	public String getDashboardStyleName()
	{
		return dashboardStyleName;
	}

	public void setDashboardStyleName(String dashboardStyleName)
	{
		this.dashboardStyleName = dashboardStyleName;
	}

	public String getChartStyleName()
	{
		return chartStyleName;
	}

	public void setChartStyleName(String chartStyleName)
	{
		this.chartStyleName = chartStyleName;
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
		Reader reader = getTemplateReaderNotNull(dashboardWidget);

		return IOUtil.readString(reader, true);
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
		Writer writer = null;

		try
		{
			writer = getTemplateWriter(dashboardWidget);
			writer.write(templateContent);
		}
		finally
		{
			IOUtil.close(writer);
		}
	}

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @throws Throwable
	 */
	protected abstract void renderHtmlDashboard(T renderContext, HtmlDashboard dashboard) throws Throwable;

	/**
	 * 获取{@linkplain HtmlTplDashboardWidget}的模板输入流。
	 * <p>
	 * 如果文件不存在，它将返回一个空字符串的{@linkplain StringReader}。
	 * </p>
	 * 
	 * @param dashboardWidget
	 * @return
	 * @throws IOException
	 */
	protected Reader getTemplateReaderNotNull(HtmlTplDashboardWidget<?> dashboardWidget) throws IOException
	{
		File templateFile = getTemplateFile(dashboardWidget);

		if (!templateFile.exists())
			return new StringReader("");

		if (dashboardWidget.hasTemplateEncoding())
			return IOUtil.getReader(templateFile, dashboardWidget.getTemplateEncoding());
		else
			return IOUtil.getReader(templateFile);
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardWidget}的模板输入流。
	 * <p>
	 * 如果文件不存在，它将返回一个空字符串的{@linkplain StringReader}。
	 * </p>
	 * 
	 * @param dashboardWidget
	 * @return
	 * @throws IOException
	 */
	protected Writer getTemplateWriter(HtmlTplDashboardWidget<?> dashboardWidget) throws IOException
	{
		File templateFile = getTemplateFile(dashboardWidget);

		if (dashboardWidget.hasTemplateEncoding())
			return IOUtil.getWriter(templateFile, dashboardWidget.getTemplateEncoding());
		else
			return IOUtil.getWriter(templateFile);
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardWidget#getId()}的指定模板文件。
	 * 
	 * @param dashboardWidget
	 * @return
	 */
	protected File getTemplateFile(HtmlTplDashboardWidget<?> dashboardWidget)
	{
		File templateFile = this.dashboardWidgetResManager.getFile(dashboardWidget.getId(),
				dashboardWidget.getTemplate());

		return templateFile;
	}

	/**
	 * 设置主题。
	 * 
	 * @param renderContext
	 */
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
	 * 此方法不会返回{@code null}，如果找不到指定ID的{@linkplain ChartWidget}，它将返回{@linkplain #htmlChartWidgetForNotFound}。
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

	/**
	 * 写{@linkplain HtmlDashboard} JS变量：
	 * <p>
	 * <code>var dashboard = {...};</code>
	 * </p>
	 * 
	 * @param out
	 * @param dashboard
	 * @param renderContextEmpty
	 * @throws IOException
	 */
	protected void writeHtmlDashboardJSVar(Writer out, HtmlDashboard dashboard, boolean renderContextEmpty)
			throws IOException
	{
		out.write("var ");
		out.write(dashboard.getVarName());
		out.write("=");
		writeNewLine(out);
		writeHtmlDashboardJSObject(out, dashboard, renderContextEmpty);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 写{@linkplain HtmlDashboard} JS初始化代码：
	 * <p>
	 * <code>
	 * <pre>
	 * var renderContext = {...};
	 * dashboard.renderContext.attributes = renderContext.attributes;
	 * ...
	 * ...
	 * dashboard.render = function(){ ... };
	 * dashboard.update = function(){ ... };
	 * window.onload = function(){
	 * ...
	 * dashboard.render();
	 * ...
	 * dashboard.update();
	 * ...
	 * };
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param out
	 * @param dashboard
	 * @param renderContextVar
	 * @param listenerVar
	 *            允许为{@code null}
	 * @throws IOException
	 */
	protected void writeHtmlDashboardJSInit(Writer out, HtmlDashboard dashboard, String renderContextVar,
			String listenerVar) throws IOException
	{
		String varName = dashboard.getVarName();
		boolean hasListener = !StringUtil.isEmpty(listenerVar);
		HtmlRenderContext renderContext = dashboard.getRenderContext();

		out.write("var ");
		out.write(renderContextVar);
		out.write("=");
		writeNewLine(out);
		writeRenderContextJSObject(out, renderContext);
		out.write(";");
		writeNewLine(out);
		out.write(varName + ".renderContext.attributes = " + renderContextVar + ".attributes;");
		writeNewLine(out);

		List<? extends HtmlChart> charts = dashboard.getCharts();
		if (charts != null)
		{
			for (HtmlChart chart : charts)
			{
				out.write(varName + ".charts.push(" + chart.getVarName() + ");");
				writeNewLine(out);
			}
		}

		out.write(varName + "." + HtmlChartPlugin.SCRIPT_RENDER_FUNCTION_NAME + " = function(){");
		writeNewLine(out);
		out.write(" for(var i=0; i<this.charts.length; i++){ this.charts[i]."
				+ HtmlChartPlugin.SCRIPT_RENDER_FUNCTION_NAME + "(); }");
		writeNewLine(out);
		out.write("};");
		writeNewLine(out);

		out.write(varName + "." + HtmlChartPlugin.SCRIPT_UPDATE_FUNCTION_NAME + " = function(){");
		writeNewLine(out);
		out.write(" for(var i=0; i<this.charts.length; i++){ this.charts[i]."
				+ HtmlChartPlugin.SCRIPT_UPDATE_FUNCTION_NAME + "(); }");
		writeNewLine(out);
		out.write("};");
		writeNewLine(out);

		if (hasListener)
		{
			out.write(varName + ".listener = window[\"" + listenerVar + "\"];");
			writeNewLine(out);
		}

		out.write("window.onload = function(){");
		writeNewLine(out);

		out.write("var doRender = true;");
		writeNewLine(out);

		if (hasListener)
		{
			out.write("if(" + varName + ".listener && " + varName + ".listener.onRender)");
			writeNewLine(out);
			out.write("  doRender=" + varName + ".listener.onRender(" + varName + "); ");
			writeNewLine(out);
		}

		out.write("if(doRender != false)");
		writeNewLine(out);
		out.write("  " + varName + "." + HtmlChartPlugin.SCRIPT_RENDER_FUNCTION_NAME + "();");
		writeNewLine(out);

		out.write("var doUpdate = true;");
		writeNewLine(out);

		if (hasListener)
		{
			out.write("if(" + varName + ".listener && " + varName + ".listener.onUpdate)");
			writeNewLine(out);
			out.write("  doUpdate=" + varName + ".listener.onUpdate(" + varName + "); ");
			writeNewLine(out);
		}

		out.write("if(doUpdate != false)");
		writeNewLine(out);
		out.write("  " + varName + "." + HtmlChartPlugin.SCRIPT_UPDATE_FUNCTION_NAME + "();");
		writeNewLine(out);

		out.write("};");
		writeNewLine(out);
	}

	/**
	 * 写{@linkplain HtmlDashboard} JS对象：
	 * <p>
	 * <code>{...}</code>
	 * </p>
	 * 
	 * @param out
	 * @param dashboard
	 * @param renderContextEmpty
	 * @throws IOException
	 */
	protected void writeHtmlDashboardJSObject(Writer out, HtmlDashboard dashboard, boolean renderContextEmpty)
			throws IOException
	{
		getHtmlDashboardScriptObjectWriter().write(out, dashboard, renderContextEmpty);
	}

	/**
	 * 写{@linkplain RenderContext} JS对象：
	 * <p>
	 * <code>{...}</code>
	 * </p>
	 * 
	 * @param out
	 * @param renderContext
	 * @throws IOException
	 */
	protected void writeRenderContextJSObject(Writer out, RenderContext renderContext) throws IOException
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
		Writer out = renderContext.getWriter();

		List<String> excludes = StringUtil.splitWithTrim(importExclude, ",");

		if (this.htmlDashboardImports != null)
		{
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

		if (!excludes.contains(this.themeImportName))
		{
			writeNewLine(out);
			writeDashboardThemeStyle(renderContext, dashboard, out);
		}
	}

	/**
	 * 写看板主题样式。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @param out
	 * @return
	 * @throws IOException
	 */
	protected boolean writeDashboardThemeStyle(HtmlRenderContext renderContext, HtmlDashboard dashboard, Writer out)
			throws IOException
	{
		DashboardTheme dashboardTheme = HtmlRenderAttributes.getDashboardTheme(renderContext);

		if (dashboardTheme == null)
			return false;

		out.write("<style type=\"text/css\">");
		writeNewLine(out);
		out.write("." + this.dashboardStyleName + "{");
		writeNewLine(out);
		writeStyleAttrs(out, dashboardTheme);
		writeStyleAttrsFillParent(out);
		out.write("}");
		writeNewLine(out);

		out.write("." + this.chartStyleName + "{");
		writeNewLine(out);
		writeStyleAttrs(out, dashboardTheme.getChartTheme());
		out.write("  display: inline-block;");
		writeNewLine(out);
		out.write("  width: 30%;");
		writeNewLine(out);
		out.write("  height: 40%;");
		writeNewLine(out);
		out.write("  min-width: 8em;");
		writeNewLine(out);
		out.write("  min-height: 6em;");
		writeNewLine(out);
		out.write("  margin-left: 2.3%;");
		writeNewLine(out);
		out.write("  margin-top: 1.5em;");
		writeNewLine(out);
		out.write("}");
		writeNewLine(out);

		out.write("</style>");
		writeNewLine(out);

		return true;
	}

	/**
	 * 写主题的样式属性。
	 * 
	 * @param out
	 * @param theme
	 * @throws IOException
	 */
	protected void writeStyleAttrs(Writer out, Theme theme) throws IOException
	{
		if (theme != null)
		{
			String borderWidth = theme.getBorderWidth();
			if (borderWidth == null || borderWidth.equals("0") || borderWidth.equalsIgnoreCase("0px"))
				borderWidth = "";

			out.write("  color: " + theme.getForegroundColor() + ";");
			writeNewLine(out);
			out.write("  background-color: " + theme.getBackgroundColor() + ";");
			writeNewLine(out);

			if (!StringUtil.isEmpty(borderWidth))
			{
				out.write("  border: " + borderWidth + " solid " + theme.getBorderColor() + ";");
				writeNewLine(out);
			}
		}
	}

	/**
	 * 写填充父元素样式属性。
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void writeStyleAttrsFillParent(Writer out) throws IOException
	{
		out.write("  position: absolute;");
		writeNewLine(out);
		out.write("  top: 0px;");
		writeNewLine(out);
		out.write("  bottom: 0px;");
		writeNewLine(out);
		out.write("  left: 0px;");
		writeNewLine(out);
		out.write("  right: 0px;");
		writeNewLine(out);
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

	/**
	 * 创建{@linkplain HtmlDashboard}实例。
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
}
