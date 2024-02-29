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

package org.datagear.util.resource;

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
