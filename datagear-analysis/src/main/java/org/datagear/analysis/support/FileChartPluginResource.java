/*
 * Copyright 2018-2023 datagear.tech
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

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + "]";
	}
}
