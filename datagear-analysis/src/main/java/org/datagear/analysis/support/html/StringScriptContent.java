/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
 * 字符串{@linkplain ScriptContent}。
 * 
 * @author datagear@163.com
 *
 */
public class StringScriptContent implements ScriptContent, Serializable
{
	private static final long serialVersionUID = 1L;

	private String content;

	public StringScriptContent()
	{
		super();
	}

	public StringScriptContent(String content)
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
