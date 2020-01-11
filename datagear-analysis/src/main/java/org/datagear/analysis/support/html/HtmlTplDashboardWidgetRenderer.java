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
 * <p>
 * 此类的{@linkplain #writeHtmlDashboardJSRender(Writer, HtmlDashboard, String)}方法的JS看板渲染逻辑为：
 * </p>
 * <code>
 * <pre>
 * window.onload = function(){
 *   dashboardRenderer.render(dashboard);
 * };
 * </pre>
 * </code>
 * <p>
 * 因此，看板页面应该定义如下JS看板渲染器对象：
 * </p>
 * <code>
 * <pre>
 * var dashboardRenderer =
 * {
 *   render : function(dashboard)
 *   {
 *     ...
 *   }
 * };
 * </pre>
 * </code>
 * <p>
 * 子类在调用此方法时可以传入自定义JS看板渲染器对象的变量名，默认为{@linkplain #getDefaultDashboardRendererVar()}。
 * </p>
 * <p>
 * 此类的{@linkplain #getHtmlDashboardImports()}的{@linkplain HtmlDashboardImport#getContent()}可以包含{@linkplain #getContextPathPlaceholder()}占位符，
 * 在渲染时，占位符会被替换为实际的{@linkplain HtmlRenderContext#getContextPath()}。
 * </p>
 * <p>
 * 此类的{@linkplain #getExtDashboardInitScript()}可以包含{@linkplain #getDashboardVarPlaceholder()}占位符，
 * 在渲染时，占位符会被替换为实际的{@linkplain HtmlDashboard#getVarName()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class HtmlTplDashboardWidgetRenderer<T extends HtmlRenderContext>
{
	public static final String DEFAULT_CONTEXT_PATH_PLACE_HOLDER = "$CONTEXTPATH";

	public static final String DEFAULT_DASHBOARD_VAR_PLACE_HOLDER = "$DASHBOARD";

	public static final String DEFAULT_DASHBOARD_RENDERER_VAR = "dashboardRenderer";

	public static final String DEFAULT_THEME_IMPORT_NAME = "dg-theme";

	public static final String DEFAULT_DASHBOARD_STYLE_NAME = "dg-dashboard";

	public static final String DEFAULT_CHART_STYLE_NAME = "dg-chart";

	protected static final String RENDER_ATTR_NAME_FOR_NOT_FOUND_SCRIPT = StringUtil
			.firstLowerCase(Global.PRODUCT_NAME_EN) + "RenderValueForNotFound";

	private DashboardWidgetResManager dashboardWidgetResManager;

	private ChartWidgetSource chartWidgetSource;

	private DashboardThemeSource dashboardThemeSource = new SimpleDashboardThemeSource();

	private HtmlDashboardScriptObjectWriter htmlDashboardScriptObjectWriter = new HtmlDashboardScriptObjectWriter();

	private HtmlChartWidget<HtmlRenderContext> htmlChartWidgetForNotFound = new HtmlChartWidget<HtmlRenderContext>(
			StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "HtmlChartWidgetForNotFound",
			new ValueHtmlChartPlugin<HtmlRenderContext>(
					StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "HtmlChartPluginForNotFound",
					RENDER_ATTR_NAME_FOR_NOT_FOUND_SCRIPT));

	/** 内置导入内容 */
	private List<HtmlDashboardImport> dashboardImports;

	/** 上下文路径占位符 */
	private String contextPathPlaceholder = DEFAULT_CONTEXT_PATH_PLACE_HOLDER;

	/** 扩展看板初始化脚本 */
	private String extDashboardInitScript;

	/** 看板变量占位符 */
	private String dashboardVarPlaceholder = DEFAULT_DASHBOARD_VAR_PLACE_HOLDER;

	/** 默认JS看板渲染器变量名 */
	private String defaultDashboardRendererVar = DEFAULT_DASHBOARD_RENDERER_VAR;

	/** JS看板渲染器的渲染函数名 */
	private String dashboardRendererRenderFunctionName = "render";

	/** 主题导入名 */
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

	public List<HtmlDashboardImport> getDashboardImports()
	{
		return dashboardImports;
	}

	public void setDashboardImports(List<HtmlDashboardImport> dashboardImports)
	{
		this.dashboardImports = dashboardImports;
	}

	public String getContextPathPlaceholder()
	{
		return contextPathPlaceholder;
	}

	public void setContextPathPlaceholder(String contextPathPlaceholder)
	{
		this.contextPathPlaceholder = contextPathPlaceholder;
	}

	public String getExtDashboardInitScript()
	{
		return extDashboardInitScript;
	}

	public void setExtDashboardInitScript(String extDashboardInitScript)
	{
		this.extDashboardInitScript = extDashboardInitScript;
	}

	public String getDashboardVarPlaceholder()
	{
		return dashboardVarPlaceholder;
	}

	public void setDashboardVarPlaceholder(String dashboardVarPlaceholder)
	{
		this.dashboardVarPlaceholder = dashboardVarPlaceholder;
	}

	public String getDefaultDashboardRendererVar()
	{
		return defaultDashboardRendererVar;
	}

	public void setDefaultDashboardRendererVar(String defaultDashboardRendererVar)
	{
		this.defaultDashboardRendererVar = defaultDashboardRendererVar;
	}

	public String getDashboardRendererRenderFunctionName()
	{
		return dashboardRendererRenderFunctionName;
	}

	public void setDashboardRendererRenderFunctionName(String dashboardRendererRenderFunctionName)
	{
		this.dashboardRendererRenderFunctionName = dashboardRendererRenderFunctionName;
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

		if (!StringUtil.isEmpty(dashboardWidget.getTemplateEncoding()))
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

		if (!StringUtil.isEmpty(dashboardWidget.getTemplateEncoding()))
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
	 * @param renderContextNoAttrs
	 * @throws IOException
	 */
	protected void writeHtmlDashboardJSVar(Writer out, HtmlDashboard dashboard, boolean renderContextNoAttrs)
			throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();

		out.write("var ");
		out.write(varName);
		out.write("=");
		writeNewLine(out);
		getHtmlDashboardScriptObjectWriter().write(out, dashboard, renderContextNoAttrs);
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
	 * dashboard.charts.push(...);
	 * ...
	 * };
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param out
	 * @param dashboard
	 * @param renderContextVar
	 * @throws IOException
	 */
	protected void writeHtmlDashboardJSInit(Writer out, HtmlDashboard dashboard, String renderContextVar)
			throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();

		HtmlRenderContext renderContext = dashboard.getRenderContext();

		out.write("var ");
		out.write(renderContextVar);
		out.write("=");
		writeNewLine(out);
		getHtmlDashboardScriptObjectWriter().writeRenderContext(out, renderContext, true);
		out.write(";");
		writeNewLine(out);
		out.write(varName + ".renderContext.attributes = " + renderContextVar + ".attributes;");
		writeNewLine(out);

		List<Chart> charts = dashboard.getCharts();
		if (charts != null)
		{
			for (Chart chart : charts)
			{
				if (!(chart instanceof HtmlChart))
					continue;

				out.write(varName + ".charts.push(" + ((HtmlChart) chart).getVarName() + ");");
				writeNewLine(out);
			}
		}

		if (!StringUtil.isEmpty(this.extDashboardInitScript))
		{
			out.write(replaceDashboardVarPlaceholder(this.extDashboardInitScript, varName));
			writeNewLine(out);
		}
	}

	/**
	 * 写{@linkplain HtmlDashboard} JS渲染代码：
	 * <p>
	 * <code>
	 * <pre>
	 * window.onload = function(){
	 * dashboardRenderer.render();
	 * };
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param out
	 * @param dashboard
	 * @param dashboardRendererVar
	 *            如果为{@code null}，则使用{@linkplain #getDefaultDashboardRendererVar()}
	 * @throws IOException
	 */
	protected void writeHtmlDashboardJSRender(Writer out, HtmlDashboard dashboard, String dashboardRendererVar)
			throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();

		if (StringUtil.isEmpty(dashboardRendererVar))
			dashboardRendererVar = this.defaultDashboardRendererVar;

		out.write("window.onload = function(){");
		writeNewLine(out);

		out.write("  " + dashboardRendererVar + "." + this.dashboardRendererRenderFunctionName + "(" + varName + ");");
		writeNewLine(out);

		out.write("};");
		writeNewLine(out);
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

		if (this.dashboardImports != null)
		{
			for (HtmlDashboardImport impt : this.dashboardImports)
			{
				String name = impt.getName();

				if (excludes.contains(name))
					continue;

				String content = replaceContextPathPlaceholder(impt.getContent(),
						renderContext.getWebContext().getContextPath());

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
		out.write("}");
		writeNewLine(out);

		out.write("." + this.chartStyleName + "{");
		writeNewLine(out);
		writeStyleAttrs(out, dashboardTheme.getChartTheme());
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
	 * 替换字符串中的看板变量名占位符为真实的看板变量名。
	 * 
	 * @param str
	 * @param dashboardVar
	 * @return
	 */
	protected String replaceDashboardVarPlaceholder(String str, String dashboardVar)
	{
		if (StringUtil.isEmpty(str))
			return str;

		return str.replace(getDashboardVarPlaceholder(), dashboardVar);
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
