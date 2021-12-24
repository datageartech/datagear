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
 * 基于{@linkplain StringBuilder}的输出复制{@linkplain Writer}。
 * <p>
 * 写入{@linkplain Writer}的内容可同时写入{@linkplain StringBuilder}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class StringBuilderCopyWriter extends Writer
{
	private Writer out;

	private StringBuilder copyOut = new StringBuilder();

	private boolean copy = true;

	/**
	 * 创建{@linkplain StringBuilderCopyWriter}，{@linkplain #isCopy()}为{@code true}
	 * 
	 * @param out
	 */
	public StringBuilderCopyWriter(Writer out)
	{
		this(out, true);
	}

	public StringBuilderCopyWriter(Writer out, boolean copy)
	{
		super();
		this.out = out;
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
	public StringBuilder getCopyOut()
	{
		return copyOut;
	}

	public void setCopyOut(StringBuilder copyOut)
	{
		this.copyOut = copyOut;
	}

	/**
	 * 获取复制输出内容。
	 * 
	 * @return
	 */
	public String getCopyString()
	{
		return this.copyOut.toString();
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
			this.copyOut.append(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException
	{
		this.out.flush();
	}

	@Override
	public void close() throws IOException
	{
		this.out.close();
	}
}
