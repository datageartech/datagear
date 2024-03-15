/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.Theme;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.html.CharsetFilterHandler;
import org.datagear.util.html.HtmlFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 
 * @author datagear@163.com
 * 
 */
public abstract class HtmlTplDashboardWidgetRenderer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlTplDashboardWidgetRenderer.class);
	
	public static final String TAG_NAME_SCRIPT="script";

	public static final String DASHBOARD_ELEMENT_ATTR_PREFIX = "dg-";

	public static final String DASHBOARD_IMPORT_ITEM_NAME_ATTR = DASHBOARD_ELEMENT_ATTR_PREFIX + "import-name";

	public static final String DEFAULT_DASHBOARD_FACTORY_VAR = "dashboardFactory";

	public static final String DEFAULT_THEME_IMPORT_NAME = "dashboardThemeStyle";

	public static final String DEFAULT_DASHBOARD_STYLE_NAME = DASHBOARD_ELEMENT_ATTR_PREFIX + "dashboard";

	public static final String DEFAULT_CHART_STYLE_NAME = DASHBOARD_ELEMENT_ATTR_PREFIX + "chart";

	public static final String DEFAULT_DASHBOARD_VAR = "dashboard";

	private ChartWidgetSource chartWidgetSource;

	private HtmlRenderContextScriptObjectWriter htmlRenderContextScriptObjectWriter = new HtmlRenderContextScriptObjectWriter();

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = new HtmlChartPluginScriptObjectWriter();

	private HtmlTplDashboardScriptObjectWriter htmlTplDashboardScriptObjectWriter = new HtmlTplDashboardScriptObjectWriter();

	private AttributeValueHtmlChartPlugin htmlChartPluginForGetWidgetException = new AttributeValueHtmlChartPlugin(
			Global.PRODUCT_NAME_EN_LC + "HtmlChartPluginForGetWidgetException",
			ChartDefinition.BUILTIN_ATTR_PREFIX + "EXCEPTION_MESSAGE", HtmlChartPluginScriptObjectWriter.INSTANCE,
			HtmlRenderContextScriptObjectWriter.INSTANCE, HtmlChartScriptObjectWriter.INSTANCE);

	/** 默认JS看板工厂变量名 */
	private String defaultDashboardFactoryVar = DEFAULT_DASHBOARD_FACTORY_VAR;

	/** JS看板工厂初始化函数名 */
	private String dashboardFactoryInitFuncName = "init";

	/** 看板对象初始化函数名 */
	private String dashboardInitFuncName = "init";

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

	private ImportHtmlChartPluginVarNameResolver importHtmlChartPluginVarNameResolver = null;

	/** 换行符 */
	private String newLine = HtmlChartPlugin.HTML_NEW_LINE;

	private HtmlFilter htmlFilter = new HtmlFilter();

	private String htmlChartWidgetIdForGetException = IDUtil.uuid();
	
	private String HtmlChartWidgetIdForNotFound = IDUtil.uuid();
	
	private String HtmlChartWidgetIdForPluginNull = IDUtil.uuid();

	public HtmlTplDashboardWidgetRenderer()
	{
		super();
	}

	public HtmlTplDashboardWidgetRenderer(ChartWidgetSource chartWidgetSource)
	{
		super();
		this.chartWidgetSource = chartWidgetSource;
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

	public String getDashboardInitFuncName()
	{
		return dashboardInitFuncName;
	}

	public void setDashboardInitFuncName(String dashboardInitFuncName)
	{
		this.dashboardInitFuncName = dashboardInitFuncName;
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

	public HtmlFilter getHtmlFilter()
	{
		return htmlFilter;
	}

	public void setHtmlFilter(HtmlFilter htmlFilter)
	{
		this.htmlFilter = htmlFilter;
	}

	public String getHtmlChartWidgetIdForGetException()
	{
		return htmlChartWidgetIdForGetException;
	}

	public void setHtmlChartWidgetIdForGetException(String htmlChartWidgetIdForGetException)
	{
		this.htmlChartWidgetIdForGetException = htmlChartWidgetIdForGetException;
	}

	public String getHtmlChartWidgetIdForNotFound()
	{
		return HtmlChartWidgetIdForNotFound;
	}

	public void setHtmlChartWidgetIdForNotFound(String htmlChartWidgetIdForNotFound)
	{
		HtmlChartWidgetIdForNotFound = htmlChartWidgetIdForNotFound;
	}

	public String getHtmlChartWidgetIdForPluginNull()
	{
		return HtmlChartWidgetIdForPluginNull;
	}

	public void setHtmlChartWidgetIdForPluginNull(String htmlChartWidgetIdForPluginNull)
	{
		HtmlChartWidgetIdForPluginNull = htmlChartWidgetIdForPluginNull;
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
		CharsetFilterHandler tl = new CharsetFilterHandler();
		this.htmlFilter.filter(in, tl);

		return tl.getCharset();
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
	 * 获取与指定图表部件ID相关异常的{@linkplain HtmlChartWidget}。
	 * <p>
	 * 此方法将返回一个不同ID（{@linkplain HtmlChartWidget#getId()}）的替代对象，用于支持渲染相关错误信息。
	 * </p>
	 * 
	 * @param htmlChartWidgetId
	 * @param t
	 * @return
	 */
	public HtmlChartWidget getHtmlChartWidgetForException(String htmlChartWidgetId, Throwable t)
	{
		return createHtmlChartWidgetForGetException(htmlChartWidgetId, t);
	}
	
	/**
	 * 渲染{@linkplain HtmlTplDashboard}。
	 * 
	 * @param dashboardWidget
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	public abstract HtmlTplDashboard render(HtmlTplDashboardWidget dashboardWidget, HtmlTplDashboardRenderContext renderContext) throws RenderException;
	
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
		HtmlChartWidget widget = new HtmlChartWidget(this.htmlChartWidgetIdForGetException, "HtmlChartWidgetForWidgetException",
				ChartDefinition.EMPTY_CHART_DATA_SET, this.htmlChartPluginForGetWidgetException);

		widget.setAttrValue(this.htmlChartPluginForGetWidgetException.getAttrName(), "Chart widget '"
				+ (exceptionWidgetId == null ? "" : exceptionWidgetId) + "' exception : " + t.getMessage());

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Create placeholder chart widget [" + widget.getId() + "] for [" + exceptionWidgetId
					+ "] on exception", t);

		return widget;
	}

	protected HtmlChartWidget createHtmlChartWidgetForNotFound(String notFoundWidgetId)
	{
		HtmlChartWidget widget = new HtmlChartWidget(this.HtmlChartWidgetIdForNotFound, "HtmlChartWidgetForWidgetNotFound",
				ChartDefinition.EMPTY_CHART_DATA_SET, this.htmlChartPluginForGetWidgetException);

		widget.setAttrValue(this.htmlChartPluginForGetWidgetException.getAttrName(),
				"Chart widget '" + (notFoundWidgetId == null ? "" : notFoundWidgetId) + "' not found");

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Create placeholder chart widget [" + widget.getId() + "] for [" + notFoundWidgetId
					+ "] on exception : not found");

		return widget;
	}

	protected HtmlChartWidget createHtmlChartWidgetForPluginNull(ChartWidget chartWidget)
	{
		HtmlChartWidget widget = new HtmlChartWidget(this.HtmlChartWidgetIdForPluginNull, "HtmlChartWidgetForWidgetPluginNull",
				ChartDefinition.EMPTY_CHART_DATA_SET, this.htmlChartPluginForGetWidgetException);

		widget.setAttrValue(this.htmlChartPluginForGetWidgetException.getAttrName(), "Chart plugin is null");

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Create placeholder chart widget [" + widget.getId() + "] for [" + chartWidget.getId()
					+ "] on exception : null chart plugin");

		return widget;
	}
	
	/**
	 * 生成一个全局唯一的看板ID。
	 * 
	 * @return
	 */
	protected String nextDashboardId()
	{
		return IDUtil.uuid();
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
	 * @param dashboard
	 * @param tmpRenderContextVarName
	 * @throws IOException
	 */
	protected void writeDashboardJsVar(HtmlTplDashboardRenderContext renderContext,
			HtmlTplDashboard dashboard, String tmpRenderContextVarName) throws IOException
	{
		if (StringUtil.isEmpty(dashboard.getVarName()))
			throw new IllegalArgumentException();
		
		Writer out = renderContext.getWriter();
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
	 * @param dashboard
	 * @param tmpRenderContextVarName
	 * @throws IOException
	 */
	protected void writeDashboardJsInit(HtmlTplDashboardRenderContext renderContext,
			HtmlTplDashboard dashboard, String tmpRenderContextVarName) throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();
		
		Writer out = renderContext.getWriter();
		
		getHtmlRenderContextScriptObjectWriter().write(out, renderContext, tmpRenderContextVarName);
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
	 * @param dashboard
	 * @param dashboardFactoryVar
	 *            如果为{@code null}，则使用{@linkplain #getDefaultDashboardFactoryVar()}
	 * @throws IOException
	 */
	protected void writeDashboardJsFactoryInit(HtmlTplDashboardRenderContext renderContext, HtmlTplDashboard dashboard, String dashboardFactoryVar)
			throws IOException
	{
		String varName = dashboard.getVarName();
		dashboardFactoryVar = getDashboardFactoryVarElseDft(dashboardFactoryVar);
		
		if (StringUtil.isEmpty(varName) || StringUtil.isEmpty(dashboardFactoryVar))
			throw new IllegalArgumentException();
		
		Writer out = renderContext.getWriter();
		
		out.write(dashboardFactoryVar + "." + this.dashboardFactoryInitFuncName + "(" + varName + ");");
		writeNewLine(out);
	}
	
	protected String getDashboardFactoryVarElseDft(String dashboardFactoryVar)
	{
		if (StringUtil.isEmpty(dashboardFactoryVar))
			dashboardFactoryVar = this.defaultDashboardFactoryVar;

		return dashboardFactoryVar;
	}

	/**
	 * 写{@linkplain HtmlTplDashboard} JS初始化代码：
	 * <p>
	 * <code>
	 * <pre>
	 * [dashboard].init();
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @throws IOException
	 */
	protected void writeDashboardJsInit(HtmlTplDashboardRenderContext renderContext, HtmlTplDashboard dashboard) throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();
		
		Writer out = renderContext.getWriter();
		
		out.write(varName + "." + this.dashboardInitFuncName + "();");
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
	 * @param dashboard
	 * @throws IOException
	 */
	protected void writeDashboardJsRender(HtmlTplDashboardRenderContext renderContext, HtmlTplDashboard dashboard) throws IOException
	{
		String varName = dashboard.getVarName();

		if (StringUtil.isEmpty(varName))
			throw new IllegalArgumentException();
		
		Writer out = renderContext.getWriter();
		
		out.write(varName + "." + this.dashboardRenderFuncName + "();");
		writeNewLine(out);
	}

	/**
	 * 写{@linkplain HtmlTplDashboard}导入项。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @param unimportStr 导入排除项字符串，以{@code ','}分隔导入排除项
	 * @throws IOException
	 */
	protected void writeDashboardImport(HtmlTplDashboardRenderContext renderContext,
			HtmlTplDashboard dashboard, String unimportStr) throws IOException
	{
		Writer out = renderContext.getWriter();
		List<String> unimports = StringUtil.splitWithTrim(unimportStr, ",");
		
		// 后台生成的样式应该放在最开头，确保页面生成的、用户自定义的css有更高优先级
		if (!unimports.contains(this.themeImportName))
		{
			writeNewLine(out);
			writeDashboardThemeStyle(renderContext, dashboard);
		}

		List<HtmlTplDashboardImport> importList = renderContext.getImportList();

		if (importList != null)
		{
			for (HtmlTplDashboardImport impt : importList)
			{
				String name = impt.getName();

				if (unimports.contains(name))
					continue;

				String content = impt.getContent();

				writeNewLine(out);
				out.write(content);
			}
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
	 * @param htmlChartWidgets
	 * @return
	 * @throws IOException
	 */
	protected List<String> writeChartPluginScriptsResolveImport(HtmlTplDashboardRenderContext renderContext, List<HtmlChartWidget> htmlChartWidgets)
			throws IOException
	{
		if (this.importHtmlChartPluginVarNameResolver == null)
			return writeChartPluginScripts(renderContext, htmlChartWidgets);

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
	 * @param htmlChartWidgets
	 * @return
	 * @throws IOException
	 */
	protected List<String> writeChartPluginScripts(HtmlTplDashboardRenderContext renderContext, List<HtmlChartWidget> htmlChartWidgets)
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
				Writer out = renderContext.getWriter();
				pluginVarName = renderContext.varNameOfChartPlugin(Integer.toString(i));
				getHtmlChartPluginScriptObjectWriter().write(out, plugin, pluginVarName, renderContext.getLocale());
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
	 * @param chartId
	 * @return
	 * @throws RenderException
	 */
	protected HtmlChart writeChart(RenderContext renderContext, HtmlChartWidget chartWidget, String chartId) throws RenderException
	{
		return chartWidget.render(renderContext, chartId);
	}

	/**
	 * 写看板主题样式。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @return
	 * @throws IOException
	 */
	protected boolean writeDashboardThemeStyle(HtmlTplDashboardRenderContext renderContext, HtmlTplDashboard dashboard) throws IOException
	{
		Writer out = renderContext.getWriter();
		DashboardTheme dashboardTheme = renderContext.getDashboardTheme();

		if (dashboardTheme == null)
			return false;

		out.write(
				"<style type=\"text/css\" " + DASHBOARD_IMPORT_ITEM_NAME_ATTR + "=\"" + this.themeImportName + "\">");
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
	 * 写脚本开始标签。
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void writeScriptStartTag(Writer out) throws IOException
	{
		out.write("<"+TAG_NAME_SCRIPT+" type=\"text/javascript\">");
	}

	/**
	 * 写脚本结束标签。
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void writeScriptEndTag(Writer out) throws IOException
	{
		out.write("</"+TAG_NAME_SCRIPT+">");
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
	 * 创建{@linkplain HtmlTplDashboard}实例。
	 * 
	 * @param dashboardWidget
	 * @param renderContext
	 * @param dashboardId
	 * @param template
	 * @return
	 */
	protected static HtmlTplDashboard createDashboard(HtmlTplDashboardWidget dashboardWidget, RenderContext renderContext,
			String dashboardId, String template)
	{
		HtmlTplDashboard dashboard = new HtmlTplDashboard();

		dashboard.setId(dashboardId);
		dashboard.setTemplate(template);
		dashboard.setWidget(dashboardWidget);
		dashboard.setRenderContext(renderContext);
		dashboard.setCharts(new ArrayList<Chart>());

		return dashboard;
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
		 * 获取引入变量名，可以是任意JS字符串。
		 * <p>
		 * 例如：
		 * </p>
		 * <p>
		 * <code>
		 * "chartPluginManager.get(\"...\")"
		 * </code>
		 * </p>
		 * <p>
		 * <code>
		 * "{\"id\": \"...\"}"
		 * </code>
		 * </p>
		 * 
		 * @param chartWidget
		 * @return
		 */
		String resolve(HtmlChartWidget chartWidget);
	}

	/**
	 * 返回插件ID JSON字符串的{@linkplain ImportHtmlChartPluginVarNameResolver}。
	 * <p>
	 * 返回格式：
	 * </p>
	 * <p>
	 * <code>
	 * "{\"id\": \"...\"}"
	 * </code>
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class IdJsonImportHtmlChartPluginVarNameResolver implements ImportHtmlChartPluginVarNameResolver
	{
		@Override
		public String resolve(HtmlChartWidget chartWidget)
		{
			String pluginId = chartWidget.getPlugin().getId();
			return "{\"" + ChartPlugin.PROPERTY_ID + "\":" + StringUtil.toJavaScriptString(pluginId) + "}";
		}
	}
}
