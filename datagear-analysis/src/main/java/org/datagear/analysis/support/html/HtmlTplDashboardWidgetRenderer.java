/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.analysis.Theme;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 抽象{@linkplain HtmlTplDashboardWidget}渲染器。
 * <p>
 * 此类的{@linkplain #writeHtmlTplDashboardJSFactoryInit(Writer, HtmlTplDashboard, String)}方法的JS看板渲染逻辑为：
 * </p>
 * <code>
 * <pre>
 * dashboardFactory.init(dashboard);
 * </pre>
 * </code>
 * <p>
 * 因此，看板页面应该定义如下JS看板工厂对象：
 * </p>
 * <code>
 * <pre>
 * var dashboardFactory =
 * {
 *   init : function(dashboard)
 *   {
 *     ...
 *   }
 * };
 * </pre>
 * </code>
 * <p>
 * 子类在调用此方法时可以传入自定义JS看板工厂对象的变量名，默认为{@linkplain #getDefaultDashboardFactoryVar()}。
 * </p>
 * <p>
 * 此类的{@linkplain #getHtmlTplDashboardImports()}的{@linkplain HtmlTplDashboardImport#getContent()}可以包含{@linkplain #getContextPathPlaceholder()}占位符，
 * 在渲染时，占位符会被替换为实际的{@linkplain HtmlRenderContext#getContextPath()}。
 * </p>
 * <p>
 * 此类的{@linkplain #getHtmlTplDashboardImports()}的{@linkplain HtmlTplDashboardImport#getContent()}可以包含{@linkplain #getVersionPlaceholder()}占位符，
 * 在渲染时，占位符会被替换为{@linkplain Global#VERSION}（可用于支持版本更新时浏览器缓存更新）。
 * </p>
 * <p>
 * 此类的{@linkplain #getExtDashboardInitScript()}可以包含{@linkplain #getDashboardVarPlaceholder()}占位符，
 * 在渲染时，占位符会被替换为实际的{@linkplain HtmlTplDashboard#getVarName()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class HtmlTplDashboardWidgetRenderer<T extends HtmlRenderContext> extends TextParserSupport
{
	public static final String DEFAULT_CONTEXT_PATH_PLACE_HOLDER = "$CONTEXTPATH";

	public static final String DEFAULT_VERSION_PLACE_HOLDER = "$VERSION";

	public static final String DEFAULT_DASHBOARD_VAR_PLACE_HOLDER = "$DASHBOARD";

	public static final String DEFAULT_DASHBOARD_FACTORY_VAR = "dashboardFactory";

	public static final String DEFAULT_THEME_IMPORT_NAME = "dg-theme";

	public static final String DEFAULT_DASHBOARD_STYLE_NAME = "dg-dashboard";

	public static final String DEFAULT_CHART_STYLE_NAME = "dg-chart";

	public static final String DEFAULT_DASHBOARD_VAR = "dashboard";

	public static final String PROPERTY_VALUE_FOR_WIDGET_GET_EXCEPTION = "targetHtmlChartWidgetGetExceptionMessage";

	public static final String PROPERTY_VALUE_FOR_WIDGET_NOT_FOUND = "targetHtmlChartWidgetNotFoundMessage";

	public static final String PROPERTY_VALUE_FOR_PLUGIN_NULL = "targetHtmlChartWidgePluginNullMessage";

	private TemplateDashboardWidgetResManager templateDashboardWidgetResManager;

	private ChartWidgetSource chartWidgetSource;

	private DashboardThemeSource dashboardThemeSource = new SimpleDashboardThemeSource();

	private HtmlRenderContextScriptObjectWriter htmlRenderContextScriptObjectWriter = new HtmlRenderContextScriptObjectWriter();

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = new HtmlChartPluginScriptObjectWriter();

	private HtmlTplDashboardScriptObjectWriter htmlTplDashboardScriptObjectWriter = new HtmlTplDashboardScriptObjectWriter();

	private HtmlChartPlugin<HtmlRenderContext> htmlChartPluginForWidgetGetException = new ValueHtmlChartPlugin<>(
			StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "HtmlChartPluginForWidgetGetException",
			PROPERTY_VALUE_FOR_WIDGET_GET_EXCEPTION);

	private HtmlChartPlugin<HtmlRenderContext> htmlChartPluginForWidgetNotFound = new ValueHtmlChartPlugin<>(
			StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "HtmlChartPluginForWidgetNotFound",
			PROPERTY_VALUE_FOR_WIDGET_NOT_FOUND);

	private HtmlChartPlugin<HtmlRenderContext> htmlChartPluginForWidgetPluginNull = new ValueHtmlChartPlugin<>(
			StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "HtmlChartPluginForWidgetPluginNull",
			PROPERTY_VALUE_FOR_PLUGIN_NULL);

	/** 内置导入内容 */
	private List<HtmlTplDashboardImport> dashboardImports;

	/** 上下文路径占位符 */
	private String contextPathPlaceholder = DEFAULT_CONTEXT_PATH_PLACE_HOLDER;

	/** 应用版本号占位符 */
	private String versionPlaceholder = DEFAULT_VERSION_PLACE_HOLDER;

	/** 扩展看板初始化脚本 */
	private String extDashboardInitScript;

	/** 看板变量占位符 */
	private String dashboardVarPlaceholder = DEFAULT_DASHBOARD_VAR_PLACE_HOLDER;

	/** 默认JS看板工厂变量名 */
	private String defaultDashboardFactoryVar = DEFAULT_DASHBOARD_FACTORY_VAR;

	/** JS看板工厂初始化函数名 */
	private String dashboardFactoryInitFuncName = "init";

	/** 看板对象渲染函数名 */
	private String dashboardRenderFuncName = "render";

	/** 主题导入名 */
	private String themeImportName = DEFAULT_THEME_IMPORT_NAME;

	/** 主题中的看板样式名 */
	private String dashboardStyleName = DEFAULT_DASHBOARD_STYLE_NAME;

	/** 主题中的图表样式名 */
	private String chartStyleName = DEFAULT_CHART_STYLE_NAME;

	/** 默认看板变量名 */
	private String defaultDashboardVar = DEFAULT_DASHBOARD_VAR;

	/** 换行符 */
	private String newLine = HtmlChartPlugin.HTML_NEW_LINE;

	public HtmlTplDashboardWidgetRenderer()
	{
		super();
	}

	public HtmlTplDashboardWidgetRenderer(TemplateDashboardWidgetResManager templateDashboardWidgetResManager,
			ChartWidgetSource chartWidgetSource)
	{
		super();
		this.templateDashboardWidgetResManager = templateDashboardWidgetResManager;
		this.chartWidgetSource = chartWidgetSource;
	}

	public TemplateDashboardWidgetResManager getTemplateDashboardWidgetResManager()
	{
		return templateDashboardWidgetResManager;
	}

	public void setTemplateDashboardWidgetResManager(
			TemplateDashboardWidgetResManager templateDashboardWidgetResManager)
	{
		this.templateDashboardWidgetResManager = templateDashboardWidgetResManager;
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

	public HtmlRenderContextScriptObjectWriter getHtmlRenderContextScriptObjectWriter()
	{
		return htmlRenderContextScriptObjectWriter;
	}

	public void setHtmlRenderContextScriptObjectWriter(
			HtmlRenderContextScriptObjectWriter htmlRenderContextScriptObjectWriter)
	{
		this.htmlRenderContextScriptObjectWriter = htmlRenderContextScriptObjectWriter;
	}

	public HtmlChartPluginScriptObjectWriter getHtmlChartPluginScriptObjectWriter()
	{
		return htmlChartPluginScriptObjectWriter;
	}

	public void setHtmlChartPluginScriptObjectWriter(
			HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter)
	{
		this.htmlChartPluginScriptObjectWriter = htmlChartPluginScriptObjectWriter;
	}

	public HtmlTplDashboardScriptObjectWriter getHtmlTplDashboardScriptObjectWriter()
	{
		return htmlTplDashboardScriptObjectWriter;
	}

	public void setHtmlTplDashboardScriptObjectWriter(
			HtmlTplDashboardScriptObjectWriter htmlTplDashboardScriptObjectWriter)
	{
		this.htmlTplDashboardScriptObjectWriter = htmlTplDashboardScriptObjectWriter;
	}

	public HtmlChartPlugin<HtmlRenderContext> getHtmlChartPluginForWidgetGetException()
	{
		return htmlChartPluginForWidgetGetException;
	}

	public void setHtmlChartPluginForWidgetGetException(
			HtmlChartPlugin<HtmlRenderContext> htmlChartPluginForWidgetGetException)
	{
		this.htmlChartPluginForWidgetGetException = htmlChartPluginForWidgetGetException;
	}

	public HtmlChartPlugin<HtmlRenderContext> getHtmlChartPluginForWidgetNotFound()
	{
		return htmlChartPluginForWidgetNotFound;
	}

	public void setHtmlChartPluginForWidgetNotFound(HtmlChartPlugin<HtmlRenderContext> htmlChartPluginForWidgetNotFound)
	{
		this.htmlChartPluginForWidgetNotFound = htmlChartPluginForWidgetNotFound;
	}

	public HtmlChartPlugin<HtmlRenderContext> getHtmlChartPluginForWidgetPluginNull()
	{
		return htmlChartPluginForWidgetPluginNull;
	}

	public void setHtmlChartPluginForWidgetPluginNull(
			HtmlChartPlugin<HtmlRenderContext> htmlChartPluginForWidgetPluginNull)
	{
		this.htmlChartPluginForWidgetPluginNull = htmlChartPluginForWidgetPluginNull;
	}

	public List<HtmlTplDashboardImport> getDashboardImports()
	{
		return dashboardImports;
	}

	public void setDashboardImports(List<HtmlTplDashboardImport> dashboardImports)
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

	public String getVersionPlaceholder()
	{
		return versionPlaceholder;
	}

	public void setVersionPlaceholder(String versionPlaceholder)
	{
		this.versionPlaceholder = versionPlaceholder;
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

	public String getDefaultDashboardFactoryVar()
	{
		return defaultDashboardFactoryVar;
	}

	public void setDefaultDashboardFactoryVar(String defaultDashboardFactoryVar)
	{
		this.defaultDashboardFactoryVar = defaultDashboardFactoryVar;
	}

	public String getDashboardFactoryInitFuncName()
	{
		return dashboardFactoryInitFuncName;
	}

	public void setDashboardFactoryInitFuncName(String dashboardFactoryInitFuncName)
	{
		this.dashboardFactoryInitFuncName = dashboardFactoryInitFuncName;
	}

	public String getDashboardRenderFuncName()
	{
		return dashboardRenderFuncName;
	}

	public void setDashboardRenderFuncName(String dashboardRenderFuncName)
	{
		this.dashboardRenderFuncName = dashboardRenderFuncName;
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

	public String getDefaultDashboardVar()
	{
		return defaultDashboardVar;
	}

	public void setDefaultDashboardVar(String defaultDashboardVar)
	{
		this.defaultDashboardVar = defaultDashboardVar;
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
	 * 解析HTML输入流的字符集，如果解析不到，则返回{@code null}。
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public String resolveCharset(InputStream in) throws IOException
	{
		Reader reader = IOUtil.getReader(in, "iso-8859-1");
		return resolveCharset(reader);
	}

	/**
	 * 解析HTML输入流的字符集，如果解析不到，则返回{@code null}。
	 * <p>
	 * 它从如下内容解析HTML字符集：
	 * </p>
	 * <p>
	 * <code>&lt;meta http-equiv="Content-Type" content="text/html; charset=***"&gt;</code>
	 * </p>
	 * <p>
	 * <code>&lt;meta charset="***"&gt;</code>
	 * </p>
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public String resolveCharset(Reader in) throws IOException
	{
		String charset = null;

		StringBuilder nameCache = createStringBuilder();

		int c = -1;
		while ((c = in.read()) > -1)
		{
			if (charset != null)
				break;

			if (c == '<')
			{
				clear(nameCache);

				int last = readHtmlTagName(in, nameCache);
				String tagName = nameCache.toString();

				// </head
				if ("/head".equalsIgnoreCase(tagName))
					break;
				// <meta
				else if ("meta".equalsIgnoreCase(tagName))
				{
					StringBuilder cache = createStringBuilder();
					StringBuilder valueCache = createStringBuilder();

					for (;;)
					{
						clear(nameCache);
						clear(valueCache);

						last = resolveHtmlTagAttr(in, last, cache, nameCache, valueCache);

						String name = toString(nameCache);
						String value = toString(valueCache);

						if ("charset".equalsIgnoreCase(name))
						{
							charset = value;
							break;
						}
						else if ("content".equalsIgnoreCase(name))
						{
							String valueLower = value.toLowerCase();
							String charsetToken = "charset=";
							int charsetTokenIdx = valueLower.indexOf(charsetToken);
							if (charsetTokenIdx > -1)
							{
								charset = value.substring(charsetTokenIdx + charsetToken.length());
								break;
							}
						}

						if (isHtmlTagEnd(last))
							break;
					}
				}
				// <!--
				else if (tagName.startsWith("!--"))
				{
					// 空注释
					if (isReadHtmlTagEmptyComment(tagName, last))
						;
					else
						skipHtmlComment(in, null);
				}
			}
		}

		return charset;
	}

	/**
	 * 设置必要的{@linkplain RenderStyle}。
	 * <p>
	 * 如果已设置，则返回；否则，设置默认并返回。
	 * </p>
	 * 
	 * @param renderContext
	 * @return
	 */
	public RenderStyle inflateRenderStyle(HtmlRenderContext renderContext)
	{
		RenderStyle renderStyle = HtmlRenderAttributes.getRenderStyle(renderContext);

		if (renderStyle == null)
		{
			renderStyle = RenderStyle.LIGHT;
			HtmlRenderAttributes.setRenderStyle(renderContext, renderStyle);
		}

		return renderStyle;
	}

	/**
	 * 设置必要的{@linkplain DashboardTheme}。
	 * <p>
	 * 如果已设置，则返回；否则，设置默认并返回。
	 * </p>
	 * 
	 * @param renderContext
	 * @param renderStyle
	 */
	public DashboardTheme inflateDashboardTheme(HtmlRenderContext renderContext, RenderStyle renderStyle)
	{
		DashboardTheme dashboardTheme = HtmlRenderAttributes.getDashboardTheme(renderContext);

		if (dashboardTheme == null)
		{
			dashboardTheme = getDashboardTheme(renderStyle);
			HtmlRenderAttributes.setDashboardTheme(renderContext, dashboardTheme);
		}

		HtmlRenderAttributes.setChartTheme(renderContext, dashboardTheme.getChartTheme());

		return dashboardTheme;
	}

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @param dashboardWidget
	 * @param template
	 * @return
	 * @throws RenderException
	 */
	public HtmlTplDashboard render(T renderContext, HtmlTplDashboardWidget<T> dashboardWidget, String template)
			throws RenderException
	{
		RenderStyle renderStyle = inflateRenderStyle(renderContext);
		inflateDashboardTheme(renderContext, renderStyle);

		HtmlTplDashboard dashboard = createHtmlTplDashboard(renderContext, dashboardWidget, template);

		try
		{
			renderHtmlTplDashboard(renderContext, dashboard);
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
	 * @param template
	 * @return
	 * @throws IOException
	 */
	public String readTemplateContent(HtmlTplDashboardWidget<T> dashboardWidget, String template) throws IOException
	{
		Reader reader = getTemplateReaderNotNull(dashboardWidget, template);

		return IOUtil.readString(reader, true);
	}

	/**
	 * 保存指定指定{@linkplain HtmlTplDashboardWidget}的模板内容。
	 * 
	 * @param dashboardWidget
	 * @param template
	 * @param templateContent
	 * @throws IOException
	 */
	public void saveTemplateContent(HtmlTplDashboardWidget<T> dashboardWidget, String template, String templateContent)
			throws IOException
	{
		Writer writer = null;

		try
		{
			writer = getTemplateWriter(dashboardWidget, template);
			writer.write(templateContent);
		}
		finally
		{
			IOUtil.close(writer);
		}
	}

	/**
	 * 生成基本的模板内容。
	 * 
	 * @param htmlCharset
	 * @param chartWidgetId
	 * @return
	 */
	public abstract String simpleTemplateContent(String htmlCharset, String... chartWidgetId);

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @throws Throwable
	 */
	protected abstract void renderHtmlTplDashboard(T renderContext, HtmlTplDashboard dashboard) throws Throwable;

	/**
	 * 获取{@linkplain HtmlTplDashboardWidget}的模板输入流。
	 * 
	 * @param dashboardWidget
	 * @param template
	 * @return
	 * @throws IOException
	 */
	protected Reader getTemplateReaderNotNull(HtmlTplDashboardWidget<?> dashboardWidget, String template)
			throws IOException
	{
		return getTemplateDashboardWidgetResManager().getTemplateReader(dashboardWidget, template);
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardWidget}的模板输入流。
	 * 
	 * @param dashboardWidget
	 * @param template
	 * @return
	 * @throws IOException
	 */
	protected Writer getTemplateWriter(HtmlTplDashboardWidget<?> dashboardWidget, String template) throws IOException
	{
		return getTemplateDashboardWidgetResManager().getTemplateWriter(dashboardWidget, template);
	}

	protected DashboardTheme getDashboardTheme(RenderStyle renderStyle)
	{
		DashboardTheme dashboardTheme = (renderStyle == null ? null
				: this.dashboardThemeSource.getDashboardTheme(renderStyle));

		if (dashboardTheme == null)
			dashboardTheme = this.dashboardThemeSource.getDashboardTheme();

		return dashboardTheme;
	}

	/**
	 * 获取用于渲染指定ID图表的{@linkplain ChartWidget}。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * <p>
	 * 如果没有找到，则返回{@linkplain #createHtmlChartWidgetForNotFound(String)}。
	 * </p>
	 * <p>
	 * 如果出现异常，则返回{@linkplain #createHtmlChartWidgetForGetException(String, Throwable)}。
	 * </p>
	 * <p>
	 * 如果{@linkplain HtmlChartWidget#getChartPlugin()}为{@code null}，则返回{@linkplain #createHtmlChartWidgetForPluginNull(ChartWidget)}。
	 * </p>
	 * 
	 * @param renderContext
	 * @param id
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected HtmlChartWidget<HtmlRenderContext> getHtmlChartWidgetForRender(HtmlRenderContext renderContext, String id)
	{
		ChartWidget chartWidget = null;

		if (!StringUtil.isEmpty(id))
		{
			try
			{
				chartWidget = this.chartWidgetSource.getChartWidget(id);
			}
			catch (Throwable t)
			{
				chartWidget = createHtmlChartWidgetForGetException(id, t);
			}
		}

		if (chartWidget == null)
			chartWidget = createHtmlChartWidgetForNotFound(id);

		if (chartWidget.getPlugin() == null)
			chartWidget = createHtmlChartWidgetForPluginNull(chartWidget);

		return (HtmlChartWidget<HtmlRenderContext>) chartWidget;
	}

	protected HtmlChartWidget<HtmlRenderContext> createHtmlChartWidgetForGetException(String exceptionWidgetId,
			Throwable t)
	{
		HtmlChartWidget<HtmlRenderContext> widget = new HtmlChartWidget<>(IDUtil.uuid(),
				"HtmlChartWidgetForWidgetGetException", ChartDefinition.EMPTY_CHART_DATA_SET,
				this.htmlChartPluginForWidgetGetException);

		widget.setChartParamValue(PROPERTY_VALUE_FOR_WIDGET_GET_EXCEPTION,
				"Chart '" + (exceptionWidgetId == null ? "" : exceptionWidgetId) + "' exception : " + t.getMessage());

		return widget;
	}

	protected HtmlChartWidget<HtmlRenderContext> createHtmlChartWidgetForNotFound(String notFoundWidgetId)
	{
		HtmlChartWidget<HtmlRenderContext> widget = new HtmlChartWidget<>(IDUtil.uuid(),
				"HtmlChartWidgetForWidgetNotFound", ChartDefinition.EMPTY_CHART_DATA_SET,
				this.htmlChartPluginForWidgetNotFound);

		widget.setChartParamValue(PROPERTY_VALUE_FOR_WIDGET_NOT_FOUND,
				"Chart '" + (notFoundWidgetId == null ? "" : notFoundWidgetId) + "' not found");

		return widget;
	}

	protected HtmlChartWidget<HtmlRenderContext> createHtmlChartWidgetForPluginNull(ChartWidget<?> chartWidget)
	{
		HtmlChartWidget<HtmlRenderContext> widget = new HtmlChartWidget<>(IDUtil.uuid(),
				"HtmlChartWidgetForWidgetPluginNull", ChartDefinition.EMPTY_CHART_DATA_SET,
				this.htmlChartPluginForWidgetPluginNull);

		widget.setChartParamValue(PROPERTY_VALUE_FOR_PLUGIN_NULL,
				"Chart plugin for rendering chart '" + chartWidget.getName() + "' not found");

		return widget;
	}

	/**
	 * 写{@linkplain HtmlTplDashboard} JS变量：
	 * <p>
	 * <code>var dashboard = {...};</code>
	 * </p>
	 * 
	 * @param renderContext
	 * @param out
	 * @param dashboard
	 * @throws IOException
	 */
	protected void writeHtmlTplDashboardJSVar(HtmlRenderContext renderContext, Writer out, HtmlTplDashboard dashboard)
			throws IOException
	{
		if (StringUtil.isEmpty(dashboard.getVarName()))
			throw new IllegalArgumentException();

		String tmpRenderContextVarName = HtmlRenderAttributes
				.generateRenderContextVarName(Long.toHexString(System.currentTimeMillis()));
		getHtmlRenderContextScriptObjectWriter().writeNoAttributes(out, renderContext, tmpRenderContextVarName);

		getHtmlTplDashboardScriptObjectWriter().write(out, dashboard, tmpRenderContextVarName);
	}

	/**
	 * 写{@linkplain HtmlTplDashboard} JS初始化代码：
	 * <p>
	 * <code>
	 * <pre>
	 * var tmpRenderContext = {...};
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
	 * @throws IOException
	 */
	protected void writeHtmlTplDashboardJSInit(Writer out, HtmlTplDashboard dashboard) throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();

		HtmlRenderContext renderContext = dashboard.getRenderContext();

		String tmpRenderContextVar = HtmlRenderAttributes.generateRenderContextVarName(renderContext);

		ChartTheme chartTheme = HtmlRenderAttributes.removeChartTheme(renderContext);

		getHtmlRenderContextScriptObjectWriter().writeOnlyAttributes(out, renderContext, tmpRenderContextVar);
		out.write(varName + "." + Dashboard.PROPERTY_RENDER_CONTEXT + "." + RenderContext.PROPERTY_ATTRIBUTES + " = "
				+ tmpRenderContextVar + "." + RenderContext.PROPERTY_ATTRIBUTES + ";");
		writeNewLine(out);
		out.write(varName + "." + Dashboard.PROPERTY_RENDER_CONTEXT + "." + RenderContext.PROPERTY_ATTRIBUTES + "."
				+ HtmlRenderAttributes.CHART_THEME + " = " + tmpRenderContextVar + "."
				+ RenderContext.PROPERTY_ATTRIBUTES + "." + HtmlRenderAttributes.DASHBOARD_THEME + "."
				+ DashboardTheme.PROPERTY_CHART_THEME + ";");
		writeNewLine(out);

		HtmlRenderAttributes.setChartTheme(renderContext, chartTheme);

		List<Chart> charts = dashboard.getCharts();
		if (charts != null)
		{
			for (Chart chart : charts)
			{
				if (!(chart instanceof HtmlChart))
					continue;

				out.write(
						varName + "." + Dashboard.PROPERTY_CHARTS + ".push(" + ((HtmlChart) chart).getVarName() + ");");
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
	 * 写{@linkplain HtmlTplDashboard} JS工厂初始化代码：
	 * <p>
	 * <code>
	 * <pre>
	 * dashboardFactory.init(dashboard);
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param out
	 * @param dashboard
	 * @param dashboardFactoryVar
	 *            如果为{@code null}，则使用{@linkplain #getDefaultDashboardFactoryVar()}
	 * @throws IOException
	 */
	protected void writeHtmlTplDashboardJSFactoryInit(Writer out, HtmlTplDashboard dashboard,
			String dashboardFactoryVar) throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();

		if (StringUtil.isEmpty(dashboardFactoryVar))
			dashboardFactoryVar = this.defaultDashboardFactoryVar;

		out.write(dashboardFactoryVar + "." + this.dashboardFactoryInitFuncName + "(" + varName + ");");
		writeNewLine(out);
	}

	/**
	 * 写{@linkplain HtmlTplDashboard} JS渲染代码：
	 * <p>
	 * <code>
	 * <pre>
	 * if(typeof($) != "undefined")
	 * 	$(document).ready(function(){ dashboard.render(); });
	 * else
	 * 	window.onload = function(){ dashboard.render(); };
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param out
	 * @param dashboard
	 * @throws IOException
	 */
	protected void writeHtmlTplDashboardJSRender(Writer out, HtmlTplDashboard dashboard) throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();

		out.write("if(typeof($) != \"undefined\")");
		writeNewLine(out);
		out.write("  $(document).ready(function(){ " + varName + "." + this.dashboardRenderFuncName + "(); });");
		writeNewLine(out);
		out.write("else");
		writeNewLine(out);
		out.write("  window.onload = function(){ " + varName + "." + this.dashboardRenderFuncName + "(); };");
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
	protected void writeDashboardImport(HtmlRenderContext renderContext, HtmlTplDashboard dashboard,
			String importExclude) throws IOException
	{
		Writer out = renderContext.getWriter();

		List<String> excludes = StringUtil.splitWithTrim(importExclude, ",");

		if (this.dashboardImports != null)
		{
			for (HtmlTplDashboardImport impt : this.dashboardImports)
			{
				String name = impt.getName();

				if (excludes.contains(name))
					continue;

				String content = replaceContextPathPlaceholder(impt.getContent(),
						renderContext.getWebContext().getContextPath());

				content = replaceVersionPlaceholder(content, Global.VERSION);

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
	 * 写{@linkplain HtmlChart}。
	 * 
	 * @param chartWidget
	 * @param renderContext
	 * @return
	 */
	protected HtmlChart writeHtmlChart(HtmlChartWidget<HtmlRenderContext> chartWidget, HtmlRenderContext renderContext)
			throws RenderException
	{
		return chartWidget.render(renderContext);
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
	protected boolean writeDashboardThemeStyle(HtmlRenderContext renderContext, HtmlTplDashboard dashboard, Writer out)
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
			if (StringUtil.isEmpty(borderWidth))
				borderWidth = "0";

			out.write("  color: " + theme.getColor() + ";");
			writeNewLine(out);
			out.write("  background-color: " + theme.getBackgroundColor() + ";");
			writeNewLine(out);
			out.write("  border-color: " + theme.getBorderColor() + ";");
			writeNewLine(out);
			out.write("  border-width: " + borderWidth + ";");
			writeNewLine(out);
			out.write("  border-style: solid;");
			writeNewLine(out);
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
	 * 替换字符串中的版本占位符为真实的版本。
	 * 
	 * @param str
	 * @param version
	 * @return
	 */
	protected String replaceVersionPlaceholder(String str, String version)
	{
		if (StringUtil.isEmpty(str))
			return str;

		if (version == null)
			version = "";

		return str.replace(getVersionPlaceholder(), version);
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
	 * 解析HTML标签名。
	 * 
	 * @param in
	 * @param cache
	 *            写入已读取字符的字符缓存，为{@code null}则不写入
	 * @param tagName
	 * @return '>'、'/'、空格、-1
	 * @throws IOException
	 */
	protected int resolveHtmlTagName(Reader in, StringBuilder cache, StringBuilder tagName) throws IOException
	{
		int c = -1;
		while ((c = in.read()) > -1)
		{
			appendChar(cache, c);

			if (c == '>' || c == '/')
			{
				break;
			}
			else if (isWhitespace(c))
			{
				if (isNotEmpty(tagName))
					break;
			}
			else
				appendChar(tagName, c);
		}

		return c;
	}

	/**
	 * 解析HTML标签属性。
	 * 
	 * @param in
	 * @param last
	 *            上一个已读取的字符
	 * @param out
	 *            写入已读取字符的缓存
	 * @param attrName
	 *            属性名写入缓存
	 * @param attrValue
	 *            属性值写入缓存，引号不会写入
	 * @return '>'、'/'、空格、下一个属性名的第一个字符、-1
	 * @throws IOException
	 */
	protected int resolveHtmlTagAttr(Reader in, int last, StringBuilder out, StringBuilder attrName,
			StringBuilder attrValue) throws IOException
	{
		// 上一个字符是标签结束字符
		if (isHtmlTagEnd(last))
			return last;

		// 上一个字符是此属性名的第一个字符
		if (last != '/' && !isWhitespace(last))
			appendChar(attrName, last);

		boolean resolveAttValue = false;

		int c = -1;
		while ((c = in.read()) > -1)
		{
			appendChar(out, c);

			if (c == '>' || c == '/')
			{
				break;
			}
			else if (c == '=')
			{
				if (isNotEmpty(attrName))
					resolveAttValue = true;
				else
					appendChar(attrName, c);
			}
			else if (c == '\'' || c == '"')
			{
				if (!resolveAttValue)
					appendChar(attrName, c);
				else
				{
					if (!isEmpty(attrValue))
						appendChar(attrValue, c);
					else
					{
						boolean endQuote = false;
						int quote = c;

						while ((c = in.read()) > -1)
						{
							appendChar(out, c);

							if (c == quote)
							{
								c = in.read();
								appendChar(out, c);

								endQuote = true;
								break;
							}
							else
								appendChar(attrValue, c);
						}

						if (endQuote)
							break;
					}
				}
			}
			else if (isWhitespace(c))
			{
				if (isNotEmpty(attrValue))
					break;
			}
			else
			{
				if (resolveAttValue)
					appendChar(attrValue, c);
				else
				{
					int prev = (isEmpty(out) ? 0 : out.charAt(out.length() - 1));

					// 只有属性名没有属性值
					if (isWhitespace(prev))
						break;
					else
						appendChar(attrName, c);
				}
			}
		}

		return c;
	}

	/**
	 * 读取字符到“{@code -->}”。
	 * 
	 * @param in
	 * @param out
	 *            为{@code null}则不写入
	 * @throws IOException
	 */
	protected void skipHtmlComment(Reader in, Writer out) throws IOException
	{
		int ppc = -1;
		int pc = -1;
		int c = -1;

		while ((c = in.read()) > -1)
		{
			if (out != null)
				out.write(c);

			if (c == '>' && pc == '-' && ppc == '-')
				break;

			ppc = pc;
			pc = c;
		}
	}

	/**
	 * 读取HTML标签名。
	 * 
	 * @param in
	 * @param out
	 * @return 最后读取的不是标签名的字符：空格、>、-1，这个字符不会写入{@code out}
	 * @throws IOException
	 */
	protected int readHtmlTagName(Reader in, StringBuilder out) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			if (isWhitespace(c) || c == '>')
				break;

			appendChar(out, c);
		}

		return c;
	}

	/**
	 * 读取的标签名是否是空HTML注释：&lt;!----&gt;、&lt;!-------&gt;
	 * 
	 * @param tagName
	 * @param last
	 * @return
	 */
	protected boolean isReadHtmlTagEmptyComment(String tagName, int last)
	{
		return (tagName.length() >= 5 && tagName.endsWith("--") && last == '>');
	}

	/**
	 * 给定字符是否表明HTML标签结束：>、-1。
	 * 
	 * @param c
	 * @return
	 */
	protected boolean isHtmlTagEnd(int c)
	{
		return (c == '>' || c < 0);
	}

	/**
	 * 创建{@linkplain HtmlTplDashboard}实例。
	 * 
	 * @param renderContext
	 * @param dashboardWidget
	 * @param template
	 * @return
	 */
	protected HtmlTplDashboard createHtmlTplDashboard(T renderContext, HtmlTplDashboardWidget<T> dashboardWidget,
			String template)
	{
		HtmlTplDashboard dashboard = new HtmlTplDashboard();

		dashboard.setId(IDUtil.uuid());
		dashboard.setTemplate(template);
		dashboard.setWidget(dashboardWidget);
		dashboard.setRenderContext(renderContext);
		dashboard.setCharts(new ArrayList<Chart>());

		return dashboard;
	}

	/**
	 * HTML <code>&lt;title&gt;</code>内容处理器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface HtmlTitleHandler
	{
		/**
		 * 处理原始<code>&lt;title&gt;</code>内容并返回新内容。
		 * 
		 * @param rawTitle
		 * @return
		 */
		String handle(String rawTitle);
	}

	/**
	 * 为HTML标题添加前缀的{@linkplain HtmlTitleHandler}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class AddPrefixHtmlTitleHandler implements HtmlTitleHandler
	{
		private String prefix;

		public AddPrefixHtmlTitleHandler()
		{
			super();
		}

		public AddPrefixHtmlTitleHandler(String prefix)
		{
			super();
			this.prefix = prefix;
		}

		public String getPrefix()
		{
			return prefix;
		}

		public void setPrefix(String prefix)
		{
			this.prefix = prefix;
		}

		@Override
		public String handle(String rawTitle)
		{
			return this.prefix + rawTitle;
		}
	}
}
