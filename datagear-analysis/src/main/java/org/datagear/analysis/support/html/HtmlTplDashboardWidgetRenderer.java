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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * {@linkplain HtmlTplDashboardWidget}渲染器。
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
 * &lt;@dashboard var="..." listener="..."&gt;
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
 * &lt;@dashboard&gt;：定义看板，“var”自定义看板JS变量名，可不填；“listener”自定义看板JS监听器，可不填。
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
public class HtmlTplDashboardWidgetRenderer<T extends HtmlRenderContext>
{
	public static final String DIRECTIVE_IMPORT = "import";

	public static final String DIRECTIVE_THEME = "theme";

	public static final String DIRECTIVE_DASHBOARD = "dashboard";

	public static final String DIRECTIVE_CHART = "chart";

	public static final String DASHBOARD_ELEMENT_STYLE_NAME = "dashboard";

	public static final String CHART_ELEMENT_WRAPPER_STYLE_NAME = "chart-wrapper";

	public static final String DEFAULT_IMPORT_CONTEXT_PATH_PLACE_HOLDER = "$CONTEXTPATH";

	protected static final String KEY_HTML_DASHBOARD_RENDER_DATA_MODEL = HtmlDashboardRenderDataModel.class
			.getSimpleName();

	protected static final String RENDER_ATTR_NAME_FOR_NOT_FOUND_SCRIPT = StringUtil
			.firstLowerCase(Global.PRODUCT_NAME_EN) + "RenderValueForNotFound";

	/** "@import"指令的输出内容 */
	private String importContent;

	/** "@import"指令的输出内容需要替换的上下文路径占位符 */
	private String importContentContextPathPlaceholder = DEFAULT_IMPORT_CONTEXT_PATH_PLACE_HOLDER;

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

	private boolean ignoreDashboardStyleBorderWidth = true;

	private Configuration _configuration;

	public HtmlTplDashboardWidgetRenderer()
	{
		super();
	}

	public HtmlTplDashboardWidgetRenderer(String importContent, DashboardWidgetResManager dashboardWidgetResManager,
			ChartWidgetSource chartWidgetSource)
	{
		super();
		this.importContent = importContent;
		this.dashboardWidgetResManager = dashboardWidgetResManager;
		this.chartWidgetSource = chartWidgetSource;
	}

	public String getImportContent()
	{
		return importContent;
	}

	public void setImportContent(String importContent)
	{
		this.importContent = importContent;
	}

	public String getImportContentContextPathPlaceholder()
	{
		return importContentContextPathPlaceholder;
	}

	public void setImportContentContextPathPlaceholder(String importContentContextPathPlaceholder)
	{
		this.importContentContextPathPlaceholder = importContentContextPathPlaceholder;
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
		cfg.setDirectoryForTemplateLoading(this.dashboardWidgetResManager.getRootDirectory());
		cfg.setDefaultEncoding(this.templateEncoding);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);

		cfg.setSharedVariable(DIRECTIVE_IMPORT, new ImportTemplateDirectiveModel());
		cfg.setSharedVariable(DIRECTIVE_THEME, new ThemeTemplateDirectiveModel());
		cfg.setSharedVariable(DIRECTIVE_DASHBOARD, new DashboardTemplateDirectiveModel());
		cfg.setSharedVariable(DIRECTIVE_CHART, new ChartTemplateDirectiveModel());

		setConfiguration(cfg);
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

		Template template = getTemplate(dashboardWidget);

		HtmlDashboard dashboard = new HtmlDashboard();

		dashboard.setId(IDUtil.uuid());
		dashboard.setWidget(dashboardWidget);
		dashboard.setRenderContext(renderContext);
		dashboard.setCharts(new ArrayList<Chart>());

		HtmlDashboardRenderDataModel dataModel = new HtmlDashboardRenderDataModel(dashboard,
				renderContext.getContextPath());

		try
		{
			template.process(buildHtmlDashboardRenderDataModel(dataModel), renderContext.getWriter());
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
	 * 获取{@linkplain HtmlTplDashboardWidget#getId()}的指定模板对象。
	 * 
	 * @param dashboardWidget
	 * @return
	 * @throws RenderException
	 */
	protected Template getTemplate(HtmlTplDashboardWidget<T> dashboardWidget) throws RenderException
	{
		String path = this.dashboardWidgetResManager.getRelativePath(dashboardWidget.getId(),
				dashboardWidget.getTemplate());

		try
		{
			return getConfiguration().getTemplate(path);
		}
		catch (Throwable t)
		{
			throw new RenderException(t);
		}
	}

	protected Configuration getConfiguration()
	{
		return _configuration;
	}

	protected void setConfiguration(Configuration _configuration)
	{
		this._configuration = _configuration;
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

		return str.replace(getImportContentContextPathPlaceholder(), contextPath);
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

			Writer out = env.getOut();

			String importContent = getImportContent();
			if (importContent == null)
				importContent = "";

			out.write("<meta charset=\"" + getWriterEncoding() + "\">");

			if (!StringUtil.isEmpty(importContent))
			{
				importContent = replaceContextPathPlaceholder(importContent, dataModel.getContextPath());
				writeNewLine(out);
				out.write(importContent);
				writeNewLine(out);
			}
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
			HtmlRenderContext renderContext = dataModel.getHtmlDashboard().getRenderContext();

			Writer out = env.getOut();

			DashboardTheme dashboardTheme = getDashboardTheme(renderContext);
			writeDashboardTheme(out, dashboardTheme);
		}

		protected DashboardTheme getDashboardTheme(RenderContext renderContext)
		{
			DashboardTheme dashboardTheme = HtmlRenderAttributes.getDashboardTheme(renderContext);
			return dashboardTheme;
		}

		protected void writeDashboardTheme(Writer out, DashboardTheme dashboardTheme) throws IOException
		{
			ChartTheme chartTheme = (dashboardTheme == null ? null : dashboardTheme.getChartTheme());

			out.write("<style type=\"text/css\">");
			writeNewLine(out);
			out.write("body{");
			writeNewLine(out);
			out.write("    padding: 0px 0px;");
			writeNewLine(out);
			out.write("    margin: 0px 0px;");
			writeNewLine(out);
			if (dashboardTheme != null)
			{
				out.write("    background-color: " + dashboardTheme.getBackgroundColor() + ";");
				writeNewLine(out);
				out.write("    color: " + dashboardTheme.getForegroundColor() + ";");
				writeNewLine(out);
			}
			out.write("}");
			writeNewLine(out);
			out.write("." + DASHBOARD_ELEMENT_STYLE_NAME + "{");
			writeNewLine(out);
			writeThemeCssAttrs(out, dashboardTheme);
			writeBorderBoxCssAttrs(out);
			if (isIgnoreDashboardStyleBorderWidth())
			{
				out.write("    border-width: 0px;");
				writeNewLine(out);
			}
			out.write("}");
			writeNewLine(out);

			out.write("." + CHART_ELEMENT_WRAPPER_STYLE_NAME + "{");
			writeNewLine(out);
			out.write("    position: relative;");
			writeNewLine(out);
			writeThemeCssAttrs(out, chartTheme);
			writeBorderBoxCssAttrs(out);
			out.write("}");
			writeNewLine(out);

			out.write("." + HtmlChartPlugin.BUILTIN_CHART_ELEMENT_STYLE_NAME + "{");
			writeNewLine(out);
			writeFillParentCssAttrs(out);
			writeBorderBoxCssAttrs(out);
			out.write("}");
			writeNewLine(out);

			out.write("</style>");
			writeNewLine(out);
		}

		protected void writeThemeCssAttrs(Writer out, Theme theme) throws IOException
		{
			if (theme != null)
			{
				String borderWidth = theme.getBorderWidth();
				if (borderWidth == null)
					borderWidth = "0";

				out.write("    color: " + theme.getForegroundColor() + ";");
				writeNewLine(out);
				out.write("    background-color: " + theme.getBackgroundColor() + ";");
				writeNewLine(out);
				out.write("    border-color: " + theme.getBorderColor() + ";");
				writeNewLine(out);
				out.write("    border-width: " + theme.getBorderWidth() + ";");
				writeNewLine(out);
				out.write("    border-style: solid;");
				writeNewLine(out);
			}
		}

		protected void writeFillParentCssAttrs(Writer out) throws IOException
		{
			out.write("    position: absolute;");
			writeNewLine(out);
			out.write("    top: 0px;");
			writeNewLine(out);
			out.write("    bottom: 0px;");
			writeNewLine(out);
			out.write("    left: 0px;");
			writeNewLine(out);
			out.write("    right: 0px;");
			writeNewLine(out);
		}

		protected void writeBorderBoxCssAttrs(Writer out) throws IOException
		{
			out.write("    box-sizing: border-box;");
			writeNewLine(out);
			out.write("    -moz-box-sizing: border-box;");
			writeNewLine(out);
			out.write("    -webkit-box-sizing: border-box;");
			writeNewLine(out);
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
			String varName = getStringParamValue(params, "var");
			String listener = getStringParamValue(params, "listener");
			boolean hasListener = !StringUtil.isEmpty(listener);

			HtmlDashboardRenderDataModel dataModel = getHtmlDashboardRenderDataModel(env);
			HtmlDashboard dashboard = dataModel.getHtmlDashboard();
			HtmlRenderContext renderContext = dashboard.getRenderContext();
			int nextSequence = -1;

			if (StringUtil.isEmpty(varName))
			{
				nextSequence = HtmlRenderAttributes.getNextSequenceIfNot(renderContext, nextSequence);
				varName = HtmlRenderAttributes.generateDashboardVarName(nextSequence);
			}

			dashboard.setVarName(varName);

			Writer out = env.getOut();

			writeScriptStartTag(out);
			writeNewLine(out);

			out.write("var ");
			out.write(varName);
			out.write("=");
			writeNewLine(out);
			writeHtmlDashboardScriptObject(out, dashboard, true);
			out.write(";");
			writeNewLine(out);

			writeScriptEndTag(out);
			writeNewLine(out);

			HtmlRenderAttributes.setChartRenderContextVarName(renderContext, varName + ".renderContext");

			if (body != null)
				body.render(out);

			writeScriptStartTag(out);
			writeNewLine(out);

			String tmpRenderContextVar = HtmlRenderAttributes.generateRenderContextVarName(nextSequence);

			// 移除内部设置的属性
			HtmlRenderAttributes.removeChartRenderContextVarName(renderContext);
			HtmlRenderAttributes.removeChartNotRenderScriptTag(renderContext);
			HtmlRenderAttributes.removeChartScriptNotInvokeRender(renderContext);
			HtmlRenderAttributes.removeChartVarName(renderContext);
			HtmlRenderAttributes.removeChartElementId(renderContext);

			renderContext.removeAttribute(RENDER_ATTR_NAME_FOR_NOT_FOUND_SCRIPT);

			out.write("var ");
			out.write(tmpRenderContextVar);
			out.write("=");
			writeNewLine(out);
			writeRenderContextScriptObject(out, renderContext);
			out.write(";");
			writeNewLine(out);
			out.write(varName + ".renderContext.attributes = " + tmpRenderContextVar + ".attributes;");
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

			out.write(varName + ".render = function(){");
			writeNewLine(out);
			out.write(" for(var i=0; i<this.charts.length; i++){ this.charts[i].render(); }");
			writeNewLine(out);
			out.write("};");
			writeNewLine(out);

			out.write(varName + ".update = function(){");
			writeNewLine(out);
			out.write(" for(var i=0; i<this.charts.length; i++){ this.charts[i].update(); }");
			writeNewLine(out);
			out.write("};");
			writeNewLine(out);

			if (hasListener)
			{
				out.write(varName + ".listener = window[\"" + listener + "\"];");
				writeNewLine(out);
			}

			out.write("window.onload = function(){");
			writeNewLine(out);

			if (hasListener)
			{
				out.write("if(" + varName + ".listener && " + varName + ".listener.beforeRender)");
				writeNewLine(out);
				out.write("  " + varName + ".listener.beforeRender(" + varName + "); ");
				writeNewLine(out);
			}

			out.write(varName + ".render();");
			writeNewLine(out);

			if (hasListener)
			{
				out.write("if(" + varName + ".listener && " + varName + ".listener.afterRender)");
				writeNewLine(out);
				out.write("  " + varName + ".listener.afterRender(" + varName + "); ");
				writeNewLine(out);
			}

			if (hasListener)
			{
				out.write("if(" + varName + ".listener && " + varName + ".listener.beforeUpdate)");
				writeNewLine(out);
				out.write("  " + varName + ".listener.beforeUpdate(" + varName + "); ");
				writeNewLine(out);
			}

			out.write(varName + ".update();");
			writeNewLine(out);

			if (hasListener)
			{
				out.write("if(" + varName + ".listener && " + varName + ".listener.afterUpdate)");
				writeNewLine(out);
				out.write("  " + varName + ".listener.afterUpdate(" + varName + "); ");
				writeNewLine(out);
			}

			out.write("};");
			writeNewLine(out);

			writeScriptEndTag(out);
			writeNewLine(out);
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

		@SuppressWarnings({ "rawtypes", "unchecked" })
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
			int nextSequence = -1;

			HtmlChartWidget<HtmlRenderContext> chartWidget = getHtmlChartWidgetForRender(renderContext, widget);

			if (StringUtil.isEmpty(var))
			{
				nextSequence = HtmlRenderAttributes.getNextSequenceIfNot(renderContext, nextSequence);
				var = HtmlRenderAttributes.generateChartVarName(nextSequence);
			}

			if (StringUtil.isEmpty(elementId))
			{
				nextSequence = HtmlRenderAttributes.getNextSequenceIfNot(renderContext, nextSequence);
				elementId = HtmlRenderAttributes.generateChartElementId(nextSequence);
			}

			HtmlRenderAttributes.setChartNotRenderScriptTag(renderContext, false);
			HtmlRenderAttributes.setChartScriptNotInvokeRender(renderContext, true);
			HtmlRenderAttributes.setChartVarName(renderContext, var);
			HtmlRenderAttributes.setChartElementId(renderContext, elementId);

			HtmlChart chart = chartWidget.render(renderContext);

			List<HtmlChart> charts = (List<HtmlChart>) htmlDashboard.getCharts();
			if (charts == null)
			{
				charts = new ArrayList<HtmlChart>();
				htmlDashboard.setCharts(charts);
			}

			charts.add(chart);
		}

		/**
		 * 写脚本开始标签。
		 * 
		 * @param out
		 * @throws IOException
		 */
		@Override
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
		@Override
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
		@Override
		protected void writeNewLine(Writer out) throws IOException
		{
			out.write(getNewLine());
		}
	}
}
