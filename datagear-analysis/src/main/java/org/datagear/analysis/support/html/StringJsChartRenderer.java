/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

	private String codeType;

	private String codeValue;

	public StringJsChartRenderer()
	{
		super();
	}

	public StringJsChartRenderer(String codeType, String codeValue)
	{
		super();
		this.codeType = codeType;
		this.codeValue = codeValue;
	}

	@Override
	public String getCodeType()
	{
		return codeType;
	}

	public void setCodeType(String codeType)
	{
		this.codeType = codeType;
	}

	public String getCodeValue()
	{
		return codeValue;
	}

	public void setCodeValue(String codeValue)
	{
		this.codeValue = codeValue;
	}

	@Override
	public Reader getCodeReader() throws IOException
	{
		return new StringReader(this.codeValue);
	}
}
