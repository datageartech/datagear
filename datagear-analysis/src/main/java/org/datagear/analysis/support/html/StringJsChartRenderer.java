/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

/**
 * 字符串{@linkplain JsChartRenderer}。
 * 
 * @author datagear@163.com
 *
 */
public class StringJsChartRenderer implements JsChartRenderer, Serializable
{
	private static final long serialVersionUID = 1L;

	private String content;

	public StringJsChartRenderer()
	{
		super();
	}

	public StringJsChartRenderer(String content)
	{
		super();
		this.content = content;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	@Override
	public Reader getReader() throws IOException
	{
		return new StringReader(this.content);
	}
}
