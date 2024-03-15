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
 * 变向输出流。
 * <p>
 * 通过{@linkplain #setRedirect(boolean)}可以控制输出流至{@linkplain #getRedirectOut()}而非{@linkplain #getOut()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class RedirectWriter extends Writer
{
	private Writer out;

	private Writer redirectOut;

	private boolean redirect;

	public RedirectWriter()
	{
		super();
	}

	public RedirectWriter(Writer out, Writer redirectOut, boolean redirect)
	{
		super();
		this.out = out;
		this.redirectOut = redirectOut;
		this.redirect = redirect;
	}

	public RedirectWriter(Writer out, Writer redirectOut, boolean redirect, Object lock)
	{
		super(lock);
		this.out = out;
		this.redirectOut = redirectOut;
		this.redirect = redirect;
	}
	
	public Writer getOut()
	{
		return out;
	}

	public void setOut(Writer out)
	{
		this.out = out;
	}

	public Writer getRedirectOut()
	{
		return redirectOut;
	}

	public void setRedirectOut(Writer redirectOut)
	{
		this.redirectOut = redirectOut;
	}

	/**
	 * 是否变向输出流至{@linkplain #getRedirectOut()}。
	 * 
	 * @return
	 */
	public boolean isRedirect()
	{
		return redirect;
	}

	/**
	 * 设置是否变向输出流至{@linkplain #getRedirectOut()}。
	 * 
	 * @param redirect
	 */
	public void setRedirect(boolean redirect)
	{
		this.redirect = redirect;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		if (this.isRedirect())
			this.redirectOut.write(cbuf, off, len);
		else
			this.out.write(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException
	{
		this.out.flush();
		this.redirectOut.flush();
	}

	@Override
	public void close() throws IOException
	{
		this.out.close();
		this.redirectOut.close();
	}
}
