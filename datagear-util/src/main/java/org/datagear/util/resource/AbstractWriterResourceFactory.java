/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.util.resource;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.datagear.util.IOUtil;

/**
 * 抽象字符输入流{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractWriterResourceFactory implements ResourceFactory<Writer>
{
	private Charset charset;

	public AbstractWriterResourceFactory()
	{
		super();
	}

	public Charset getCharset()
	{
		return charset;
	}

	public void setCharset(Charset charset)
	{
		this.charset = charset;
	}

	@Override
	public Writer get() throws Exception
	{
		OutputStreamWriter out = (this.charset == null ? new OutputStreamWriter(getOutputStream())
				: new OutputStreamWriter(getOutputStream(), this.charset));

		BufferedWriter writer = new BufferedWriter(out);

		return writer;
	}

	@Override
	public void release(Writer resource) throws Exception
	{
		IOUtil.close(resource);
	}

	/**
	 * 获取字节输出流。
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract OutputStream getOutputStream() throws Exception;
}
