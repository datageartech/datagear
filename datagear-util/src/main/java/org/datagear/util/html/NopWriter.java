/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util.html;

import java.io.IOException;
import java.io.Writer;

/**
 * 空{@linkplain Writer}。
 * <p>
 * 它不执行任何实际的写操作，可用于构建无输出的{@linkplain FilterHandler}。
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
