/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

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
