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

package org.datagear.util;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * 文件内容加载器。
 * 
 * @author datagear@163.com
 *
 */
public class FileContentLoader
{
	private File file;

	private String encoding;

	private boolean cached = true;

	private volatile String _prevReadContent = null;
	private volatile long _prevReadModfied = -1;

	public FileContentLoader()
	{
		super();
	}

	public FileContentLoader(File file)
	{
		super();
		this.file = file;
	}

	public FileContentLoader(File file, String encoding)
	{
		super();
		this.file = file;
		this.encoding = encoding;
	}

	public FileContentLoader(File file, String encoding, boolean cached)
	{
		super();
		this.file = file;
		this.encoding = encoding;
		this.cached = cached;
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public boolean isCached()
	{
		return cached;
	}

	public void setCached(boolean cached)
	{
		this.cached = cached;
	}

	/**
	 * 加载文件内容。
	 * <p>
	 * 如果{@linkplain #isCached()}为{@code true}，会缓存文件内容，并每次检查文件修改时间重新加载。
	 * </p>
	 * 
	 * @return
	 * @throws IOException
	 */
	public String load() throws IOException
	{
		if (!this.isCached())
		{
			return readFile(this.file);
		}
		else
		{
			if (this._prevReadContent == null || this._prevReadModfied != this.file.lastModified())
			{
				this._prevReadContent = readFile(this.file);
			}

			return this._prevReadContent;
		}
	}

	protected String readFile(File file) throws IOException
	{
		Reader in = null;

		try
		{
			if (StringUtil.isEmpty(this.encoding))
				in = IOUtil.getReader(file);
			else
				in = IOUtil.getReader(file, this.encoding);

			return IOUtil.readString(in, false);
		}
		finally
		{
			IOUtil.close(in);
		}
	}
}
