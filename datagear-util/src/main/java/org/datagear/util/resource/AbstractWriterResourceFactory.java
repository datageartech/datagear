/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
