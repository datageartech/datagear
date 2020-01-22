/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.Chart;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.DashboardWidgetResManager;
import org.datagear.util.StringUtil;

import freemarker.core.Environment;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * 使用Freemarker作为模板的{@linkplain HtmlTplDashboardWidget}渲染器。
 * <p>
 * 此类可渲染由{@linkplain DashboardWidgetResManager}管理模板的{@linkplain HtmlTplDashboardWidget}，
 * 其中{@linkplain HtmlTplDashboardWidget#getTemplate()}应该是可以通过{@linkplain DashboardWidgetResManager#getFile(String, String)}找到的模板文件名。
 * </p>
 * <p>
 * 此类需要手动调用{@linkplain #init()}方法进行初始化。
 * </p>
 * <p>
 * 支持的模板格式如下：
 * </p>
 * <code>
 * <pre>
 * ...
 * &lt;@import /&gt;
 * ...
 * &lt;@theme /&gt;
 * ...
 * &lt;@dashboard var="..." renderer="..."&gt;
 *   ...
 *   <@chart widget="..." var="..." elementId="..." /&gt;
 *   ...
 *   <@chart widget="..." var="..." elementId="..." /&gt;
 *   ...
 * &lt;/@dashboard&gt;
 * </pre>
 * </code>
 * <p>
 * &lt;@import /&gt;：引入内置JS、CSS等HTML资源。
 * </p>
 * <p>
 * &lt;@theme /&gt;：引入内置CSS主题样式。
 * </p>
 * <p>
 * &lt;@dashboard&gt;：定义看板，“var”自定义看板JS变量名，可不填；“renderer”自定义看板渲染器JS变量，可不填，默认为{@linkplain HtmlTplDashboardWidgetRenderer#getDefaultDashboardRendererVar()}。
 * </p>
 * <p>
 * &lt;@chart
 * /&gt;：定义图表，“widget”为{@linkplain HtmlChartWidget#getId()}，必填；“var”自定义图表JS变量名，可不填；“elementId”自定义图表HTML元素ID，可不填。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class HtmlTplDashboardWidgetFmkRenderer<T extends HtmlRenderContext> extends HtmlTplDashboardWidgetRenderer<T>
{
	public static final String DIRECTIVE_IMPORT = "import";

	public static final String DIRECTIVE_THEME = "theme";

	public static final String DIRECTIVE_DASHBOARD = "dashboard";

	public static final String DIRECTIVE_CHART = "chart";

	protected static final String KEY_HTML_DASHBOARD_RENDER_DATA_MODEL = HtmlDashboardRenderDataModel.class
			.getSimpleName();

	private String defaultTemplateEncoding = "UTF-8";

	private boolean ignoreDashboardStyleBorderWidth = true;

	private Configuration _configuration;

	public HtmlTplDashboardWidgetFmkRenderer()
	{
		super();
	}

	public HtmlTplDashboardWidgetFmkRenderer(DashboardWidgetResManager dashboardWidgetResManager,
			ChartWidgetSource chartWidgetSource)
	{
		super(dashboardWidgetResManager, chartWidgetSource);
	}

	public String getDefaultTemplateEncoding()
	{
		return defaultTemplateEncoding;
	}

	public void setDefaultTemplateEncoding(String defaultTemplateEncoding)
	{
		this.defaultTemplateEncoding = defaultTemplateEncoding;
	}

	public boolean isIgnoreDashboardStyleBorderWidth()
	{
		return ignoreDashboardStyleBorderWidth;
	}

	public void setIgnoreDashboardStyleBorderWidth(boolean ignoreDashboardStyleBorderWidth)
	{
		this.ignoreDashboardStyleBorderWidth = ignoreDashboardStyleBorderWidth;
	}

	/**
	 * 初始化。
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException
	{
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
		cfg.setDirectoryForTemplateLoading(getDashboardWidgetResManager().getRootDirectory());
		cfg.setDefaultEncoding(this.defaultTemplateEncoding);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);

		cfg.setSharedVariable(DIRECTIVE_IMPORT, new ImportTemplateDirectiveModel());
		cfg.setSharedVariable(DIRECTIVE_THEME, new ThemeTemplateDirectiveModel());
		cfg.setSharedVariable(DIRECTIVE_DASHBOARD, new DashboardTemplateDirectiveModel());
		cfg.setSharedVariable(DIRECTIVE_CHART, new ChartTemplateDirectiveModel());

		setConfiguration(cfg);
	}

	@Override
	protected void renderHtmlDashboard(T renderContext, HtmlDashboard dashboard) throws Throwable
	{
		HtmlDashboardRenderDataModel dataModel = new HtmlDashboardRenderDataModel(dashboard,
				renderContext.getWebContext().getContextPath());

		Template template = getTemplate((HtmlTplDashboardWidget<?>) dashboard.getWidget());

		template.process(buildHtmlDashboardRenderDataModel(dataModel), renderContext.getWriter());
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardWidget#getId()}的指定模板对象。
	 * 
	 * @param dashboardWidget
	 * @return
	 * @throws Exception
	 */
	protected Template getTemplate(HtmlTplDashboardWidget<?> dashboardWidget) throws Exception
	{
		String path = getDashboardWidgetResManager().getRelativePath(dashboardWidget.getId(),
				dashboardWidget.getTemplate());

		return getConfiguration().getTemplate(path);
	}

	protected Configuration getConfiguration()
	{
		return _configuration;
	}

	protected void setConfiguration(Configuration _configuration)
	{
		this._configuration = _configuration;
	}

	protected Object buildHtmlDashboardRenderDataModel(HtmlDashboardRenderDataModel dataModel)
	{
		Map<String, Object> map = new HashMap<String, Object>();

		map.put(KEY_HTML_DASHBOARD_RENDER_DATA_MODEL, dataModel);

		return map;
	}

	protected HtmlDashboardRenderDataModel getHtmlDashboardRenderDataModel(Environment env)
			throws TemplateModelException
	{
		TemplateHashModel templateHashModel = env.getDataModel();
		HtmlDashboardRenderDataModel dataModel = (HtmlDashboardRenderDataModel) templateHashModel
				.get(KEY_HTML_DASHBOARD_RENDER_DATA_MODEL);

		return dataModel;
	}

	/**
	 * HTML看板渲染数据模型。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class HtmlDashboardRenderDataModel implements WrapperTemplateModel
	{
		private HtmlDashboard htmlDashboard;

		private String contextPath = "";

		public HtmlDashboardRenderDataModel()
		{
			super();
		}

		public HtmlDashboardRenderDataModel(HtmlDashboard htmlDashboard, String contextPath)
		{
			super();
			this.htmlDashboard = htmlDashboard;
			this.contextPath = contextPath;
		}

		public HtmlDashboard getHtmlDashboard()
		{
			return htmlDashboard;
		}

		public void setHtmlDashboard(HtmlDashboard htmlDashboard)
		{
			this.htmlDashboard = htmlDashboard;
		}

		public String getContextPath()
		{
			return contextPath;
		}

		public void setContextPath(String contextPath)
		{
			this.contextPath = contextPath;
		}

		@Override
		public Object getWrappedObject()
		{
			return this.htmlDashboard;
		}
	}

	protected abstract class AbstractTemplateDirectiveModel implements TemplateDirectiveModel
	{
		public AbstractTemplateDirectiveModel()
		{
			super();
		}

		/**
		 * 获取字符串参数值。
		 * 
		 * @param params
		 * @param key
		 * @return
		 * @throws TemplateModelException
		 */
		protected String getStringParamValue(Map<?, ?> params, String key) throws TemplateModelException
		{
			Object value = params.get(key);

			if (value == null)
				return null;
			else if (value instanceof String)
				return (String) value;
			else if (value instanceof TemplateScalarModel)
				return ((TemplateScalarModel) value).getAsString();
			else
				throw new TemplateModelException(
						"Can not get string from [" + value.getClass().getName() + "] instance");
		}
	}

	/**
	 * “@import”指令。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class ImportTemplateDirectiveModel extends AbstractTemplateDirectiveModel
	{
		public ImportTemplateDirectiveModel()
		{
			super();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
				throws TemplateException, IOException
		{
			HtmlDashboardRenderDataModel dataModel = getHtmlDashboardRenderDataModel(env);
			HtmlDashboard dashboard = dataModel.getHtmlDashboard();
			HtmlRenderContext renderContext = dashboard.getRenderContext();

			writeDashboardImport(renderContext, dashboard, "");
		}
	}

	/**
	 * “@theme”指令。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class ThemeTemplateDirectiveModel extends AbstractTemplateDirectiveModel
	{
		public ThemeTemplateDirectiveModel()
		{
			super();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
				throws TemplateException, IOException
		{
			HtmlDashboardRenderDataModel dataModel = getHtmlDashboardRenderDataModel(env);
			HtmlDashboard dashboard = dataModel.getHtmlDashboard();
			HtmlRenderContext renderContext = dashboard.getRenderContext();

			Writer out = env.getOut();

			writeDashboardThemeStyle(renderContext, dashboard, out);
		}
	}

	/**
	 * “@dashboard”指令。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class DashboardTemplateDirectiveModel extends AbstractTemplateDirectiveModel
	{
		public DashboardTemplateDirectiveModel()
		{
			super();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
				throws TemplateException, IOException
		{
			String dashboardVar = getStringParamValue(params, "var");
			String rendererVar = getStringParamValue(params, "renderer");

			if (StringUtil.isEmpty(dashboardVar))
				dashboardVar = getDefaultDashboardVar();

			HtmlDashboardRenderDataModel dataModel = getHtmlDashboardRenderDataModel(env);
			HtmlDashboard dashboard = dataModel.getHtmlDashboard();
			HtmlRenderContext renderContext = dashboard.getRenderContext();

			dashboard.setVarName(dashboardVar);

			Writer out = env.getOut();

			writeScriptStartTag(out);
			writeNewLine(out);

			writeHtmlDashboardJSVar(renderContext, out, dashboard);

			writeScriptEndTag(out);
			writeNewLine(out);

			setHtmlChartPluginRenderOption(renderContext, dashboardVar);

			if (body != null)
				body.render(out);

			writeScriptStartTag(out);
			writeNewLine(out);

			// 移除内部设置的属性
			HtmlChartPluginRenderOption.removeOption(renderContext);

			writeHtmlDashboardJSInit(out, dashboard);
			writeHtmlDashboardJSRender(out, dashboard, rendererVar);

			writeScriptEndTag(out);
			writeNewLine(out);
		}
	}

	/**
	 * 设置用于渲染Freemark看板图表的{@linkplain HtmlChartPluginRenderOption}。
	 * 
	 * @param renderContext
	 * @param dashboardVarName
	 * @return
	 */
	protected HtmlChartPluginRenderOption setHtmlChartPluginRenderOption(RenderContext renderContext,
			String dashboardVarName)
	{
		HtmlChartPluginRenderOption option = new HtmlChartPluginRenderOption();
		option.setNotWriteChartElement(false);
		option.setNotWriteScriptTag(false);
		option.setNotWriteInvoke(true);
		option.setNotWriteRenderContextObject(true);
		option.setRenderContextVarName(dashboardVarName + ".renderContext");

		HtmlChartPluginRenderOption.setOption(renderContext, option);

		return option;
	}

	/**
	 * “@chart”指令。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class ChartTemplateDirectiveModel extends AbstractTemplateDirectiveModel
	{
		public ChartTemplateDirectiveModel()
		{
			super();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
				throws TemplateException, IOException
		{
			String widget = getStringParamValue(params, "widget");
			String var = getStringParamValue(params, "var");
			String elementId = getStringParamValue(params, "elementId");

			HtmlDashboardRenderDataModel dataModel = getHtmlDashboardRenderDataModel(env);
			HtmlDashboard htmlDashboard = dataModel.getHtmlDashboard();
			HtmlRenderContext renderContext = htmlDashboard.getRenderContext();

			HtmlChartWidget<HtmlRenderContext> chartWidget = getHtmlChartWidgetForRender(renderContext, widget);

			if (StringUtil.isEmpty(var))
				var = HtmlRenderAttributes.generateChartVarName(renderContext);

			if (StringUtil.isEmpty(elementId))
				elementId = HtmlRenderAttributes.generateChartElementId(renderContext);

			HtmlChartPluginRenderOption option = HtmlChartPluginRenderOption.getOption(renderContext);
			option.setChartElementId(elementId);
			option.setChartVarName(var);

			HtmlChart chart = chartWidget.render(renderContext);

			List<Chart> charts = htmlDashboard.getCharts();
			if (charts == null)
			{
				charts = new ArrayList<Chart>();
				htmlDashboard.setCharts(charts);
			}

			charts.add(chart);
		}
	}
}
