/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.Writer;

import org.datagear.analysis.support.AbstractRenderContext;

/**
 * 默认{@linkplain HtmlRenderContext}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultHtmlRenderContext extends AbstractRenderContext implements HtmlRenderContext
{
	private WebContext webContext;

	private Writer writer;

	private int sequence = 1;

	public DefaultHtmlRenderContext()
	{
		super();
	}

	public DefaultHtmlRenderContext(WebContext webContext, Writer writer)
	{
		super();
		this.webContext = webContext;
		this.writer = writer;
	}

	@Override
	public WebContext getWebContext()
	{
		return webContext;
	}

	public void setWebContext(WebContext webContext)
	{
		this.webContext = webContext;
	}

	@Override
	public Writer getWriter()
	{
		return this.writer;
	}

	public void setWriter(Writer writer)
	{
		this.writer = writer;
	}

	@Override
	public int nextSequence()
	{
		return this.sequence++;
	}
}
