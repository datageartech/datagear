/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.scheduling;

import java.io.File;

import org.datagear.connection.IOUtil;

/**
 * 删除过期文件任务。
 * <p>
 * 此类不会删除{@linkplain #getDirectory()}。
 * </p>
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
		if (this.directory.exists())
		{
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

				if (deleted)
					deleteFileIfModifiedBefore(child, time);
			}
		}
	}

	protected boolean deleteFileIfModifiedBefore(File file, long beforeTime)
	{
		if (!file.exists())
			return true;

		boolean delete = false;

		if (isModifiedBefore(file, beforeTime))
			delete = true;
		else
		{
			if (file.isDirectory())
			{
				File[] children = file.listFiles();

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

		if (delete)
			IOUtil.deleteFile(file);

		return delete;
	}

	protected boolean isModifiedBefore(File file, long time)
	{
		long lastModified = file.lastModified();

		return lastModified < time;
	}
}
