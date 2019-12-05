/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderStyle;

/**
 * {@linkplain RenderContext}常用工具类。
 * 
 * @author datagear@163.com
 *
 */
public class RenderContextUtil
{
	public static final String ATTR_RENDER_STYPE = RenderStyle.class.getName();

	public static final String ATTR_CHART_THEME = ChartTheme.class.getName();

	/**
	 * 获取{@linkplain RenderStyle}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static RenderStyle getRenderStyle(RenderContext renderContext)
	{
		return renderContext.getAttribute(ATTR_RENDER_STYPE);
	}

	/**
	 * 设置{@linkplain RenderStyle}。
	 * 
	 * @param renderContext
	 * @param renderStyle
	 */
	public static void setRenderStyle(RenderContext renderContext, RenderStyle renderStyle)
	{
		renderContext.setAttribute(ATTR_RENDER_STYPE, renderStyle);
	}

	/**
	 * 获取{@linkplain ChartTheme}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static ChartTheme getChartTheme(RenderContext renderContext)
	{
		return renderContext.getAttribute(ATTR_RENDER_STYPE);
	}

	/**
	 * 设置{@linkplain ChartTheme}。
	 * 
	 * @param renderContext
	 * @param chartTheme
	 */
	public static void setChartTheme(RenderContext renderContext, ChartTheme chartTheme)
	{
		renderContext.setAttribute(ATTR_RENDER_STYPE, chartTheme);
	}
}
