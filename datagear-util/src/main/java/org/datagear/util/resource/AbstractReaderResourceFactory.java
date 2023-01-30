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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.datagear.util.IOUtil;

/**
 * 抽象字符输入流{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractReaderResourceFactory implements ResourceFactory<Reader>
{
	private Charset charset;

	public AbstractReaderResourceFactory()
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
	public Reader get() throws Exception
	{
		InputStreamReader in = (this.charset == null ? new InputStreamReader(getInputStream())
				: new InputStreamReader(getInputStream(), this.charset));

		BufferedReader reader = new BufferedReader(in);

		return reader;
	}

	@Override
	public void release(Reader resource) throws Exception
	{
		IOUtil.close(resource);
	}

	/**
	 * 获取字节输入流。
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract InputStream getInputStream() throws Exception;
}
