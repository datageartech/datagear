/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datagear.analysis.Chart;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.util.CacheService;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.html.CopyWriter;
import org.datagear.util.html.DefaultFilterHandler;
import org.datagear.util.html.HeadBodyAwareFilterHandler;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * 使用原生HTML网页作为模板的{@linkplain HtmlTplDashboardWidget}渲染器。
 * <p>
 * 支持的模板格式如下：
 * </p>
 * <code>
 * <pre>
 * ...
 * &lt;html
 *     dg-dashboard-factory="..."
 *     dg-dashboard-var="..."
 *     dg-dashboard-unimport="..."
 *     dg-loadable-chart-widgets="..."
 *     dg-dashboard-code="..."
 *     dg-dashboard-auto-render="..."（已废弃）
 *     &gt;
 * ...
 * &lt;head&gt;
 * ...
 * &lt;/head&gt;
 * ...
 * &lt;body&gt;
 *     ...
 *     &lt;div id="..." dg-chart-widget="..."&gt;&lt;/div&gt;
 *     ...
 *     &lt;script dg-dashboard-code="..."&gt;&lt;/script&gt;
 *     ...
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
 * <code>html dg-dashboard-unimport</code>：选填，定义看板网页不加载的内置库（{@linkplain HtmlTplDashboardRenderContext#getImportList()}），多个以“,”隔开
 * </p>
 * <p>
 * <code>html dg-loadable-charts</code>：选填，定义看板网页允许在页面端通过JS异步加载的{@linkplain ChartWidget}模式（{@linkplain LoadableChartWidgets}），多个以“,”隔开
 * </p>
 * <p>
 * <code>html dg-dashboard-code</code>：选填，自定义看板脚本写入内容，可选值参考下面的<code>&lt;script dg-dashboard-code="..."&gt;&lt;/script&gt;</code>
 * </p>
 * <p>
 * <code>html dg-dashboard-auto-render</code>：已在4.4.0版本废弃，选填，定义看板网页是否自动执行渲染函数，可选值：{@code "true"}
 * 是；{@code "false"} 否。默认值为：{@code "true"}
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
 * {@code "instance"} ：不仅写入调用看板初始化函数的代码，不写入调用看板渲染函数的代码； <br>
 * {@code "init"} ：仅写入调用看板初始化函数的代码，不写入调用看板渲染函数的代码； <br>
 * {@code "render"} ：写入调用看板初始化函数的代码，写入调用看板渲染函数的代码； <br>
 * {@code 其他}
 * ：由<code>&lt;html&gt;</code>上的<code>dg-dashboard-code</code>、<code>dg-dashboard-auto-render</code>决定。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class HtmlTplDashboardWidgetHtmlRenderer extends HtmlTplDashboardWidgetRenderer
{
	public static final String DEFAULT_CHART_TAG_NAME = "div";

	public static final String HTML_TAG_TITLE_START = "<title>";

	public static final String HTML_TAG_TITLE_CLOSE = "</title>";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_VAR = DASHBOARD_ELEMENT_ATTR_PREFIX + "dashboard-var";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_FACTORY = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "dashboard-factory";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_UNIMPORT = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "dashboard-unimport";

	public static final String DEFAULT_ATTR_NAME_LOADABLE_CHART_WIDGETS = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "loadable-chart-widgets";

	/**
	 * @deprecated {@code dg-dashboard-auto-render}特性已在4.4.0版本废弃，后续版本将移除
	 */
	@Deprecated
	public static final String DEFAULT_ATTR_NAME_DASHBOARD_AUTO_RENDER = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "dashboard-auto-render";

	public static final String DEFAULT_ATTR_NAME_DASHBOARD_CODE = DASHBOARD_ELEMENT_ATTR_PREFIX
			+ "dashboard-code";

	public static final String DEFAULT_ATTR_NAME_CHART_WIDGET = DASHBOARD_ELEMENT_ATTR_PREFIX + "chart-widget";

	public static final String ATTR_NAME_CHART_AUTO_RESIZE = DASHBOARD_ELEMENT_ATTR_PREFIX + "chart-auto-resize";

	public static final String DASHBOARD_CODE_ATTR_VALUE_INSTANCE = "instance";
	
	public static final String DASHBOARD_CODE_ATTR_VALUE_INIT = "init";

	public static final String DASHBOARD_CODE_ATTR_VALUE_RENDER = "render";
	
	/** 属性名：看板JS变量名 */
	private String attrNameDashboardVar = DEFAULT_ATTR_NAME_DASHBOARD_VAR;

	/** 属性名：看板工厂JS变量名 */
	private String attrNameDashboardFactory = DEFAULT_ATTR_NAME_DASHBOARD_FACTORY;

	/** 属性名：看板导入排除项 */
	private String attrNameDashboardUnimport = DEFAULT_ATTR_NAME_DASHBOARD_UNIMPORT;

	/** 属性名：异步加载图表部件模式 */
	private String attrNameLoadableChartWidgets = DEFAULT_ATTR_NAME_LOADABLE_CHART_WIDGETS;

	/**
	 * 属性名：是否自动执行看板渲染函数
	 * 
	 * @deprecated {@code dg-dashboard-auto-render}特性已在4.4.0版本废弃，后续版本将移除
	 */
	@Deprecated
	private String attrNameDashboardAutoRender = DEFAULT_ATTR_NAME_DASHBOARD_AUTO_RENDER;

	/** 属性名：写入看板脚本标识属性 */
	private String attrNameDashboardCode = DEFAULT_ATTR_NAME_DASHBOARD_CODE;

	/** 图表标签名 */
	private String chartTagName = DEFAULT_CHART_TAG_NAME;

	/** 属性名：图表部件ID */
	private String attrNameChartWidget = DEFAULT_ATTR_NAME_CHART_WIDGET;

	/**全局JS对象（通常是：window）的局部变量名*/
	private String localGlobalVarName = Global.PRODUCT_NAME_EN_LC + "Global" + IDUtil.toStringOfMaxRadix();
	
	private CacheService cacheService = null;

	public HtmlTplDashboardWidgetHtmlRenderer()
	{
		super();
	}

	public HtmlTplDashboardWidgetHtmlRenderer(ChartWidgetSource chartWidgetSource)
	{
		super(chartWidgetSource);
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

	public String getAttrNameDashboardUnimport()
	{
		return attrNameDashboardUnimport;
	}

	public void setAttrNameDashboardUnimport(String attrNameDashboardUnimport)
	{
		this.attrNameDashboardUnimport = attrNameDashboardUnimport;
	}

	public String getAttrNameLoadableChartWidgets()
	{
		return attrNameLoadableChartWidgets;
	}

	public void setAttrNameLoadableChartWidgets(String attrNameLoadableChartWidgets)
	{
		this.attrNameLoadableChartWidgets = attrNameLoadableChartWidgets;
	}

	/**
	 * @deprecated {@code dg-dashboard-auto-render}特性已在4.4.0版本废弃，后续版本将移除
	 * @return
	 */
	@Deprecated
	public String getAttrNameDashboardAutoRender()
	{
		return attrNameDashboardAutoRender;
	}

	/**
	 * @deprecated {@code dg-dashboard-auto-render}特性已在4.4.0版本废弃，后续版本将移除
	 * @param attrNameDashboardAutoRender
	 */
	@Deprecated
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

	public String getLocalGlobalVarName()
	{
		return localGlobalVarName;
	}

	public void setLocalGlobalVarName(String localGlobalVarName)
	{
		this.localGlobalVarName = localGlobalVarName;
	}

	public CacheService getCacheService()
	{
		return cacheService;
	}

	public void setCacheService(CacheService cacheService)
	{
		this.cacheService = cacheService;
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
	public HtmlTplDashboard render(HtmlTplDashboardWidget dashboardWidget, HtmlTplDashboardRenderContext renderContext)
			throws RenderException
	{
		if (!renderContext.hasTemplateReader())
			throw new IllegalArgumentException("[renderContext.templateReader] required");

		TplDashboardMeta dashboardMeta = getTplDashboardMetaCache(dashboardWidget, renderContext);

		try
		{
			DashboardFilterContext context = null;

			if (dashboardMeta != null)
			{
				context = doRenderDashboard(dashboardWidget, renderContext, dashboardMeta);
			}
			else
			{
				context = doRenderDashboard(dashboardWidget, renderContext);
				setTplDashboardMetaCache(dashboardWidget, renderContext, context.getDashboardMeta());
			}

			return context.getDashboard();
		}
		catch(IOException e)
		{
			throw new RenderException(e);
		}
	}

	protected TplDashboardMeta getTplDashboardMetaCache(HtmlTplDashboardWidget dashboardWidget,
			HtmlTplDashboardRenderContext renderContext)
	{
		if (this.cacheService == null)
			return null;

		// 没有上次修改时间的不应返回缓存
		if (!renderContext.hasTemplateLastModified())
			return null;

		TplDashboardMetaCacheKey key = new TplDashboardMetaCacheKey(dashboardWidget.getId(),
				renderContext.getTemplate());
		ValueWrapper valueWrapper = this.cacheService.get(key);
		TplDashboardMetaCacheValue value = (valueWrapper == null ? null
				: (TplDashboardMetaCacheValue) valueWrapper.get());

		if (value == null || value.getTemplateLastModified() != renderContext.getTemplateLastModified())
			return null;

		return value.getDashboardMeta();
	}

	protected boolean setTplDashboardMetaCache(HtmlTplDashboardWidget dashboardWidget,
			HtmlTplDashboardRenderContext renderContext, TplDashboardMeta dashboardMeta)
	{
		if (this.cacheService == null)
			return false;

		// 没有上次修改时间的不应设置缓存
		if (!renderContext.hasTemplateLastModified())
			return false;

		TplDashboardMetaCacheKey key = new TplDashboardMetaCacheKey(dashboardWidget.getId(),
				renderContext.getTemplate());
		TplDashboardMetaCacheValue value = new TplDashboardMetaCacheValue(dashboardMeta,
				renderContext.getTemplateLastModified());

		this.cacheService.put(key, value);

		return true;
	}

	protected DashboardFilterContext doRenderDashboard(HtmlTplDashboardWidget dashboardWidget,
			HtmlTplDashboardRenderContext renderContext) throws RenderException, IOException
	{
		DashboardFilterContext context = new DashboardFilterContext(dashboardWidget, renderContext, nextDashboardId());
		DashboardFilterHandler filterHandler = new DashboardFilterHandler(context);
		
		getHtmlFilter().filter(renderContext.getTemplateReader(), filterHandler);
		
		return context;
	}
	
	protected DashboardFilterContext doRenderDashboard(HtmlTplDashboardWidget dashboardWidget,
			HtmlTplDashboardRenderContext renderContext, TplDashboardMeta dashboardMeta)
			throws RenderException, IOException
	{
		DashboardFilterContext context = new DashboardFilterContext(dashboardWidget, renderContext, dashboardMeta,
				nextDashboardId());
		IndexedDashboardFilterHandler filterHandler = new IndexedDashboardFilterHandler(context);
		
		getHtmlFilter().filter(renderContext.getTemplateReader(), filterHandler);
		
		return context;
	}

	/**
	 * 写看板脚本。
	 * 
	 * @param renderContext
	 * @param dashboardMeta
	 * @param dashboard
	 * @param writeScriptTag
	 * @throws IOException
	 */
	protected void writeDashboardScript(HtmlTplDashboardRenderContext renderContext, TplDashboardMeta dashboardMeta,
			HtmlTplDashboard dashboard, boolean writeScriptTag) throws IOException
	{
		String globalDashboardVar = dashboardMeta.getDashboardVar();
		if (StringUtil.isEmpty(globalDashboardVar))
			globalDashboardVar = getDefaultDashboardVar();
		
		Writer out = renderContext.getWriter();
		String dashboardCode = dashboardMeta.getDashboardCode();
		boolean writeDashboardInit = false;
		boolean writeDashboardRender = false;

		// 只处理合法值，否则应由dg-dashboard-auto-render特性决定
		if (DASHBOARD_CODE_ATTR_VALUE_INSTANCE.equalsIgnoreCase(dashboardCode))
		{
			writeDashboardInit = false;
			writeDashboardRender = false;
		}
		else if (DASHBOARD_CODE_ATTR_VALUE_INIT.equalsIgnoreCase(dashboardCode))
		{
			writeDashboardInit = true;
			writeDashboardRender = false;
		}
		else if (DASHBOARD_CODE_ATTR_VALUE_RENDER.equalsIgnoreCase(dashboardCode))
		{
			writeDashboardInit = true;
			writeDashboardRender = true;
		}
		else
		{
			writeDashboardInit = true;
			writeDashboardRender = dashboardMeta.isDashboardAutoRender();
		}

		String tmp0RenderContextVarName = renderContext.varNameOfRenderContext("Tmp0");
		String tmp1RenderContextVarName = renderContext.varNameOfRenderContext("Tmp1");
		String localDashboardVarName = renderContext.varNameOfDashboard("Tmp");

		dashboard.setVarName(localDashboardVarName);
		dashboard.setLoadableChartWidgets(dashboardMeta.getLoadableChartWidgets());

		if(writeScriptTag)
			writeScriptStartTag(out);
		
		writeNewLine(out);
		out.write("(function("+this.localGlobalVarName+"){");
		writeNewLine(out);

		writeDashboardJsVar(renderContext, dashboard, tmp0RenderContextVarName);

		writeChartScripts(renderContext, dashboard, dashboardMeta);
		writeDashboardJsInit(renderContext, dashboard, tmp1RenderContextVarName);
		writeDashboardJsFactoryInit(renderContext, dashboard, dashboardMeta.getDashboardFactoryVar());
		
		out.write(this.localGlobalVarName + "." + globalDashboardVar + "=" + localDashboardVarName + ";");
		writeNewLine(out);
		
		if (writeDashboardInit)
			writeDashboardJsInit(renderContext, dashboard);

		if (writeDashboardRender)
			writeDashboardJsRender(renderContext, dashboard);
		
		out.write("})(this);");
		writeNewLine(out);
		
		if(writeScriptTag)
		{
			writeScriptEndTag(out);
			writeNewLine(out);
		}
	}

	/**
	 * 写图表脚本。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @param dashboardMeta
	 * @throws IOException
	 */
	protected void writeChartScripts(HtmlTplDashboardRenderContext renderContext,
			HtmlTplDashboard dashboard, TplDashboardMeta dashboardMeta) throws IOException
	{
		List<Chart> charts = dashboard.getCharts();
		if (charts == null)
		{
			charts = new ArrayList<>();
			dashboard.setCharts(charts);
		}

		List<TplChartMeta> chartMetas = dashboardMeta.getTplChartMetas();
		
		if (chartMetas != null)
		{
			List<HtmlChartWidget> chartWidgets = getChartWidgets(chartMetas);
			List<String> chartPluginVarNames = writeChartPluginScriptsResolveImport(renderContext, chartWidgets);

			HtmlChartRenderContext chartRenderContext = new HtmlChartRenderContext(renderContext.getWriter());
			chartRenderContext.setAttributes(renderContext.getAttributes());
			chartRenderContext.setNotWriteChartElement(true);
			chartRenderContext.setNotWriteScriptTag(true);
			chartRenderContext.setNotWriteInvoke(true);
			chartRenderContext.setNotWritePluginObject(true);
			chartRenderContext.setNotWriteRenderContextObject(true);
			chartRenderContext.setRenderContextVarName(dashboard.getVarName() + "." + Dashboard.PROPERTY_RENDER_CONTEXT);
			
			String seed = nextChartIdSeed(dashboard);
			
			for (int i = 0, len = chartMetas.size(); i < len; i++)
			{
				TplChartMeta chartMeta = chartMetas.get(i);
				HtmlChartWidget chartWidget = chartWidgets.get(i);
				String chartId = nextChartId(dashboard, seed, i);
				
				chartRenderContext.setChartElementId(chartMeta.getElementId());
				chartRenderContext.setPluginVarName(chartPluginVarNames.get(i));
				chartRenderContext.setChartVarName(renderContext.varNameOfChart(Integer.toString(i)));
				
				HtmlChart chart = writeChart(chartRenderContext, chartWidget, chartId);
				charts.add(chart);
			}
		}
	}
	
	protected String nextChartIdSeed(HtmlTplDashboard dashboard)
	{
		return IDUtil.randomIdOnTime20();
	}
	
	protected String nextChartId(HtmlTplDashboard dashboard, String seed, int chartIndex)
	{
		return Integer.toString(chartIndex) + seed;
	}

	protected List<HtmlChartWidget> getChartWidgets(List<TplChartMeta> chartMetas)
	{
		List<HtmlChartWidget> list = new ArrayList<>();

		if (chartMetas == null)
			return list;

		for (TplChartMeta chartMeta : chartMetas)
			list.add(getHtmlChartWidgetForRender(chartMeta.getWidgetId()));

		return list;
	}

	/**
	 * {@linkplain TplDashboardMeta}缓存主键。
	 * 
	 * @author datagear@163.com
	 */
	protected static class TplDashboardMetaCacheKey implements Serializable
	{
		private static final long serialVersionUID = 1L;
	
		private final String dashboardWidgetId;
	
		private final String template;
	
		public TplDashboardMetaCacheKey(String dashboardWidgetId, String template)
		{
			super();
			this.dashboardWidgetId = dashboardWidgetId;
			this.template = template;
		}
	
		public String getDashboardWidgetId()
		{
			return dashboardWidgetId;
		}
	
		public String getTemplate()
		{
			return template;
		}
	
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dashboardWidgetId == null) ? 0 : dashboardWidgetId.hashCode());
			result = prime * result + ((template == null) ? 0 : template.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TplDashboardMetaCacheKey other = (TplDashboardMetaCacheKey) obj;
			if (dashboardWidgetId == null)
			{
				if (other.dashboardWidgetId != null)
					return false;
			}
			else if (!dashboardWidgetId.equals(other.dashboardWidgetId))
				return false;
			if (template == null)
			{
				if (other.template != null)
					return false;
			}
			else if (!template.equals(other.template))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [dashboardWidgetId=" + dashboardWidgetId + ", template=" + template
					+ "]";
		}
	}

	/**
	 * {@linkplain TplDashboardMeta}缓存值。
	 * 
	 * @author datagear@163.com
	 */
	protected static class TplDashboardMetaCacheValue implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final TplDashboardMeta dashboardMeta;

		private final long templateLastModified;

		public TplDashboardMetaCacheValue(TplDashboardMeta dashboardMeta, long templateLastModified)
		{
			super();
			this.dashboardMeta = dashboardMeta;
			this.templateLastModified = templateLastModified;
		}

		public TplDashboardMeta getDashboardMeta()
		{
			return dashboardMeta;
		}

		public long getTemplateLastModified()
		{
			return templateLastModified;
		}
	}

	/**
	 * 模板里解析而得的看板信息。
	 * <p>
	 * 此类需支持序列化，因为可能被缓存。
	 * </p>
	 * 
	 * @author datagear@163.com
	 */
	protected static class TplDashboardMeta implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 看板变量名称 */
		private String dashboardVar = null;
		
		/** 看板工厂名称 */
		private String dashboardFactoryVar = null;
		
		/** 导入排除项 */
		private String dashboardUnimport = null;

		/** 看板脚本 */
		private String dashboardCode = null;

		/**
		 * 是否自动执行看板渲染函数
		 * 
		 * @deprecated {@code dg-dashboard-auto-render}特性已在4.4.0版本废弃，后续版本将移除
		 */
		@Deprecated
		private String dashboardAutoRender = null;
		
		/** 图表信息 */
		private List<TplChartMeta> chartMetas = new ArrayList<>();
		
		/** 异步加载信息 */
		private LoadableChartWidgets loadableChartWidgets = null;
		
		/**
		 * 标签开始前置插入内容：标签索引 -&gt; 插入内容
		 */
		private Map<Integer, List<TplDashboardInserter>> beforeWriteTagStartInsertersMap = new HashMap<Integer, List<TplDashboardInserter>>();
		
		/**
		 * 标签结束前置插入内容：标签索引 -&gt; 插入内容
		 */
		private Map<Integer, List<TplDashboardInserter>> beforeWriteTagEndInsertersMap = new HashMap<Integer, List<TplDashboardInserter>>();
		
		/**
		 * 标签结束后置插入内容：标签索引 -&gt; 插入内容
		 */
		private Map<Integer, List<TplDashboardInserter>> afterWriteTagEndInsertersMap = new HashMap<Integer, List<TplDashboardInserter>>();
		
		/**
		 * 文档结束后置插入内容：标签索引 -&gt; 插入内容
		 */
		private List<TplDashboardInserter> afterWriteInserters = new ArrayList<TplDashboardInserter>();
		
		public TplDashboardMeta()
		{
			super();
		}

		public boolean hasDashboardVar()
		{
			return !StringUtil.isEmpty(this.dashboardVar);
		}

		public String getDashboardVar()
		{
			return dashboardVar;
		}

		public void setDashboardVar(String dashboardVar)
		{
			this.dashboardVar = dashboardVar;
		}
		
		public boolean hasDashboardFactoryVar()
		{
			return !StringUtil.isEmpty(this.dashboardFactoryVar);
		}

		public String getDashboardFactoryVar()
		{
			return dashboardFactoryVar;
		}

		public void setDashboardFactoryVar(String dashboardFactoryVar)
		{
			this.dashboardFactoryVar = dashboardFactoryVar;
		}
		
		public boolean hasDashboardUnimport()
		{
			return !StringUtil.isEmpty(this.dashboardUnimport);
		}

		public String getDashboardUnimport()
		{
			return dashboardUnimport;
		}

		public void setDashboardUnimport(String dashboardUnimport)
		{
			this.dashboardUnimport = dashboardUnimport;
		}
		
		public boolean hasDashboardCode()
		{
			return !StringUtil.isEmpty(this.dashboardCode);
		}
		
		public String getDashboardCode()
		{
			return dashboardCode;
		}

		public void setDashboardCode(String dashboardCode)
		{
			this.dashboardCode = dashboardCode;
		}

		/**
		 * @deprecated {@code dg-dashboard-auto-render}特性已在4.4.0版本废弃，后续版本将移除
		 * @return
		 */
		@Deprecated
		public boolean isDashboardAutoRender()
		{
			return !Boolean.FALSE.toString().equalsIgnoreCase(this.dashboardAutoRender);
		}

		/**
		 * @deprecated {@code dg-dashboard-auto-render}特性已在4.4.0版本废弃，后续版本将移除
		 * @return
		 */
		@Deprecated
		public String getDashboardAutoRender()
		{
			return dashboardAutoRender;
		}

		/**
		 * @deprecated {@code dg-dashboard-auto-render}特性已在4.4.0版本废弃，后续版本将移除
		 * @param dashboardAutoRender
		 */
		@Deprecated
		public void setDashboardAutoRender(String dashboardAutoRender)
		{
			this.dashboardAutoRender = dashboardAutoRender;
		}

		public List<TplChartMeta> getTplChartMetas()
		{
			return chartMetas;
		}

		public void addTplChartMeta(TplChartMeta tplChartMeta)
		{
			this.chartMetas.add(tplChartMeta);
		}

		public LoadableChartWidgets getLoadableChartWidgets()
		{
			return loadableChartWidgets;
		}

		public void setLoadableChartWidgets(LoadableChartWidgets loadableChartWidgets)
		{
			this.loadableChartWidgets = loadableChartWidgets;
		}

		public Map<Integer, List<TplDashboardInserter>> getBeforeWriteTagStartInsertersMap()
		{
			return beforeWriteTagStartInsertersMap;
		}

		public Map<Integer, List<TplDashboardInserter>> getBeforeWriteTagEndInsertersMap()
		{
			return beforeWriteTagEndInsertersMap;
		}

		public Map<Integer, List<TplDashboardInserter>> getAfterWriteTagEndInsertersMap()
		{
			return afterWriteTagEndInsertersMap;
		}
		
		public List<TplDashboardInserter> getBeforeWriteTagStartInserters(int tagIndex)
		{
			return beforeWriteTagStartInsertersMap.get(tagIndex);
		}

		public List<TplDashboardInserter> getBeforeWriteTagEndInserters(int tagIndex)
		{
			return beforeWriteTagEndInsertersMap.get(tagIndex);
		}

		public List<TplDashboardInserter> getAfterWriteTagEndInserters(int tagIndex)
		{
			return afterWriteTagEndInsertersMap.get(tagIndex);
		}

		public List<TplDashboardInserter> getAfterWriteInserters()
		{
			return afterWriteInserters;
		}

		public void addBeforeWriteTagStartInserter(int tagIndex, TplDashboardInserter inserter)
		{
			addInserter(this.beforeWriteTagStartInsertersMap, tagIndex, inserter);
		}

		public void addBeforeWriteTagEndInserter(int tagIndex, TplDashboardInserter inserter)
		{
			addInserter(this.beforeWriteTagEndInsertersMap, tagIndex, inserter);
		}

		public void addAfterWriteTagEndInserter(int tagIndex, TplDashboardInserter inserter)
		{
			addInserter(this.afterWriteTagEndInsertersMap, tagIndex, inserter);
		}

		public void addAfterWriteInserter(TplDashboardInserter inserter)
		{
			this.afterWriteInserters.add(inserter);
		}
		
		protected void addInserter(Map<Integer, List<TplDashboardInserter>> inserterss, int tagIndex, TplDashboardInserter inserter)
		{
			List<TplDashboardInserter> inserters = inserterss.get(tagIndex);
			if(inserters == null)
			{
				inserters = new ArrayList<TplDashboardInserter>(3);
				inserterss.put(tagIndex, inserters);
			}
			
			inserters.add(inserter);
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [dashboardVar=" + dashboardVar + ", dashboardFactoryVar=" + dashboardFactoryVar
					+ ", dashboardUnimport=" + dashboardUnimport + ", dashboardCode=" + dashboardCode
					+ ", chartMetas=" + chartMetas + "]";
		}
	}

	/**
	 * 模板里解析而得的图表信息。
	 * <p>
	 * 此类需支持序列化，因为可能被缓存。
	 * </p>
	 * 
	 * @author datagear@163.com
	 */
	protected static class TplChartMeta implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 图表部件ID */
		private String widgetId;
		
		/** 图表元素ID */
		private String elementId;
		
		public TplChartMeta(String widgetId, String elementId)
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
	
	/**
	 * HTML看板模板插入内容。
	 * <p>
	 * 子类需支持序列化，因为可能被缓存。
	 * </p>
	 * 
	 * @author datagear@163.com
	 */
	protected static interface TplDashboardInserter extends Serializable
	{
		/**
		 * 插入内容。
		 * 
		 * @param filterContext
		 * @throws IOException
		 */
		void insert(DashboardFilterContext filterContext) throws IOException;
	}
	
	/**
	 * 插入内容：静态文本。
	 * 
	 * @author datagear@163.com
	 */
	protected static class TplDashboardTextInserter implements TplDashboardInserter
	{
		private static final long serialVersionUID = 1L;

		private String text = "";

		public TplDashboardTextInserter()
		{
			super();
		}

		public TplDashboardTextInserter(String text)
		{
			super();
			this.text = text;
		}

		public String getText()
		{
			return text;
		}

		public void setText(String text)
		{
			this.text = text;
		}

		@Override
		public void insert(DashboardFilterContext filterContext) throws IOException
		{
			Writer out = filterContext.getRenderContext().getWriter();
			out.write(this.text);
		}
	}

	/**
	 * 插入内容：标题。
	 * <p>
	 * 注意：由于需要支持序列化，所以此类不能定义为非静态匿名内部类。
	 * </p>
	 * 
	 * @author datagear@163.com
	 */
	protected static class TplDashboardTitleInserter implements TplDashboardInserter
	{
		private static final long serialVersionUID = 1L;

		private final String rawTitleContent;

		private final boolean wrapTitleTag;

		public TplDashboardTitleInserter(String rawTitleContent, boolean wrapTitleTag)
		{
			super();
			this.rawTitleContent = rawTitleContent;
			this.wrapTitleTag = wrapTitleTag;
		}

		public String getRawTitleContent()
		{
			return rawTitleContent;
		}

		public boolean isWrapTitleTag()
		{
			return wrapTitleTag;
		}

		@Override
		public void insert(DashboardFilterContext filterContext) throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 插入内容：看板导入项。
	 * <p>
	 * 注意：由于需要支持序列化，所以此类不能定义为非静态匿名内部类。
	 * </p>
	 * 
	 * @author datagear@163.com
	 */
	protected static class TplDashboardImportInserter implements TplDashboardInserter
	{
		private static final long serialVersionUID = 1L;
	
		public TplDashboardImportInserter()
		{
			super();
		}
		
		@Override
		public void insert(DashboardFilterContext filterContext) throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 插入内容：看板脚本。
	 * <p>
	 * 注意：由于需要支持序列化，所以此类不能定义为非静态匿名内部类。
	 * </p>
	 * 
	 * @author datagear@163.com
	 */
	protected static class TplDashboardScriptInserter implements TplDashboardInserter
	{
		private static final long serialVersionUID = 1L;
	
		private final boolean writeScriptTag;
		
		public TplDashboardScriptInserter(boolean writeScriptTag)
		{
			super();
			this.writeScriptTag = writeScriptTag;
		}
	
		public boolean isWriteScriptTag()
		{
			return writeScriptTag;
		}
	
		@Override
		public void insert(DashboardFilterContext filterContext) throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * HTML看板模板过滤上下文。
	 * <p>
	 * 完成过滤后，将填充{@linkplain DashboardFilterContext#getDashboard()}、{@linkplain DashboardFilterContext#getDashboardMeta()}。
	 * </p>
	 * 
	 * @author datagear@163.com
	 */
	protected static class DashboardFilterContext
	{
		private final HtmlTplDashboardWidget dashboardWidget;
		private final HtmlTplDashboardRenderContext renderContext;
		
		private final TplDashboardMeta dashboardMeta;
		private final HtmlTplDashboard dashboard;
		
		public DashboardFilterContext(HtmlTplDashboardWidget dashboardWidget, HtmlTplDashboardRenderContext renderContext, String dashboardId)
		{
			super();
			this.dashboardWidget = dashboardWidget;
			this.renderContext = renderContext;
			this.dashboard = createDashboard(dashboardWidget, renderContext, dashboardId, renderContext.getTemplate());
			this.dashboardMeta = new TplDashboardMeta();
		}

		public DashboardFilterContext(HtmlTplDashboardWidget dashboardWidget, HtmlTplDashboardRenderContext renderContext,
										TplDashboardMeta dashboardMeta, String dashboardId)
		{
			super();
			this.dashboardWidget = dashboardWidget;
			this.renderContext = renderContext;
			this.dashboardMeta = dashboardMeta;
			this.dashboard = createDashboard(dashboardWidget, renderContext, dashboardId, renderContext.getTemplate());
		}

		public DashboardFilterContext(HtmlTplDashboardWidget dashboardWidget, HtmlTplDashboardRenderContext renderContext, TplDashboardMeta dashboardMeta,
				HtmlTplDashboard dashboard)
		{
			super();
			this.dashboardWidget = dashboardWidget;
			this.renderContext = renderContext;
			this.dashboardMeta = dashboardMeta;
			this.dashboard = dashboard;
		}

		public HtmlTplDashboardWidget getDashboardWidget()
		{
			return dashboardWidget;
		}

		public HtmlTplDashboardRenderContext getRenderContext()
		{
			return renderContext;
		}

		public TplDashboardMeta getDashboardMeta()
		{
			return dashboardMeta;
		}

		public HtmlTplDashboard getDashboard()
		{
			return dashboard;
		}
	};
	
	/**
	 * HTML看板模板过滤器。
	 * 
	 * @author datagear@163.com
	 */
	protected class DashboardFilterHandler extends HeadBodyAwareFilterHandler
	{
		public static final String POS_BEFORE_WRITE_TAG_START = "beforeWriteTagStart";
		public static final String POS_BEFORE_WRITE_TAG_END = "beforeWriteTagEnd";
		public static final String POS_AFTER_WRITE_TAG_END = "afterWriteTagEnd";
		public static final String POS_AFTER_WRITE = "afterWrite";
		
		private final DashboardFilterContext filterContext;
		
		private int tagCount = 0;
		private boolean htmlTagResolved = false;
		private boolean inTitleTag = false;
		private boolean titleTagHandled = false;
		private boolean dashboardImportWritten = false;
		private boolean dashboardScriptWritten = false;

		public DashboardFilterHandler(DashboardFilterContext filterContext)
		{
			super(new CopyWriter(filterContext.getRenderContext().getWriter(), new StringWriter(), false));
			this.filterContext = filterContext;
		}

		public DashboardFilterContext getFilterContext()
		{
			return filterContext;
		}

		@Override
		public void beforeWriteTagStart(Reader in, String tagName) throws IOException
		{
			// </body>前写看板脚本
			if (!this.dashboardScriptWritten && this.isInBodyTag() && equalsIgnoreCase(tagName, "/body"))
			{
				// 确保看板脚本前已写完看板导入库，比如没有定义<head></head>
				writeHtmlTplDashboardScriptIfNon(true, POS_BEFORE_WRITE_TAG_START);
			}

			// 处理标题
			if (!this.titleTagHandled && this.isInHeadTag())
			{
				// 优先</title>前插入标题后缀
				if (equalsIgnoreCase(tagName, "/title"))
				{
					HtmlTitleHandler htmlTitleHandler = this.filterContext.getRenderContext().getHtmlTitleHandler();
					if (htmlTitleHandler != null)
					{
						String titleContent = ((StringWriter) this.getCopyWriter().getCopyOut()).toString();
						write(htmlTitleHandler.suffix(titleContent));
						
						TplDashboardTitleInserter inserter = new TplDashboardTitleInserter(titleContent, false);
						this.filterContext.getDashboardMeta().addBeforeWriteTagStartInserter(this.tagCount, inserter);
					}
					
					this.titleTagHandled = true;
				}
				// 其次</head>前插入<title></title>
				else if(equalsIgnoreCase(tagName, "/head"))
				{
					HtmlTitleHandler htmlTitleHandler = this.filterContext.getRenderContext().getHtmlTitleHandler();
					if (htmlTitleHandler != null)
					{
						write(HTML_TAG_TITLE_START);
						write(htmlTitleHandler.suffix(""));
						write(HTML_TAG_TITLE_CLOSE);
						
						TplDashboardTitleInserter inserter = new TplDashboardTitleInserter("", true);
						this.filterContext.getDashboardMeta().addBeforeWriteTagStartInserter(this.tagCount, inserter);
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
			
			this.tagCount++;
		}

		@Override
		public boolean isResolveTagAttrs(Reader in, String tagName)
		{
			//看板脚本未写入时<body></body>内需解析属性的标签
			if(!this.dashboardScriptWritten && this.isInBodyTag())
			{
				//图表元素
				if(equalsIgnoreCase(tagName, HtmlTplDashboardWidgetHtmlRenderer.this.chartTagName))
				{
					return true;
				}
				//<script dg-dashboard-code>
				else if(equalsIgnoreCase(tagName, TAG_NAME_SCRIPT))
				{
					return true;
				}
			}
			
			//<html ...>
			if(!this.htmlTagResolved && equalsIgnoreCase(tagName, "html"))
			{
				return true;
			}
			
			return false;
		}
		
		@Override
		public void beforeWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
				throws IOException
		{
			// 解析<body></body>内的图表元素属性
			if (!this.dashboardScriptWritten && this.isInBodyTag())
			{
				if(equalsIgnoreCase(tagName, HtmlTplDashboardWidgetHtmlRenderer.this.chartTagName))
				{
					resolveChartTagAttr(attrs);
				}
			}
			
			// 解析<html>标签上的看板属性
			if (!this.htmlTagResolved && equalsIgnoreCase(tagName, "html"))
			{
				resolveHtmlTagAttr(attrs);
				this.htmlTagResolved = true;
			}
		}

		@Override
		public void afterWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs) throws IOException
		{
			super.afterWriteTagEnd(in, tagName, tagEnd, attrs);
			
			if (!this.inTitleTag && this.isInHeadTag() && equalsIgnoreCase(tagName, "title"))
			{
				this.inTitleTag = !isSelfCloseTagEnd(tagEnd);
				this.getCopyWriter().setCopy(this.inTitleTag);
			}

			// 优先<head>后，其次<body>后（当没有定义<head>时）插入看板导入库
			if (!this.dashboardImportWritten && !isSelfCloseTagEnd(tagEnd)
					&& (equalsIgnoreCase(tagName, "head") || equalsIgnoreCase(tagName, "body")))
			{
				writeDashboardImportIfNon(POS_AFTER_WRITE_TAG_END);
			}
			
			// <body></body>内的<script dg-dashboard-code></script>内写入看板脚本
			// 必须限定在<body></body>内，因为页面端看板初始化需要<body>标签上的属性信息
			if(!this.dashboardScriptWritten && this.isInBodyTag() && equalsIgnoreCase(tagName, TAG_NAME_SCRIPT))
			{
				// 此处不需要再次校验和writeDashboardImportWithSet()，因为上面已确保写入
				//if (!this.dashboardImportWritten)
				//	writeDashboardImportWithSet();
				
				if(attrs != null && attrs.containsKey(HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardCode))
				{
					String dashboardCode = attrs.get(HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardCode);
					if (!StringUtil.isEmpty(dashboardCode))
						this.filterContext.getDashboardMeta().setDashboardCode(dashboardCode);

					writeHtmlTplDashboardScriptIfNon(isSelfCloseTagEnd(tagEnd), POS_AFTER_WRITE_TAG_END);
				}
			}

			// 如果</body>前没写（没有定义</body>），则在</html>后写看板脚本
			if (!this.dashboardScriptWritten && !this.isInHeadTag() && !this.isInBodyTag()
					&& equalsIgnoreCase(tagName, "/html"))
			{
				// 确保看板脚本前已写完看板导入库，比如没有定义<head></head>、<body></body>
				writeHtmlTplDashboardScriptIfNon(true, POS_AFTER_WRITE_TAG_END);
			}
		}

		@Override
		public void afterWrite(Reader in) throws IOException
		{
			if (!this.isAborted())
			{
				// 如果已全部完成而仍没有写看板脚本，则应在此写入，比如原始HTML里没有</body>标签
				writeHtmlTplDashboardScriptIfNon(true, POS_AFTER_WRITE);
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

		protected boolean writeDashboardImportIfNon(String pos) throws IOException
		{
			if(this.dashboardImportWritten)
				return false;
			
			writeDashboardImport(this.filterContext.getRenderContext(),
					this.filterContext.getDashboard(), this.filterContext.getDashboardMeta().getDashboardUnimport());

			TplDashboardMeta dashboardMeta = this.filterContext.getDashboardMeta();
			TplDashboardImportInserter inserter = new TplDashboardImportInserter();
			
			if(POS_BEFORE_WRITE_TAG_START.equals(pos))
				dashboardMeta.addBeforeWriteTagStartInserter(this.tagCount, inserter);
			else if(POS_BEFORE_WRITE_TAG_END.equals(pos))
				dashboardMeta.addBeforeWriteTagEndInserter(this.tagCount, inserter);
			else if(POS_AFTER_WRITE_TAG_END.equals(pos))
				dashboardMeta.addAfterWriteTagEndInserter(this.tagCount, inserter);
			else if(POS_AFTER_WRITE.equals(pos))
				dashboardMeta.addAfterWriteInserter(inserter);
			else
				throw new UnsupportedOperationException();

			this.dashboardImportWritten = true;
			
			return true;
		}
		
		protected boolean writeHtmlTplDashboardScriptIfNon(boolean writeScriptTag, String pos) throws IOException
		{
			if(this.dashboardScriptWritten)
				return false;
			
			writeDashboardImportIfNon(pos);
			
			writeDashboardScript(this.filterContext.getRenderContext(),
					this.filterContext.getDashboardMeta(), this.filterContext.getDashboard(), writeScriptTag);

			TplDashboardMeta dashboardMeta = this.filterContext.getDashboardMeta();
			TplDashboardScriptInserter inserter = new TplDashboardScriptInserter(writeScriptTag);
			
			if(POS_BEFORE_WRITE_TAG_START.equals(pos))
				dashboardMeta.addBeforeWriteTagStartInserter(this.tagCount, inserter);
			else if(POS_BEFORE_WRITE_TAG_END.equals(pos))
				dashboardMeta.addBeforeWriteTagEndInserter(this.tagCount, inserter);
			else if(POS_AFTER_WRITE_TAG_END.equals(pos))
				dashboardMeta.addAfterWriteTagEndInserter(this.tagCount, inserter);
			else if(POS_AFTER_WRITE.equals(pos))
				dashboardMeta.addAfterWriteInserter(inserter);
			else
				throw new UnsupportedOperationException();
			
			this.dashboardScriptWritten = true;
			
			return true;
		}

		protected void resolveHtmlTagAttr(Map<String, String> attrs)
		{
			for (Map.Entry<String, String> entry : attrs.entrySet())
			{
				String name = entry.getKey();

				if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardVar.equalsIgnoreCase(name))
				{
					this.filterContext.getDashboardMeta().setDashboardVar(trim(entry.getValue()));
				}
				else if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardFactory.equalsIgnoreCase(name))
				{
					this.filterContext.getDashboardMeta().setDashboardFactoryVar(trim(entry.getValue()));
				}
				else if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardUnimport.equalsIgnoreCase(name))
				{
					this.filterContext.getDashboardMeta().setDashboardUnimport(trim(entry.getValue()));
				}
				else if(HtmlTplDashboardWidgetHtmlRenderer.this.attrNameLoadableChartWidgets.equalsIgnoreCase(name))
				{
					this.filterContext.getDashboardMeta().setLoadableChartWidgets(resolveLoadableChartWidgets(trim(entry.getValue())));
				}
				else if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardCode.equalsIgnoreCase(name))
				{
					this.filterContext.getDashboardMeta().setDashboardCode(trim(entry.getValue()));
				}
				else if (HtmlTplDashboardWidgetHtmlRenderer.this.attrNameDashboardAutoRender.equalsIgnoreCase(name))
				{
					this.filterContext.getDashboardMeta().setDashboardAutoRender(trim(entry.getValue()));
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

			TplDashboardMeta dashboardMeta = this.filterContext.getDashboardMeta();
			TplChartMeta chartMeta = new TplChartMeta(chartWidget, elementId);
			
			// 元素没有定义“id”属性，应自动插入
			if (StringUtil.isEmpty(chartMeta.getElementId()))
			{
				String elementIdSuffix = Integer.toString(dashboardMeta.getTplChartMetas().size());
				elementId = this.filterContext.getRenderContext().elementIdOfChart(elementIdSuffix);
				chartMeta.setElementId(elementId);

				String writeContent = " id=\"" + elementId + "\" ";
				write(writeContent);
				
				TplDashboardTextInserter inserter = new TplDashboardTextInserter(writeContent);
				dashboardMeta.addBeforeWriteTagEndInserter(this.tagCount, inserter);
			}

			dashboardMeta.addTplChartMeta(chartMeta);
		}
	}
	
	/**
	 * 基于{@linkplain DashboardFilterContext#getDashboardMeta()}中索引信息的HTML看板模板过滤器。
	 * 
	 * @author datagear@163.com
	 */
	protected class IndexedDashboardFilterHandler extends DefaultFilterHandler
	{
		private final DashboardFilterContext filterContext;
		
		private final TplDashboardMeta dashboardMeta;
		private int tagCount = 0;

		public IndexedDashboardFilterHandler(DashboardFilterContext filterContext)
		{
			super(filterContext.getRenderContext().getWriter());
			this.filterContext = filterContext;
			this.dashboardMeta = this.filterContext.getDashboardMeta();
			
			if(this.dashboardMeta == null)
				throw new IllegalArgumentException();
		}

		public DashboardFilterContext getFilterContext()
		{
			return filterContext;
		}

		@Override
		public void beforeWriteTagStart(Reader in, String tagName) throws IOException
		{
			doInsert(this.dashboardMeta.getBeforeWriteTagStartInserters(this.tagCount));
			
			this.tagCount++;
		}

		@Override
		public void beforeWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
				throws IOException
		{
			doInsert(this.dashboardMeta.getBeforeWriteTagEndInserters(this.tagCount));
		}

		@Override
		public void afterWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
				throws IOException
		{
			doInsert(this.dashboardMeta.getAfterWriteTagEndInserters(this.tagCount));
		}

		@Override
		public void afterWrite(Reader in) throws IOException
		{
			doInsert(this.dashboardMeta.getAfterWriteInserters());
		}
		
		protected void doInsert(List<TplDashboardInserter> inserters) throws IOException
		{
			if(inserters == null || inserters.isEmpty())
				return;

			for(TplDashboardInserter inserter : inserters)
			{
				if (inserter instanceof TplDashboardTitleInserter)
				{
					HtmlTitleHandler htmlTitleHandler = this.filterContext.getRenderContext().getHtmlTitleHandler();

					if (htmlTitleHandler != null)
					{
						TplDashboardTitleInserter titleInserter = (TplDashboardTitleInserter) inserter;
						Writer out = filterContext.getRenderContext().getWriter();

						if (titleInserter.isWrapTitleTag())
							out.write(HTML_TAG_TITLE_START);

						out.write(htmlTitleHandler.suffix(titleInserter.getRawTitleContent()));

						if (titleInserter.isWrapTitleTag())
							out.write(HTML_TAG_TITLE_CLOSE);
					}
				}
				else if (inserter instanceof TplDashboardImportInserter)
				{
					writeDashboardImport(this.filterContext.getRenderContext(), this.filterContext.getDashboard(),
							this.filterContext.getDashboardMeta().getDashboardUnimport());
				}
				else if (inserter instanceof TplDashboardScriptInserter)
				{
					writeDashboardScript(this.filterContext.getRenderContext(), this.filterContext.getDashboardMeta(),
							this.filterContext.getDashboard(),
							((TplDashboardScriptInserter) inserter).isWriteScriptTag());
				}
				else
				{
					inserter.insert(this.filterContext);
				}
			}
		}
	}
}
