/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.html;

import java.io.IOException;
import java.io.Writer;

/**
 * 空{@linkplain Writer}。
 * <p>
 * 它不执行任何实际的写操作，
 * 当{@linkplain HtmlFilter#filter(java.io.Reader, Writer)}、{@linkplain HtmlFilter#filter(java.io.Reader, Writer, TagListener)}不需要写操作时，可以使用此类替代。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class NopWriter extends Writer
{
	public static final Writer NOP_WRITER = new NopWriter();

	public NopWriter()
	{
		super();
	}

	@Override
	public void write(int c) throws IOException
	{
	}

	@Override
	public void write(char[] cbuf) throws IOException
	{
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
	}

	@Override
	public void write(String str) throws IOException
	{
	}

	@Override
	public void write(String str, int off, int len) throws IOException
	{
	}

	@Override
	public Writer append(CharSequence csq) throws IOException
	{
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException
	{
		return this;
	}

	@Override
	public Writer append(char c) throws IOException
	{
		return this;
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
