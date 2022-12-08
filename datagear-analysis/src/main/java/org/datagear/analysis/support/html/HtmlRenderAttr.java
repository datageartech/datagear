/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.Writer;
import java.util.Collection;
import java.util.Locale;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.util.Global;
import org.datagear.util.StringUtil;

/**
 * HTML渲染上下文属性定义类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class HtmlRenderAttr
{
	/** 属性名：HTML输出流 */
	private String htmlWriterName = "htmlWriter";

	/** 属性名：地区 */
	private String localeName = "locale";

	/** 属性名：忽略输出的属性集名 */
	private String ignoreRenderAttrsName = "ignoreRenderAttrs";

	/** 生成名称、ID的种子 */
	private String seed = null;

	public HtmlRenderAttr()
	{
		super();
	}

	public String getHtmlWriterName()
	{
		return htmlWriterName;
	}

	public void setHtmlWriterName(String htmlWriterName)
	{
		this.htmlWriterName = htmlWriterName;
	}

	public String getLocaleName()
	{
		return localeName;
	}

	public void setLocaleName(String localeName)
	{
		this.localeName = localeName;
	}

	public String getIgnoreRenderAttrsName()
	{
		return ignoreRenderAttrsName;
	}

	public void setIgnoreRenderAttrsName(String ignoreRenderAttrsName)
	{
		this.ignoreRenderAttrsName = ignoreRenderAttrsName;
	}

	/**
	 * 获取HTML输出流，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public Writer getHtmlWriterNonNull(RenderContext renderContext)
	{
		Writer out = renderContext.getAttribute(this.htmlWriterName);

		if (out == null)
			throw new RenderException("The [" + this.htmlWriterName + "] attribute must be set");

		return out;
	}

	/**
	 * 获取HTML输出流，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public Writer getHtmlWriter(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.htmlWriterName);
	}

	/**
	 * 设置HTML输出流。
	 * 
	 * @param renderContext
	 * @param htmlWriter
	 */
	public void setHtmlWriter(RenderContext renderContext, Writer htmlWriter)
	{
		renderContext.setAttribute(this.htmlWriterName, htmlWriter);
	}

	/**
	 * 移除HTML输出流。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public Writer removeHtmlWriter(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.htmlWriterName);
	}

	/**
	 * 获取{@linkplain Locale}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public Locale getLocale(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.localeName);
	}

	/**
	 * 设置{@linkplain Locale}。
	 * 
	 * @param renderContext
	 * @param locale
	 */
	public void setLocale(RenderContext renderContext, Locale locale)
	{
		renderContext.setAttribute(this.localeName, locale);
	}

	/**
	 * 移除{@linkplain Locale}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public Locale removeLocale(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.localeName);
	}

	/**
	 * 获取忽略渲染的{@linkplain RenderContext#getAttribute(String)}集合，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public Collection<String> getIgnoreRenderAttrs(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.ignoreRenderAttrsName);
	}

	/**
	 * 设置忽略渲染的{@linkplain RenderContext#getAttribute(String)}集合。
	 * 
	 * @param renderContext
	 * @param ignoreRenderAttrs
	 */
	public void setIgnoreRenderAttrs(RenderContext renderContext, Collection<String> ignoreRenderAttrs)
	{
		renderContext.setAttribute(this.ignoreRenderAttrsName, ignoreRenderAttrs);
	}

	/**
	 * 移除忽略渲染的{@linkplain RenderContext#getAttribute(String)}集合。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public Collection<String> removeIgnoreRenderAttrs(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.ignoreRenderAttrsName);
	}

	/**
	 * 获取种子。
	 * 
	 * @return 返回{@code null}表示无种子
	 */
	public String getSeed()
	{
		return seed;
	}

	public void setSeed(String seed)
	{
		this.seed = seed;
	}

	/**
	 * 生成默认{@linkplain RenderContext}变量名。
	 * 
	 * @param suffix
	 * @return
	 */
	public String genRenderContextVarName()
	{
		return genRenderContextVarName(null);
	}

	/**
	 * 生成{@linkplain RenderContext}变量名。
	 * 
	 * @param suffix
	 * @return
	 */
	public String genRenderContextVarName(String suffix)
	{
		return genIdentifier("RenderContext", suffix);
	}

	/**
	 * 生成默认图表插件变量名。
	 * 
	 * @return
	 */
	public String genChartPluginVarName()
	{
		return genChartPluginVarName(null);
	}

	/**
	 * 生成图表插件变量名。
	 * 
	 * @param suffix
	 *            允许为{@code null}
	 * @return
	 */
	public String genChartPluginVarName(String suffix)
	{
		return genIdentifier("ChartPlugin", suffix);
	}

	/**
	 * 生成默认图表HTML元素ID。
	 * 
	 * @return
	 */
	public String genChartElementId()
	{
		return genChartElementId(null);
	}

	/**
	 * 生成图表HTML元素ID。
	 * 
	 * @param suffix
	 *            允许为{@code null}
	 * @return
	 */
	public String genChartElementId(String suffix)
	{
		return genIdentifier("chartele", suffix);
	}

	/**
	 * 生成默认图表变量名。
	 * 
	 * @return
	 */
	public String genChartVarName()
	{
		return genChartVarName(null);
	}

	/**
	 * 生成图表变量名。
	 * 
	 * @param suffix
	 *            允许为{@code null}
	 * @return
	 */
	public String genChartVarName(String suffix)
	{
		return genIdentifier("Chart", suffix);
	}

	/**
	 * 生成默认看板变量名。
	 * 
	 * @return
	 */
	public String genDashboardVarName()
	{
		return genDashboardVarName(null);
	}

	/**
	 * 生成看板变量名。
	 * 
	 * @param suffix
	 *            允许为{@code null}
	 * @return
	 */
	public String genDashboardVarName(String suffix)
	{
		return genIdentifier("Dashboard", suffix);
	}

	/**
	 * 生成标识。
	 * 
	 * @param prefix
	 * @param suffix 允许为{@code null}
	 * @return
	 */
	protected String genIdentifier(String prefix, String suffix)
	{
		StringBuilder sb = new StringBuilder(Global.PRODUCT_NAME_EN_LC);

		if (!StringUtil.isEmpty(this.seed))
			sb.append(this.seed);

		sb.append(prefix);

		if (!StringUtil.isEmpty(suffix))
			sb.append(suffix);

		return sb.toString();
	}
}
