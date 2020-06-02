/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.Serializable;
import java.io.Writer;
import java.util.Locale;

import org.datagear.analysis.RenderContext;

/**
 * HTML渲染上下文属性定义类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class HtmlRenderAttr implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 属性名：HTML输出流 */
	private String htmlWriterName = "htmlWriter";

	/** 属性名：地区 */
	private String localeName = "locale";

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
}
