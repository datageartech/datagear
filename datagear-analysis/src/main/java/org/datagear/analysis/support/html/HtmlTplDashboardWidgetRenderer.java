/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.analysis.Theme;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.html.HtmlTplDashboardImport.ImportItem;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 抽象{@linkplain HtmlTplDashboardWidget}渲染器。
 * <p>
 * 注意：此类{@linkplain #render(RenderContext, HtmlTplDashboardWidget, String)}的{@linkplain RenderContext}必须符合{@linkplain HtmlTplDashboardRenderAttr#inflate(RenderContext, Writer, WebContext)}规范。
 * </p>
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
 * 此类的{@linkplain #getHtmlTplDashboardImport()}的{@linkplain ImportItem#getContent()}可以包含{@linkplain #getContextPathPlaceholder()}占位符，
 * 在渲染时，占位符会被替换为{@linkplain HtmlTplDashboardRenderAttr#getWebContext(RenderContext)}的{@linkplain WebContext#getContextPath()}。
 * </p>
 * <p>
 * 此类的{@linkplain #getHtmlTplDashboardImport()}的{@linkplain ImportItem#getContent()}可以包含{@linkplain #getVersionPlaceholder()}占位符，
 * 在渲染时，占位符会被替换为{@linkplain Global#VERSION}（可用于支持版本更新时浏览器缓存更新）。
 * </p>
 * <p>
 * 此类的{@linkplain #getHtmlTplDashboardImport()}可以包含{@linkplain #getDashboardVarPlaceholder()}占位符，
 * 在渲染时，占位符会被替换为实际的{@linkplain HtmlTplDashboard#getVarName()}。
 * </p>
 * 
 * @author datagear@163.com
 * 
 */
public abstract class HtmlTplDashboardWidgetRenderer extends TextParserSupport
{
	public static final String DASHBOARD_IMPORT_ITEM_NAME_ATTR = "dg-import-name";

	public static final String DEFAULT_CONTEXT_PATH_PLACE_HOLDER = "$CONTEXTPATH";

	public static final String DEFAULT_VERSION_PLACE_HOLDER = "$VERSION";

	public static final String DEFAULT_RANDOMCODE_VAR_PLACE_HOLDER = "$RANDOMCODE";

	public static final String DEFAULT_DASHBOARD_VAR_PLACE_HOLDER = "$DASHBOARD";

	public static final String DEFAULT_DASHBOARD_FACTORY_VAR = "dashboardFactory";

	public static final String DEFAULT_THEME_IMPORT_NAME = "dashboardThemeStyle";

	public static final String DEFAULT_DASHBOARD_STYLE_NAME = "dg-dashboard";

	public static final String DEFAULT_CHART_STYLE_NAME = "dg-chart";

	public static final String DEFAULT_DASHBOARD_VAR = "dashboard";

	private TemplateDashboardWidgetResManager templateDashboardWidgetResManager;

	private ChartWidgetSource chartWidgetSource;

	private HtmlRenderContextScriptObjectWriter htmlRenderContextScriptObjectWriter = new HtmlRenderContextScriptObjectWriter();

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = new HtmlChartPluginScriptObjectWriter();

	private HtmlTplDashboardScriptObjectWriter htmlTplDashboardScriptObjectWriter = new HtmlTplDashboardScriptObjectWriter();

	private AttributeValueHtmlChartPlugin htmlChartPluginForGetWidgetException = new AttributeValueHtmlChartPlugin(
			Global.PRODUCT_NAME_EN + "HtmlChartPluginForGetWidgetException",
			Global.PRODUCT_NAME_EN + "HtmlChartPluginForGetWidgetExceptionMsg");

	/** 导入项 */
	private HtmlTplDashboardImport htmlTplDashboardImport;

	/** 上下文路径占位符 */
	private String contextPathPlaceholder = DEFAULT_CONTEXT_PATH_PLACE_HOLDER;

	/** 应用版本号占位符 */
	private String versionPlaceholder = DEFAULT_VERSION_PLACE_HOLDER;

	/** 随机码占位符 */
	private String randomCodePlaceholder = DEFAULT_RANDOMCODE_VAR_PLACE_HOLDER;

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

	private ImportHtmlChartPluginVarNameResolver importHtmlChartPluginVarNameResolver;

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

	public AttributeValueHtmlChartPlugin getHtmlChartPluginForGetWidgetException()
	{
		return htmlChartPluginForGetWidgetException;
	}

	public void setHtmlChartPluginForGetWidgetException(
			AttributeValueHtmlChartPlugin htmlChartPluginForGetWidgetException)
	{
		this.htmlChartPluginForGetWidgetException = htmlChartPluginForGetWidgetException;
	}

	public HtmlTplDashboardImport getHtmlTplDashboardImport()
	{
		return htmlTplDashboardImport;
	}

	public void setHtmlTplDashboardImport(HtmlTplDashboardImport htmlTplDashboardImport)
	{
		this.htmlTplDashboardImport = htmlTplDashboardImport;
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

	public String getRandomCodePlaceholder()
	{
		return randomCodePlaceholder;
	}

	public void setRandomCodePlaceholder(String randomCodePlaceholder)
	{
		this.randomCodePlaceholder = randomCodePlaceholder;
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

	public ImportHtmlChartPluginVarNameResolver getImportHtmlChartPluginVarNameResolver()
	{
		return importHtmlChartPluginVarNameResolver;
	}

	public void setImportHtmlChartPluginVarNameResolver(
			ImportHtmlChartPluginVarNameResolver importHtmlChartPluginVarNameResolver)
	{
		this.importHtmlChartPluginVarNameResolver = importHtmlChartPluginVarNameResolver;
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
	 * 获取用于渲染指定图表部件ID的{@linkplain HtmlChartWidget}。
	 * <p>
	 * 注意：如果没有找到对应的{@linkplain HtmlChartWidget}、或者底层出现异常，此方法将返回一个不同ID（{@linkplain HtmlChartWidget#getId()}）的替代对象，
	 * 用于支持渲染相关错误信息。
	 * </p>
	 * 
	 * @param htmlChartWidgetId
	 * @return
	 */
	public HtmlChartWidget getHtmlChartWidget(String htmlChartWidgetId)
	{
		return getHtmlChartWidgetForRender(htmlChartWidgetId);
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
	public HtmlTplDashboard render(RenderContext renderContext, HtmlTplDashboardWidget dashboardWidget, String template)
			throws RenderException
	{
		HtmlTplDashboardRenderAttr renderAttr = getHtmlTplDashboardRenderAttrNonNull(renderContext);

		HtmlTplDashboard dashboard = createHtmlTplDashboard(renderContext, dashboardWidget, template);

		try
		{
			renderHtmlTplDashboard(renderContext, renderAttr, dashboard);
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

	protected HtmlTplDashboardRenderAttr getHtmlTplDashboardRenderAttrNonNull(RenderContext renderContext)
	{
		return HtmlTplDashboardRenderAttr.getNonNull(renderContext);
	}

	/**
	 * 读取指定{@linkplain HtmlTplDashboardWidget}的资源内容。
	 * <p>
	 * 如果资源不存在，将返回空字符串。
	 * </p>
	 * 
	 * @param dashboardWidget
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public String readResourceContent(HtmlTplDashboardWidget dashboardWidget, String name) throws IOException
	{
		Reader reader = getResourceReaderNonNull(dashboardWidget, name);

		return IOUtil.readString(reader, true);
	}

	/**
	 * 保存指定{@linkplain HtmlTplDashboardWidget}的资源内容。
	 * 
	 * @param dashboardWidget
	 * @param name
	 * @param content
	 * @throws IOException
	 */
	public void saveResourceContent(HtmlTplDashboardWidget dashboardWidget, String name, String content)
			throws IOException
	{
		Writer writer = null;

		try
		{
			writer = getResourceWriter(dashboardWidget, name);
			writer.write(content);
		}
		finally
		{
			IOUtil.close(writer);
		}
	}

	protected Reader getResourceReaderNonNull(HtmlTplDashboardWidget dashboardWidget, String name) throws IOException
	{
		TemplateDashboardWidgetResManager rm = getTemplateDashboardWidgetResManager();

		Reader reader = null;

		try
		{
			reader = rm.getReader(dashboardWidget, name);
		}
		catch (FileNotFoundException e)
		{
		}

		if (reader == null)
			reader = IOUtil.getReader("");

		return reader;
	}

	protected Writer getResourceWriter(HtmlTplDashboardWidget dashboardWidget, String template) throws IOException
	{
		return getTemplateDashboardWidgetResManager().getWriter(dashboardWidget, template);
	}

	/**
	 * 渲染{@linkplain HtmlTplDashboard}。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param dashboard
	 * @throws Throwable
	 */
	protected abstract void renderHtmlTplDashboard(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			HtmlTplDashboard dashboard) throws Throwable;

	/**
	 * 生成基本的模板内容。
	 * 
	 * @param htmlCharset
	 * @param chartWidgetId
	 * @return
	 */
	public abstract String simpleTemplateContent(String htmlCharset, String... chartWidgetId);

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
	 * @param id
	 * @return
	 */
	protected HtmlChartWidget getHtmlChartWidgetForRender(String id)
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

		return (HtmlChartWidget) chartWidget;
	}

	protected HtmlChartWidget createHtmlChartWidgetForGetException(String exceptionWidgetId, Throwable t)
	{
		HtmlChartWidget widget = new HtmlChartWidget(IDUtil.uuid(), "HtmlChartWidgetForWidgetException",
				ChartDefinition.EMPTY_CHART_DATA_SET, this.htmlChartPluginForGetWidgetException);

		widget.setAttribute(this.htmlChartPluginForGetWidgetException.getChartAttributeName(), "Chart widget '"
				+ (exceptionWidgetId == null ? "" : exceptionWidgetId) + "' exception : " + t.getMessage());

		return widget;
	}

	protected HtmlChartWidget createHtmlChartWidgetForNotFound(String notFoundWidgetId)
	{
		HtmlChartWidget widget = new HtmlChartWidget(IDUtil.uuid(), "HtmlChartWidgetForWidgetNotFound",
				ChartDefinition.EMPTY_CHART_DATA_SET, this.htmlChartPluginForGetWidgetException);

		widget.setAttribute(this.htmlChartPluginForGetWidgetException.getChartAttributeName(),
				"Chart widget '" + (notFoundWidgetId == null ? "" : notFoundWidgetId) + "' not found");

		return widget;
	}

	protected HtmlChartWidget createHtmlChartWidgetForPluginNull(ChartWidget chartWidget)
	{
		HtmlChartWidget widget = new HtmlChartWidget(IDUtil.uuid(), "HtmlChartWidgetForWidgetPluginNull",
				ChartDefinition.EMPTY_CHART_DATA_SET, this.htmlChartPluginForGetWidgetException);

		widget.setAttribute(this.htmlChartPluginForGetWidgetException.getChartAttributeName(), "Chart plugin is null");

		return widget;
	}

	/**
	 * 写{@linkplain HtmlTplDashboard} JS变量：
	 * <p>
	 * <code>
	 * <pre>
	 * var [tmpRenderContextVarName] = {};
	 * var [dashboardVarName] = {..., renderContext: [tmpRenderContextVarName], ...};
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param out
	 * @param dashboard
	 * @param tmpRenderContextVarName
	 * @throws IOException
	 */
	protected void writeHtmlTplDashboardJSVar(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			Writer out, HtmlTplDashboard dashboard, String tmpRenderContextVarName) throws IOException
	{
		if (StringUtil.isEmpty(dashboard.getVarName()))
			throw new IllegalArgumentException();

		getHtmlRenderContextScriptObjectWriter().writeNoAttributes(out, renderContext, tmpRenderContextVarName);
		getHtmlTplDashboardScriptObjectWriter().write(out, dashboard, tmpRenderContextVarName);
	}

	/**
	 * 写{@linkplain HtmlTplDashboard} JS初始化代码：
	 * <p>
	 * <code>
	 * <pre>
	 * var [tmpRenderContext] = { attributes: {...} };
	 * dashboard.renderContext.attributes = [tmpRenderContext].attributes;
	 * ...
	 * dashboard.charts.push(...);
	 * ...
	 * };
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param out
	 * @param dashboard
	 * @param tmpRenderContextVarName
	 * @throws IOException
	 */
	protected void writeHtmlTplDashboardJSInit(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			Writer out, HtmlTplDashboard dashboard, String tmpRenderContextVarName) throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();

		getHtmlRenderContextScriptObjectWriter().write(out, renderContext, tmpRenderContextVarName,
				getHtmlRenderContextIgnoreAttrs(renderContext, renderAttr, out, dashboard));
		out.write(varName + "." + Dashboard.PROPERTY_RENDER_CONTEXT + "." + RenderContext.PROPERTY_ATTRIBUTES + " = "
				+ tmpRenderContextVarName + "." + RenderContext.PROPERTY_ATTRIBUTES + ";");
		writeNewLine(out);

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

	protected Collection<String> getHtmlRenderContextIgnoreAttrs(RenderContext renderContext,
			HtmlTplDashboardRenderAttr renderAttr, Writer out, HtmlTplDashboard dashboard)
	{
		Collection<String> ignores = renderAttr.getIgnoreRenderAttrs(renderContext);
		return (ignores != null ? ignores : Arrays.asList(renderAttr.getHtmlWriterName()));
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
	 * @param renderContext
	 * @param renderAttr
	 * @param out
	 * @param dashboard
	 * @param dashboardFactoryVar
	 *            如果为{@code null}，则使用{@linkplain #getDefaultDashboardFactoryVar()}
	 * @throws IOException
	 */
	protected void writeHtmlTplDashboardJSFactoryInit(RenderContext renderContext,
			HtmlTplDashboardRenderAttr renderAttr, Writer out, HtmlTplDashboard dashboard, String dashboardFactoryVar)
			throws IOException
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
	 * [dashboard].render();
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param out
	 * @param dashboard
	 * @throws IOException
	 */
	protected void writeHtmlTplDashboardJSRender(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			Writer out, HtmlTplDashboard dashboard) throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();

		out.write(varName + "." + this.dashboardRenderFuncName + "();");
		writeNewLine(out);
	}

	/**
	 * 写看板导入项。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param out
	 * @param dashboard
	 * @param importExclude
	 * @throws IOException
	 */
	protected void writeDashboardImport(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr, Writer out,
			HtmlTplDashboard dashboard, String importExclude) throws IOException
	{
		WebContext webContext = renderAttr.getWebContext(renderContext);

		List<String> excludes = StringUtil.splitWithTrim(importExclude, ",");

		if (this.htmlTplDashboardImport != null)
		{
			List<ImportItem> importItems = this.htmlTplDashboardImport.getImportItems();

			if (importItems != null)
			{
				String randomCode = genRandomCode();

				for (ImportItem impt : importItems)
				{
					String name = impt.getName();

					if (excludes.contains(name))
						continue;

					String content = replaceContextPathPlaceholder(impt.getContent(), webContext.getContextPath());
					content = replaceVersionPlaceholder(content, Global.VERSION);
					content = replaceRandomCodePlaceholder(content, randomCode);

					writeNewLine(out);
					out.write(content);
				}
			}
		}

		if (!excludes.contains(this.themeImportName))
		{
			writeNewLine(out);
			writeDashboardThemeStyle(renderContext, renderAttr, out, dashboard);
		}
	}

	/**
	 * 写{@linkplain HtmlChartPlugin} JS脚本，并返回对应的变量名列表。
	 * <p>
	 * 如果{@linkplain #getImportHtmlChartPluginVarNameResolver()}不为{@code null}，此方法不会写{@linkplain HtmlChartPlugin}
	 * JS脚本，而仅返回导入变量名列表。
	 * </p>
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param out
	 * @param htmlChartWidgets
	 * @return
	 * @throws IOException
	 */
	protected List<String> writeHtmlChartPluginScriptsResolveImport(RenderContext renderContext,
			HtmlTplDashboardRenderAttr renderAttr, Writer out, List<HtmlChartWidget> htmlChartWidgets)
			throws IOException
	{
		if (this.importHtmlChartPluginVarNameResolver == null)
			return writeHtmlChartPluginScripts(renderContext, renderAttr, out, htmlChartWidgets);

		List<String> pluginVarNames = new ArrayList<>(htmlChartWidgets.size());

		for (HtmlChartWidget widget : htmlChartWidgets)
		{
			String importVarName = this.importHtmlChartPluginVarNameResolver.resolve(widget);
			pluginVarNames.add(importVarName);
		}

		return pluginVarNames;
	}

	/**
	 * 写{@linkplain HtmlChartPlugin} JS脚本，并返回对应的变量名列表。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param out
	 * @param htmlChartWidgets
	 * @return
	 * @throws IOException
	 */
	protected List<String> writeHtmlChartPluginScripts(RenderContext renderContext,
			HtmlTplDashboardRenderAttr renderAttr, Writer out, List<HtmlChartWidget> htmlChartWidgets)
			throws IOException
	{
		List<String> pluginVarNames = new ArrayList<>(htmlChartWidgets.size());

		for (int i = 0; i < htmlChartWidgets.size(); i++)
		{
			String pluginVarName = null;

			HtmlChartWidget widget = htmlChartWidgets.get(i);
			HtmlChartPlugin plugin = widget.getPlugin();

			for (int j = 0; j < i; j++)
			{
				HtmlChartWidget myWidget = htmlChartWidgets.get(j);
				HtmlChartPlugin myPlugin = myWidget.getPlugin();

				if (myPlugin.getId().equals(plugin.getId()))
				{
					pluginVarName = pluginVarNames.get(j);
					break;
				}
			}

			if (pluginVarName == null)
			{
				pluginVarName = renderAttr.genChartPluginVarName(Integer.toString(i));
				getHtmlChartPluginScriptObjectWriter().write(out, plugin, pluginVarName);
			}

			pluginVarNames.add(pluginVarName);
		}

		return pluginVarNames;
	}

	/**
	 * 写{@linkplain HtmlChart}。
	 *
	 * @param renderContext
	 * @param chartWidget
	 * @return
	 */
	protected HtmlChart writeHtmlChart(RenderContext renderContext, HtmlChartWidget chartWidget) throws RenderException
	{
		return chartWidget.render(renderContext);
	}

	/**
	 * 写看板主题样式。
	 * 
	 * @param renderContext
	 * @param out
	 * @param dashboard
	 * @return
	 * @throws IOException
	 */
	protected boolean writeDashboardThemeStyle(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr,
			Writer out, HtmlTplDashboard dashboard) throws IOException
	{
		DashboardTheme dashboardTheme = renderAttr.getDashboardThemeNonNull(renderContext);

		out.write("<style type='text/css' " + DASHBOARD_IMPORT_ITEM_NAME_ATTR + "='" + this.themeImportName + "'>");
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

			if (theme.hasBorderColor())
			{
				writeNewLine(out);
				out.write("  border-color: " + theme.getBorderColor() + ";");
			}

			if (theme.hasBorderWidth())
			{
				writeNewLine(out);
				out.write("  border-width: " + borderWidth + ";");
				writeNewLine(out);
				out.write("  border-style: solid;");
			}

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
	 * 替换字符串中的随机码占位符为真实的随机码。
	 * 
	 * @param str
	 * @param randomCode
	 * @return
	 */
	protected String replaceRandomCodePlaceholder(String str, String randomCode)
	{
		if (StringUtil.isEmpty(str))
			return str;

		if (randomCode == null)
			randomCode = "";

		return str.replace(getRandomCodePlaceholder(), randomCode);
	}

	/**
	 * 生成一个随机码。
	 * 
	 * @return
	 */
	protected String genRandomCode()
	{
		return "rc" + Long.toHexString(System.currentTimeMillis());
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
	protected HtmlTplDashboard createHtmlTplDashboard(RenderContext renderContext,
			HtmlTplDashboardWidget dashboardWidget, String template)
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
		 * @param title
		 * @return
		 */
		String handle(String title);
	}

	/**
	 * 添加扩展内容的{@linkplain HtmlTitleHandler}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ExtContentHtmlTitleHandler implements HtmlTitleHandler
	{
		/** 扩展内容 */
		private String content = "";

		/** 当标题为空时的扩展内容 */
		private String contentForEmpty = null;

		/** 是否将内容添加为标题前缀而非后缀 */
		private boolean prefix = false;

		public ExtContentHtmlTitleHandler()
		{
			super();
		}

		public ExtContentHtmlTitleHandler(String content)
		{
			super();
			this.content = content;
		}

		public String getContent()
		{
			return content;
		}

		public void setContent(String content)
		{
			this.content = content;
		}

		public String getContentForEmpty()
		{
			return contentForEmpty;
		}

		public void setContentForEmpty(String contentForEmpty)
		{
			this.contentForEmpty = contentForEmpty;
		}

		public boolean isPrefix()
		{
			return prefix;
		}

		public void setPrefix(boolean prefix)
		{
			this.prefix = prefix;
		}

		@Override
		public String handle(String title)
		{
			String content = this.content;

			if (StringUtil.isEmpty(title) && this.contentForEmpty != null)
				content = this.contentForEmpty;

			return (prefix ? content + title : title + content);
		}
	}

	/**
	 * 引入{@linkplain HtmlChartPlugin}变量名处理器。
	 * <p>
	 * 为了避免每次渲染{@linkplain HtmlTplDashboard}时都内联渲染{@linkplain HtmlChartPlugin}对象，
	 * 通常会将所有{@linkplain HtmlChartPlugin}对象独立渲染，而在渲染{@linkplain HtmlTplDashboard}时仅引入，
	 * 此类即为此提供支持，用于获取引入{@linkplain HtmlChartPlugin}对象的变量名。
	 * </p>
	 * <p>
	 * 注意：{@linkplain HtmlTplDashboardWidgetRenderer#getHtmlChartPluginForGetWidgetException()}
	 * 也应该加入独立渲染的{@linkplain HtmlChartPlugin}集合。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface ImportHtmlChartPluginVarNameResolver
	{
		/**
		 * 获取引入变量名。
		 * <p>
		 * 例如：<code>"chartPluginManager.get('...')"</code>
		 * </p>
		 * 
		 * @param chartWidget
		 * @return
		 */
		String resolve(HtmlChartWidget chartWidget);
	}

	public static class TemplateImportHtmlChartPluginVarNameResolver implements ImportHtmlChartPluginVarNameResolver
	{
		public static final String PLACEHOLDER_CHART_PLUGIN_ID = "$CHART_PLUGIN_ID";

		private String template;

		public TemplateImportHtmlChartPluginVarNameResolver()
		{
			super();
		}

		public TemplateImportHtmlChartPluginVarNameResolver(String template)
		{
			super();
			this.template = template;
		}

		public String getTemplate()
		{
			return template;
		}

		public void setTemplate(String template)
		{
			this.template = template;
		}

		@Override
		public String resolve(HtmlChartWidget chartWidget)
		{
			String chartPluginId = chartWidget.getPlugin().getId();
			return this.template.replace(PLACEHOLDER_CHART_PLUGIN_ID, chartPluginId);
		}
	}
}
