/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

import java.io.IOException;
import java.io.Writer;

/**
 * 基于{@linkplain StringBuilder}的{@linkplain Writer}。
 * 
 * @author datagear@163.com
 *
 */
public class StringBuilderWriter extends Writer
{
	private StringBuilder out = new StringBuilder();

	public StringBuilderWriter()
	{
		super();
	}

	public StringBuilderWriter(StringBuilder out)
	{
		super();
		this.out = out;
	}

	public StringBuilder getOut()
	{
		return out;
	}

	public void setOut(StringBuilder out)
	{
		this.out = out;
	}

	/**
	 * 获取输出字符串。
	 * 
	 * @return
	 */
	public String getString()
	{
		return this.out.toString();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		this.out.append(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException
	{
	}

	@Override
	public void close() throws IOException
	{
	}
}
