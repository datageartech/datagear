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

package org.datagear.util.html;

import java.io.IOException;
import java.io.Writer;

/**
 * 复制输出流。
 * <p>
 * 通过{@linkplain #setCopy(boolean)}可控制写入{@linkplain #getOut()}的内容可同时写入{@linkplain #getCopyOut()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CopyWriter extends Writer
{
	private Writer out;

	private Writer copyOut;

	private boolean copy;

	public CopyWriter(Writer out, Writer copyOut, boolean copy)
	{
		super();
		this.out = out;
		this.copyOut = copyOut;
		this.copy = copy;
	}

	public CopyWriter(Writer out, Writer copyOut, boolean copy, Object lock)
	{
		super(lock);
		this.out = out;
		this.copyOut = copyOut;
		this.copy = copy;
	}

	/**
	 * 获取实际输出流。
	 * 
	 * @return
	 */
	public Writer getOut()
	{
		return out;
	}

	public void setOut(Writer out)
	{
		this.out = out;
	}

	/**
	 * 获取输出复制内容。
	 * <p>
	 * 只会在{@linkplain #isCopy()}为{@code true}时写入内容。
	 * </p>
	 * 
	 * @return
	 */
	public Writer getCopyOut()
	{
		return copyOut;
	}

	public void setCopyOut(Writer copyOut)
	{
		this.copyOut = copyOut;
	}

	/**
	 * 是否处于输出复制状态。
	 * <p>
	 * 为{@code true}时，{@linkplain #getCopyOut()}里才会写入内容。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isCopy()
	{
		return copy;
	}

	public void setCopy(boolean copy)
	{
		this.copy = copy;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		this.out.write(cbuf, off, len);

		if (this.copy)
			this.copyOut.write(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException
	{
		this.out.flush();
		this.copyOut.flush();
	}

	@Override
	public void close() throws IOException
	{
		this.out.close();
		this.copyOut.close();
	}
}
