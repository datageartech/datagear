/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.util.Locale;

import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderStyle;
import org.datagear.util.Global;
import org.datagear.util.StringUtil;

/**
 * HTML渲染属性常量。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlRenderAttributes
{
	public static final String RENDER_STYPE = "renderStyle";

	public static final String DASHBOARD_THEME = "dashboardTheme";

	public static final String CHART_THEME = "chartTheme";

	public static final String LOCALE = "locale";

	/**
	 * 获取{@linkplain RenderStyle}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static RenderStyle getRenderStyle(RenderContext renderContext)
	{
		return renderContext.getAttribute(RENDER_STYPE);
	}

	/**
	 * 设置{@linkplain RenderStyle}。
	 * 
	 * @param renderContext
	 * @param renderStyle
	 */
	public static void setRenderStyle(RenderContext renderContext, RenderStyle renderStyle)
	{
		renderContext.setAttribute(RENDER_STYPE, renderStyle);
	}

	/**
	 * 移除{@linkplain RenderStyle}。
	 * 
	 * @param renderContext
	 */
	public static void removeRenderStyle(RenderContext renderContext)
	{
		renderContext.removeAttribute(RENDER_STYPE);
	}

	/**
	 * 获取{@linkplain DashboardTheme}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static DashboardTheme getDashboardTheme(RenderContext renderContext)
	{
		return renderContext.getAttribute(DASHBOARD_THEME);
	}

	/**
	 * 设置{@linkplain DashboardTheme}。
	 * 
	 * @param renderContext
	 * @param dashboardTheme
	 */
	public static void setDashboardTheme(RenderContext renderContext, DashboardTheme dashboardTheme)
	{
		renderContext.setAttribute(DASHBOARD_THEME, dashboardTheme);
	}

	/**
	 * 移除{@linkplain DashboardTheme}。
	 * 
	 * @param renderContext
	 */
	public static void removeDashboardTheme(RenderContext renderContext)
	{
		renderContext.removeAttribute(DASHBOARD_THEME);
	}

	/**
	 * 获取{@linkplain ChartTheme}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static ChartTheme getChartTheme(RenderContext renderContext)
	{
		return renderContext.getAttribute(CHART_THEME);
	}

	/**
	 * 设置{@linkplain ChartTheme}。
	 * 
	 * @param renderContext
	 * @param chartTheme
	 */
	public static void setChartTheme(RenderContext renderContext, ChartTheme chartTheme)
	{
		renderContext.setAttribute(CHART_THEME, chartTheme);
	}

	/**
	 * 移除{@linkplain ChartTheme}。
	 * 
	 * @param renderContext
	 */
	public static ChartTheme removeChartTheme(RenderContext renderContext)
	{
		return renderContext.removeAttribute(CHART_THEME);
	}

	/**
	 * 获取{@linkplain Locale}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static Locale getLocale(RenderContext renderContext)
	{
		return renderContext.getAttribute(LOCALE);
	}

	/**
	 * 设置{@linkplain Locale}。
	 * 
	 * @param renderContext
	 * @param locale
	 */
	public static void setLocale(RenderContext renderContext, Locale locale)
	{
		renderContext.setAttribute(LOCALE, locale);
	}

	/**
	 * 移除{@linkplain Locale}。
	 * 
	 * @param renderContext
	 */
	public static Locale removeLocale(RenderContext renderContext)
	{
		return renderContext.removeAttribute(LOCALE);
	}

	/**
	 * 生成图表HTML元素ID。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static String generateChartElementId(HtmlRenderContext renderContext)
	{
		return generateChartElementId(Integer.toString(renderContext.nextSequence()));
	}

	/**
	 * 生成图表HTML元素ID。
	 * 
	 * @param suffix
	 * @return
	 */
	public static String generateChartElementId(String suffix)
	{
		return StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "ChartElement" + suffix;
	}

	/**
	 * 生成图表插件变量名。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static String generateChartPluginVarName(HtmlRenderContext renderContext)
	{
		return generateChartPluginVarName(Integer.toString(renderContext.nextSequence()));
	}

	/**
	 * 生成图表插件变量名。
	 * 
	 * @param suffix
	 * @return
	 */
	public static String generateChartPluginVarName(String suffix)
	{
		return StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "ChartPlugin" + suffix;
	}

	/**
	 * 生成图表变量名。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static String generateChartVarName(HtmlRenderContext renderContext)
	{
		return generateChartVarName(Integer.toString(renderContext.nextSequence()));
	}

	/**
	 * 生成图表变量名。
	 * 
	 * @param suffix
	 * @return
	 */
	public static String generateChartVarName(String suffix)
	{
		return StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "Chart" + suffix;
	}

	/**
	 * 生成看板变量名。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static String generateDashboardVarName(HtmlRenderContext renderContext)
	{
		return generateDashboardVarName(Integer.toString(renderContext.nextSequence()));
	}

	/**
	 * 生成看板变量名。
	 * 
	 * @param suffix
	 * @return
	 */
	public static String generateDashboardVarName(String suffix)
	{
		return StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "Dashboard" + suffix;
	}

	/**
	 * 生成{@linkplain RenderContext}变量名。
	 * 
	 * @param renderContext
	 * @return
	 */
	public static String generateRenderContextVarName(HtmlRenderContext renderContext)
	{
		return generateRenderContextVarName(Integer.toString(renderContext.nextSequence()));
	}

	/**
	 * 生成{@linkplain RenderContext}变量名。
	 * 
	 * @param suffix
	 * @return
	 */
	public static String generateRenderContextVarName(String suffix)
	{
		return StringUtil.firstLowerCase(Global.PRODUCT_NAME_EN) + "RenderContext" + suffix;
	}
}
