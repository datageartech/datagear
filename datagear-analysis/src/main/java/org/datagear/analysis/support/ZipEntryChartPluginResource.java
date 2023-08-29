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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.datagear.analysis.ChartPluginResource;
import org.datagear.util.IOUtil;

/**
 * ZIP包内实体{@linkplain ChartPluginResource}。
 * 
 * @author datagear@163.com
 *
 */
public class ZipEntryChartPluginResource implements ChartPluginResource, Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;

	private File zipFile;

	private String entryName;

	public ZipEntryChartPluginResource()
	{
		super();
	}

	public ZipEntryChartPluginResource(String name, File zipFile, String entryName)
	{
		super();
		this.name = name;
		this.zipFile = zipFile;
		this.entryName = entryName;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public File getZipFile()
	{
		return zipFile;
	}

	public void setZipFile(File zipFile)
	{
		this.zipFile = zipFile;
	}

	public String getEntryName()
	{
		return entryName;
	}

	public void setEntryName(String entryName)
	{
		this.entryName = entryName;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		ZipInputStream in = null;
		boolean found = false;

		try
		{
			in = IOUtil.getZipInputStream(this.zipFile);

			ZipEntry zipEntry = null;
			while ((zipEntry = in.getNextEntry()) != null)
			{
				String name = zipEntry.getName();

				if (name.equals(this.entryName))
				{
					found = true;
					break;
				}
				else
					in.closeEntry();
			}

			if (!found)
				throw new FileNotFoundException(this.entryName);

			return in;
		}
		finally
		{
			if (!found)
				IOUtil.close(in);
		}
	}

	@Override
	public long getLastModified()
	{
		return this.zipFile.lastModified();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + "]";
	}
}
