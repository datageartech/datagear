/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * 字符串{@linkplain JsChartRenderer}。
 * 
 * @author datagear@163.com
 *
 */
public class StringJsChartRenderer implements JsChartRenderer
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
