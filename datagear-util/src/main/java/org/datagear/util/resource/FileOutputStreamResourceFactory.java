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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.datagear.util.IOUtil;

/**
 * 文件输出流{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class FileOutputStreamResourceFactory implements ResourceFactory<OutputStream>
{
	private File file;

	public FileOutputStreamResourceFactory()
	{
		super();
	}

	public FileOutputStreamResourceFactory(File file)
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
	public OutputStream get() throws Exception
	{
		return new BufferedOutputStream(new FileOutputStream(this.file));
	}

	@Override
	public void release(OutputStream resource) throws Exception
	{
		IOUtil.close(resource);
	}

	/**
	 * 构建{@linkplain FileOutputStreamResourceFactory}。
	 * 
	 * @param file
	 * @return
	 */
	public static FileOutputStreamResourceFactory valueOf(File file)
	{
		return new FileOutputStreamResourceFactory(file);
	}
}
