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
	private Writer writer;

	private int sequence = 1;

	public DefaultHtmlRenderContext()
	{
		super();
	}

	public DefaultHtmlRenderContext(Writer writer)
	{
		super();
		this.writer = writer;
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
