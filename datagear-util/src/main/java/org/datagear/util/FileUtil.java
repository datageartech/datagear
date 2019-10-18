/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 文件工具类。
 * 
 * @author datagear@163.com
 *
 */
public class FileUtil
{
	private FileUtil()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取文件对象。
	 * 
	 * @param file
	 * @return
	 */
	public static File getFile(String file)
	{
		return new File(file);
	}

	/**
	 * 获取指定目录下的文件对象。
	 * 
	 * @param parent
	 * @param file
	 * @return
	 */
	public static File getFile(File parent, String file)
	{
		return new File(parent, file);
	}

	/**
	 * 获取目录对象。
	 * <p>
	 * 如果目录不存在，则创建。
	 * </p>
	 * 
	 * @param file
	 * @return
	 */
	public static File getDirectory(String file)
	{
		File directory = new File(file);

		if (!directory.exists())
			directory.mkdirs();

		return directory;
	}

	/**
	 * 获取指定目录下的子目录。
	 * <p>
	 * 如果子目录不存在，则创建。
	 * </p>
	 * 
	 * @param parent
	 * @param file
	 * @return
	 */
	public static File getDirectory(File parent, String file)
	{
		File directory = new File(parent, file);

		if (!directory.exists())
			directory.mkdirs();

		return directory;
	}

	/**
	 * 删除文件。
	 * 
	 * @param file
	 * @return
	 */
	public static boolean deleteFile(File file)
	{
		if (!file.exists())
			return true;

		if (file.isDirectory())
		{
			File[] children = file.listFiles();

			for (File child : children)
				deleteFile(child);
		}

		return file.delete();
	}

	/**
	 * 清空目录，保留目录本身。
	 * 
	 * @param directory
	 * @return
	 */
	public static boolean clearDirectory(File directory)
	{
		if (!directory.exists())
			return true;

		boolean clear = true;

		if (directory.isDirectory())
		{
			File[] children = directory.listFiles();

			for (File child : children)
			{
				boolean deleted = deleteFile(child);

				if (!deleted && clear)
					clear = false;
			}
		}

		return clear;
	}

	/**
	 * 在指定目录下生成一个文件。
	 * 
	 * @param parent
	 * @return
	 */
	public static File generateUniqueFile(File parent)
	{
		return new File(parent, IDUtil.uuid());
	}

	/**
	 * 在指定目录下生成一个文件。
	 * 
	 * @param parent
	 * @param extension
	 *            为{@code null}时不生成后缀
	 * @return
	 */
	public static File generateUniqueFile(File parent, String extension)
	{
		String name = (StringUtil.isEmpty(extension) ? IDUtil.uuid() : IDUtil.uuid() + "." + extension);
		return new File(parent, name);
	}

	/**
	 * 在指定目录下生成一个子文件夹。
	 * 
	 * @param parent
	 * @return
	 */
	public static File generateUniqueDirectory(File parent)
	{
		File file = new File(parent, IDUtil.uuid());

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

	/**
	 * 获取{@linkplain File}的{@linkplain URL}。
	 * 
	 * @param file
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static URL toURL(File file) throws IllegalArgumentException
	{
		try
		{
			return file.toURI().toURL();
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("Illegal [" + file.toString() + "] to URL");
		}
	}

	/**
	 * 获取文件名后缀。
	 * <p>
	 * 如果文件名没有后缀，将返回{@code null}
	 * </p>
	 * 
	 * @param file
	 * @return
	 */
	public static String getExtension(File file)
	{
		if (file.isDirectory())
			throw new IllegalArgumentException("[file] must not be directory");

		String name = file.getName();

		return getExtension(name);
	}

	/**
	 * 获取文件名后缀。
	 * <p>
	 * 如果文件名没有后缀，将返回{@code null}
	 * </p>
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getExtension(String fileName)
	{
		if (fileName == null)
			return null;

		int dotIdx = fileName.lastIndexOf('.');

		if (dotIdx > 0 && dotIdx < fileName.length() - 1)
			return fileName.substring(dotIdx + 1);
		else
			return null;
	}
}
