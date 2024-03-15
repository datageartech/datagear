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

package org.datagear.util.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.datagear.util.IOUtil;

/**
 * 文件输入流{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class FileInputStreamResourceFactory implements ResourceFactory<InputStream>
{
	private File file;

	public FileInputStreamResourceFactory()
	{
		super();
	}

	public FileInputStreamResourceFactory(File file)
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
	public InputStream get() throws Exception
	{
		return new BufferedInputStream(new FileInputStream(this.file));
	}

	@Override
	public void release(InputStream resource) throws Exception
	{
		IOUtil.close(resource);
	}

	/**
	 * 构建{@linkplain FileInputStreamResourceFactory}。
	 * 
	 * @param file
	 * @return
	 */
	public static FileInputStreamResourceFactory valueOf(File file)
	{
		return new FileInputStreamResourceFactory(file);
	}
}
