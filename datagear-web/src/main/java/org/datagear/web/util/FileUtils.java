/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
	 * 获取目录下指定名称的文件。
	 * 
	 * @param directory
	 * @param fileName
	 * @return
	 */
	public static File getFile(File directory, String fileName)
	{
		return new File(directory, fileName);
	}

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
	 * 获取目录下的子目录。
	 * 
	 * @param directory
	 * @param fileName
	 * @return
	 */
	public static File getDirectory(File directory, String fileName)
	{
		File file = getFile(directory, fileName);

		if (!file.exists())
			file.mkdirs();

		return file;
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
	 * 删除目录下指定名称的文件。
	 * 
	 * @param directory
	 * @param fileName
	 */
	public static void deleteFile(File directory, String fileName)
	{
		File file = getFile(directory, fileName);

		deleteFile(file);
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
	 * 获取目录下指定文件的输入流。
	 * 
	 * @param directory
	 * @param fileName
	 * @return
	 */
	public static InputStream getInputStream(File directory, String fileName) throws IOException
	{
		File file = getFile(directory, fileName);
		return getInputStream(file);
	}

	/**
	 * 获取目录下指定文件的输入流。
	 * 
	 * @param file
	 * @return
	 */
	public static InputStream getInputStream(File file) throws IOException
	{
		return new BufferedInputStream(new FileInputStream(file));
	}

	/**
	 * 获取目录下指定文件的输出流。
	 * 
	 * @param directory
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static OutputStream getOutputStream(File directory, String fileName) throws IOException
	{
		File file = getFile(directory, fileName);
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	/**
	 * 将输入流写入目录下的指定文件。
	 * 
	 * @param in
	 * @param directory
	 * @param fileName
	 * @throws IOException
	 */
	public static void write(InputStream in, File directory, String fileName) throws IOException
	{
		OutputStream out = null;

		try
		{
			out = getOutputStream(directory, fileName);

			byte[] buffer = new byte[1024];

			int readLen = 0;
			while ((readLen = in.read(buffer)) > 0)
				out.write(buffer, 0, readLen);
		}
		finally
		{
			close(out);
		}
	}

	/**
	 * 将目录下的指定文件写入输出流。
	 * 
	 * @param directory
	 * @param fileName
	 * @param out
	 * @throws IOException
	 */
	public static void write(File directory, String fileName, OutputStream out) throws IOException
	{
		write(getFile(directory, fileName), out);
	}

	/**
	 * 将文件写入输出流。
	 * 
	 * @param file
	 * @param out
	 * @throws IOException
	 */
	public static void write(File file, OutputStream out) throws IOException
	{
		InputStream in = null;

		try
		{
			in = getInputStream(file);

			byte[] buffer = new byte[1024];

			int readLen = 0;
			while ((readLen = in.read(buffer)) > 0)
				out.write(buffer, 0, readLen);
		}
		finally
		{
			close(in);
		}
	}

	/**
	 * 删除文件夹里的所有文件。
	 * 
	 * @param directory
	 */
	public static void deleteFileIn(File directory)
	{
		if (!directory.exists())
			return;

		if (directory.isDirectory())
		{
			File[] children = directory.listFiles();

			for (File child : children)
				deleteFile(child);
		}
	}

	/**
	 * 删除文件。
	 * 
	 * @param file
	 */
	public static void deleteFile(File file)
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
	 * 关闭{@linkplain Closeable}。
	 * 
	 * @param closeable
	 */
	public static void close(Closeable closeable)
	{
		if (closeable == null)
			return;

		try
		{
			closeable.close();
		}
		catch (IOException e)
		{
		}
	}

	/**
	 * 解压ZIP输入流至指定文件夹。
	 * 
	 * @param zipInputStream
	 * @param directory
	 * @throws IOException
	 */
	public static void unzip(ZipInputStream zipInputStream, File directory) throws IOException
	{
		if (!directory.exists())
			directory.mkdirs();

		byte[] buffer = new byte[1024];

		ZipEntry zipEntry = null;

		while ((zipEntry = zipInputStream.getNextEntry()) != null)
		{
			File my = new File(directory, zipEntry.getName());

			if (zipEntry.isDirectory())
			{
				if (!my.exists())
					my.mkdirs();
			}
			else
			{
				File parent = my.getParentFile();

				if (parent != null && !parent.exists())
					parent.mkdirs();

				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(my));

				try
				{
					int readLen = 0;
					while ((readLen = zipInputStream.read(buffer)) > 0)
						out.write(buffer, 0, readLen);
				}
				finally
				{
					out.close();
				}
			}

			zipInputStream.closeEntry();
		}
	}
}
