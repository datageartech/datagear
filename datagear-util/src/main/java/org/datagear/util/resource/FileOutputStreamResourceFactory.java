/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
