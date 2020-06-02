/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.Writer;

import org.datagear.analysis.RenderContext;

/**
 * {@linkplain HtmlChart}渲染上下文属性定义类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartRenderAttr extends HtmlRenderAttr
{
	private static final long serialVersionUID = 1L;

	/**
	 * {@linkplain HtmlChartRenderAttr}的渲染上下文属性名。
	 */
	public static final String NAME = HtmlChartRenderAttr.class.getName();

	/**
	 * 设置{@linkplain HtmlChartRenderAttr}对象。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 */
	public static void set(RenderContext renderContext, HtmlChartRenderAttr renderAttr)
	{
		renderContext.setAttribute(NAME, renderAttr);
	}

	/**
	 * 获取{@linkplain HtmlChartRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr get(RenderContext renderContext)
	{
		return renderContext.getAttribute(NAME);
	}

	/**
	 * 移除{@linkplain HtmlChartRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlChartRenderAttr remove(RenderContext renderContext)
	{
		return renderContext.removeAttribute(NAME);
	}

	/**
	 * 设置{@linkplain HtmlChartWidget#render(HtmlRenderContext)}必须的上下文属性值。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 * @param htmlWriter
	 */
	public static void inflate(RenderContext renderContext, HtmlChartRenderAttr renderAttr, Writer htmlWriter)
	{
		HtmlChartRenderAttr.set(renderContext, renderAttr);
		renderAttr.setHtmlWriter(renderContext, htmlWriter);
	}
}
