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
