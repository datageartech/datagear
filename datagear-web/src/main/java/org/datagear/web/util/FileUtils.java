/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.util;

import java.io.File;

import org.datagear.persistence.support.UUID;
import org.datagear.web.vo.FileInfo;

/**
 * 文件工具类。
 * 
 * @author datagear@163.com
 *
 */
public class FileUtils
{
	/**
	 * 生成目录下唯一文件。
	 * 
	 * @param directory
	 * @return
	 */
	public static File generateUniqueFile(File directory)
	{
		return new File(directory, UUID.gen());
	}

	/**
	 * 生成目录下唯一子目录。
	 * 
	 * @param directory
	 * @return
	 */
	public static File generateUniqueDirectory(File directory)
	{
		File file = new File(directory, UUID.gen());

		if (!file.exists())
			file.mkdirs();

		return file;
	}

	/**
	 * 列出指定目录下的文件名称。
	 * 
	 * @param directory
	 * @return
	 */
	public static String[] listFileNames(File directory)
	{
		if (!directory.exists())
			return new String[0];

		return directory.list();
	}

	/**
	 * 获取指定文件的{@linkplain FileInfo}。
	 * 
	 * @param file
	 * @return
	 */
	public static FileInfo getFileInfo(File file)
	{
		FileInfo fileInfo = new FileInfo(file.getName());

		if (file.exists())
			fileInfo.setBytes(file.length());

		return fileInfo;
	}

	/**
	 * 获取指定目录下文件的{@linkplain FileInfo}。
	 * 
	 * @param directory
	 * @return
	 */
	public static FileInfo[] getFileInfos(File directory)
	{
		if (!directory.exists())
			return new FileInfo[0];

		File[] files = directory.listFiles();

		FileInfo[] fileInfos = new FileInfo[files.length];

		for (int i = 0; i < files.length; i++)
			fileInfos[i] = getFileInfo(files[i]);

		return fileInfos;
	}
}
