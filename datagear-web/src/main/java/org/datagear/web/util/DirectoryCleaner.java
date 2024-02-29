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

package org.datagear.web.util;

import java.io.File;

import org.datagear.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 目录清洁工，删除目录内过期的文件（不删除目录本身）。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryCleaner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryCleaner.class);

	/** 目录 */
	private File directory;

	/** 过期分钟数 */
	private int expiredMinutes;

	/** 忽略删除的文件名 */
	private String[] ignoreFileNames = null;

	public DirectoryCleaner()
	{
		super();
	}

	public DirectoryCleaner(File directory, int expiredMinutes)
	{
		super();
		this.directory = directory;
		this.expiredMinutes = expiredMinutes;
	}

	public File getDirectory()
	{
		return directory;
	}

	public void setDirectory(File directory)
	{
		this.directory = directory;
	}

	public String[] getIgnoreFileNames()
	{
		return ignoreFileNames;
	}

	public void setIgnoreFileNames(String[] ignoreFileNames)
	{
		this.ignoreFileNames = ignoreFileNames;
	}

	public void setIgnoreFileName(String ignoreFileName)
	{
		this.ignoreFileNames = new String[] { ignoreFileName };
	}

	public int getExpiredMinutes()
	{
		return expiredMinutes;
	}

	public void setExpiredMinutes(int expiredMinutes)
	{
		this.expiredMinutes = expiredMinutes;
	}

	/**
	 * 执行清洁，删除过期文件。
	 */
	public void clean()
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start clean directory: {}", this.directory.getAbsolutePath());

		if (this.directory.exists())
		{
			long time = new java.util.Date().getTime() - this.expiredMinutes * 1000 * 60;

			File[] children = this.directory.listFiles();

			for (File child : children)
			{
				boolean deleted = true;

				if (this.ignoreFileNames != null)
				{
					for (String ignoreFileName : this.ignoreFileNames)
					{
						if (child.getName().equals(ignoreFileName))
						{
							deleted = false;
							break;
						}
					}
				}

				if (deleted)
					deleteFileIfModifiedBefore(child, time);
			}
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("finish clean directory: {}", this.directory.getAbsolutePath());
	}

	protected boolean deleteFileIfModifiedBefore(File file, long beforeTime)
	{
		if (!file.exists())
			return true;

		boolean delete = false;

		if (file.isDirectory())
		{
			File[] children = file.listFiles();

			if (children == null || children.length == 0)
				delete = isModifiedBefore(file, beforeTime);
			else
			{
				int deleteCount = 0;

				for (File child : children)
				{
					if (deleteFileIfModifiedBefore(child, beforeTime))
						deleteCount++;
				}

				if (deleteCount == children.length)
					delete = true;
			}
		}
		else
			delete = isModifiedBefore(file, beforeTime);

		if (delete)
		{
			FileUtil.deleteFile(file);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delete expired file: {}", file.getAbsolutePath());
		}

		return delete;
	}

	protected boolean isModifiedBefore(File file, long time)
	{
		long lastModified = file.lastModified();

		return lastModified < time;
	}
}
