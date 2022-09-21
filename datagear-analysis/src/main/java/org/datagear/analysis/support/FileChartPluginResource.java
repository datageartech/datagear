/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.datagear.analysis.ChartPluginResource;
import org.datagear.util.IOUtil;

/**
 * 文件{@linkplain ChartPluginResource}。
 * 
 * @author datagear@163.com
 *
 */
public class FileChartPluginResource implements ChartPluginResource
{
	private String name;

	private File file;

	public FileChartPluginResource()
	{
		super();
	}

	public FileChartPluginResource(String name, File file)
	{
		super();
		this.name = name;
		this.file = file;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
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
	public InputStream getInputStream() throws IOException
	{
		return IOUtil.getInputStream(this.file);
	}

	@Override
	public long getLastModified()
	{
		return file.lastModified();
	}
}
