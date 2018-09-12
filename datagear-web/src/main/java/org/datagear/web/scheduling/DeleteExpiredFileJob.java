/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.scheduling;

import java.io.File;

/**
 * 删除过期文件任务。
 * 
 * @author datagear@163.com
 *
 */
public class DeleteExpiredFileJob
{
	/** 处理目录 */
	private File directory;

	/** 忽略删除的文件名 */
	private String[] ignoreFileNames;

	/** 过期阀值分钟数 */
	private int expireThresholdMinutes;

	public DeleteExpiredFileJob()
	{
		super();
	}

	public DeleteExpiredFileJob(File directory, int expireThresholdMinutes)
	{
		super();
		this.directory = directory;
		this.expireThresholdMinutes = expireThresholdMinutes;
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

	public int getExpireThresholdMinutes()
	{
		return expireThresholdMinutes;
	}

	public void setExpireThresholdMinutes(int expireThresholdMinutes)
	{
		this.expireThresholdMinutes = expireThresholdMinutes;
	}

	/**
	 * 删除
	 */
	public void delete()
	{
		if (!this.directory.exists())
			return;

		long time = new java.util.Date().getTime() - this.expireThresholdMinutes * 1000 * 60;

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

			if (deleted && isModifiedBefore(child, time))
				deleteFile(child);
		}
	}

	protected boolean isModifiedBefore(File file, long time)
	{
		long lastModified = file.lastModified();

		return lastModified < time;
	}

	protected void deleteFile(File file)
	{
		if (!file.exists())
			return;

		if (file.isDirectory())
		{
			File[] children = file.listFiles();

			for (File child : children)
				deleteFile(child);
		}

		file.delete();
	}
}
