/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 文件字符输出流{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class FileWriterResourceFactory extends AbstractWriterResourceFactory
{
	private File file;

	public FileWriterResourceFactory()
	{
		super();
	}

	public FileWriterResourceFactory(File file)
	{
		super();
		this.file = file;
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	@Override
	protected OutputStream getOutputStream() throws Exception
	{
		return new FileOutputStream(this.file);
	}

	/**
	 * 构建{@linkplain FileWriterResourceFactory}。
	 * 
	 * @param file
	 * @return
	 */
	public static FileWriterResourceFactory valueOf(File file)
	{
		return new FileWriterResourceFactory(file);
	}

	/**
	 * 构建{@linkplain FileWriterResourceFactory}。
	 * 
	 * @param file
	 * @param charset
	 * @return
	 */
	public static FileWriterResourceFactory valueOf(File file, Charset charset)
	{
		FileWriterResourceFactory resourceFactory = new FileWriterResourceFactory(file);
		resourceFactory.setCharset(charset);

		return resourceFactory;
	}

	/**
	 * 构建{@linkplain FileWriterResourceFactory}。
	 * 
	 * @param file
	 * @param charsetName
	 * @return
	 */
	public static FileWriterResourceFactory valueOf(File file, String charsetName)
	{
		Charset charset = Charset.forName(charsetName);

		FileWriterResourceFactory resourceFactory = new FileWriterResourceFactory(file);
		resourceFactory.setCharset(charset);

		return resourceFactory;
	}
}
