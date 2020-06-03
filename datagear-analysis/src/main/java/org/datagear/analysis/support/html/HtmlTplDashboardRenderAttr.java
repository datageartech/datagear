/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.Writer;

import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.HtmlTitleHandler;

/**
 * {@linkplain HtmlTplDashboard}渲染上下文属性定义类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardRenderAttr extends HtmlRenderAttr
{
	private static final long serialVersionUID = 1L;

	/**
	 * {@linkplain HtmlTplDashboardRenderAttr}的渲染上下文属性名。
	 */
	public static final String ATTR_NAME = HtmlTplDashboardRenderAttr.class.getName();

	/** 属性名：Web上下文 */
	private String webContextName = "webContext";

	/** 属性名：渲染风格 */
	private String renderStyleName = "renderStyle";

	/** 属性名：看板主题 */
	private String dashboardThemeName = "dashboardTheme";

	/** 属性名：HTML标题处理器 */
	private String htmlTitleHandlerName = "htmlTitleHandler";

	public HtmlTplDashboardRenderAttr()
	{
		super();
	}

	public String getWebContextName()
	{
		return webContextName;
	}

	public void setWebContextName(String webContextName)
	{
		this.webContextName = webContextName;
	}

	public String getRenderStyleName()
	{
		return renderStyleName;
	}

	public void setRenderStyleName(String renderStyleName)
	{
		this.renderStyleName = renderStyleName;
	}

	public String getDashboardThemeName()
	{
		return dashboardThemeName;
	}

	public void setDashboardThemeName(String dashboardThemeName)
	{
		this.dashboardThemeName = dashboardThemeName;
	}

	public String getHtmlTitleHandlerName()
	{
		return htmlTitleHandlerName;
	}

	public void setHtmlTitleHandlerName(String htmlTitleHandlerName)
	{
		this.htmlTitleHandlerName = htmlTitleHandlerName;
	}

	/**
	 * 获取{@linkplain WebContext}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public WebContext getWebContextNonNull(RenderContext renderContext)
	{
		WebContext webContext = getWebContext(renderContext);

		if (webContext == null)
			throw new RenderException("The [" + this.webContextName + "] attribute must be set");

		return webContext;
	}

	/**
	 * 获取{@linkplain WebContext}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public WebContext getWebContext(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.webContextName);
	}

	/**
	 * 设置{@linkplain WebContext}。
	 * 
	 * @param renderContext
	 * @param renderStyle
	 */
	public void setWebContext(RenderContext renderContext, WebContext webContext)
	{
		renderContext.setAttribute(this.webContextName, webContext);
	}

	/**
	 * 移除{@linkplain WebContext}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public WebContext removeWebContext(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.webContextName);
	}

	/**
	 * 获取{@linkplain RenderStyle}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public RenderStyle getRenderStyle(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.renderStyleName);
	}

	/**
	 * 设置{@linkplain RenderStyle}。
	 * 
	 * @param renderContext
	 * @param renderStyle
	 */
	public void setRenderStyle(RenderContext renderContext, RenderStyle renderStyle)
	{
		renderContext.setAttribute(this.renderStyleName, renderStyle);
	}

	/**
	 * 移除{@linkplain RenderStyle}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public RenderStyle removeRenderStyle(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.renderStyleName);
	}

	/**
	 * 获取{@linkplain DashboardTheme}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public DashboardTheme getDashboardTheme(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.dashboardThemeName);
	}

	/**
	 * 设置{@linkplain DashboardTheme}。
	 * 
	 * @param renderContext
	 * @param dashboardTheme
	 */
	public void setDashboardTheme(RenderContext renderContext, DashboardTheme dashboardTheme)
	{
		renderContext.setAttribute(this.dashboardThemeName, dashboardTheme);
	}

	/**
	 * 移除{@linkplain DashboardTheme}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public DashboardTheme removeDashboardTheme(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.dashboardThemeName);
	}

	/**
	 * 获取{@linkplain HtmlTitleHandler}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public HtmlTitleHandler getHtmlTitleHandler(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.htmlTitleHandlerName);
	}

	/**
	 * 设置{@linkplain HtmlTitleHandler}。
	 * 
	 * @param renderContext
	 * @param htmlTitleHandler
	 */
	public void setHtmlTitleHandler(RenderContext renderContext, HtmlTitleHandler htmlTitleHandler)
	{
		renderContext.setAttribute(this.htmlTitleHandlerName, htmlTitleHandler);
	}

	/**
	 * 移除{@linkplain HtmlTitleHandler}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public HtmlTitleHandler removeHtmlTitleHandler(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.htmlTitleHandlerName);
	}

	/**
	 * 设置{@linkplain HtmlTplDashboardWidget#render(RenderContext)}必须的上下文属性值。
	 * 
	 * @param renderContext
	 * @param htmlWriter
	 * @param webContext
	 */
	public void inflate(RenderContext renderContext, Writer htmlWriter, WebContext webContext)
	{
		HtmlTplDashboardRenderAttr.set(renderContext, this);
		setHtmlWriter(renderContext, htmlWriter);
		setWebContext(renderContext, webContext);
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr getNonNull(RenderContext renderContext)
	{
		HtmlTplDashboardRenderAttr renderAttr = get(renderContext);

		if (renderAttr == null)
			throw new RenderException("The [" + ATTR_NAME + "] attribute must be set");

		return renderAttr;
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardRenderAttr}对象，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr get(RenderContext renderContext)
	{
		return renderContext.getAttribute(ATTR_NAME);
	}

	/**
	 * 设置{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 */
	public static void set(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr)
	{
		renderContext.setAttribute(ATTR_NAME, renderAttr);
	}

	/**
	 * 移除{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr remove(RenderContext renderContext)
	{
		return renderContext.removeAttribute(ATTR_NAME);
	}

	/**
	 * Web上下文信息。
	 * <p>
	 * 这些信息可以输出值客户端，提供看板交互支持。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class WebContext
	{
		/** 上下文路径 */
		private String contextPath;

		/** 更新看板数据的URL */
		private String updateDashboardURL;

		/** 更新看板数据的的看板ID参数名 */
		private String dashboardIdParam = "dashboardId";

		/** 更新看板数据的图表集参数名 */
		private String chartIdsParam = "chartsId";

		/** 更新看板数据的图表集参数值的参数名 */
		private String chartsParamValuesParam = "chartsParamValues";

		public WebContext()
		{
			super();
		}

		public WebContext(String contextPath, String updateDashboardURL)
		{
			super();
			this.contextPath = contextPath;
			this.updateDashboardURL = updateDashboardURL;
		}

		public String getContextPath()
		{
			return contextPath;
		}

		public void setContextPath(String contextPath)
		{
			this.contextPath = contextPath;
		}

		public String getUpdateDashboardURL()
		{
			return updateDashboardURL;
		}

		public void setUpdateDashboardURL(String updateDashboardURL)
		{
			this.updateDashboardURL = updateDashboardURL;
		}

		public String getDashboardIdParam()
		{
			return dashboardIdParam;
		}

		public void setDashboardIdParam(String dashboardIdParam)
		{
			this.dashboardIdParam = dashboardIdParam;
		}

		public String getChartIdsParam()
		{
			return chartIdsParam;
		}

		public void setChartIdsParam(String chartIdsParam)
		{
			this.chartIdsParam = chartIdsParam;
		}

		public String getChartsParamValuesParam()
		{
			return chartsParamValuesParam;
		}

		public void setChartsParamValuesParam(String chartsParamValuesParam)
		{
			this.chartsParamValuesParam = chartsParamValuesParam;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [contextPath=" + contextPath + ", updateDashboardURL="
					+ updateDashboardURL + ", dashboardIdParam=" + dashboardIdParam + ", chartIdsParam=" + chartIdsParam
					+ ", chartsParamValuesParam=" + chartsParamValuesParam + "]";
		}
	}
}
