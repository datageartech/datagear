/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 文件字符输入流{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class FileReaderResourceFactory extends AbstractReaderResourceFactory
{
	private File file;

	public FileReaderResourceFactory()
	{
		super();
	}

	public FileReaderResourceFactory(File file)
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
	protected InputStream getInputStream() throws Exception
	{
		return new FileInputStream(this.file);
	}

	/**
	 * 构建{@linkplain FileReaderResourceFactory}。
	 * 
	 * @param file
	 * @return
	 */
	public static FileReaderResourceFactory valueOf(File file)
	{
		return new FileReaderResourceFactory(file);
	}

	/**
	 * 构建{@linkplain FileReaderResourceFactory}。
	 * 
	 * @param file
	 * @param charset
	 * @return
	 */
	public static FileReaderResourceFactory valueOf(File file, Charset charset)
	{
		FileReaderResourceFactory resourceFactory = new FileReaderResourceFactory(file);
		resourceFactory.setCharset(charset);

		return resourceFactory;
	}

	/**
	 * 构建{@linkplain FileReaderResourceFactory}。
	 * 
	 * @param file
	 * @param charsetName
	 * @return
	 */
	public static FileReaderResourceFactory valueOf(File file, String charsetName)
	{
		Charset charset = Charset.forName(charsetName);

		FileReaderResourceFactory resourceFactory = new FileReaderResourceFactory(file);
		resourceFactory.setCharset(charset);

		return resourceFactory;
	}
}
