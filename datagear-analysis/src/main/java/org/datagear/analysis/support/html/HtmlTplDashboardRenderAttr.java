/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.Writer;

import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.RenderContext;
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
	public static final String NAME = HtmlTplDashboardRenderAttr.class.getName();

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
	 * 设置{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 */
	public static void set(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr)
	{
		renderContext.setAttribute(NAME, renderAttr);
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr get(RenderContext renderContext)
	{
		return renderContext.getAttribute(NAME);
	}

	/**
	 * 移除{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr remove(RenderContext renderContext)
	{
		return renderContext.removeAttribute(NAME);
	}

	/**
	 * 设置{@linkplain HtmlTplDashboardWidget#render(HtmlRenderContext)}必须的上下文属性值。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param htmlWriter
	 */
	public static void inflate(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr, Writer htmlWriter)
	{
		HtmlTplDashboardRenderAttr.set(renderContext, renderAttr);
		renderAttr.setHtmlWriter(renderContext, htmlWriter);
	}
}
