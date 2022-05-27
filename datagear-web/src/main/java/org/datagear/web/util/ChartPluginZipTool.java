/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;

/**
 * 图表插件ZIP工具，批量压缩、解压。
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginZipTool
{
	/**
	 * 将指定目录下的所有图表插件文件夹压缩为ZIP文件。
	 * 
	 * @param directory
	 */
	public static void zip(File directory) throws IOException
	{
		File[] files = directory.listFiles();

		int count = 0;

		for (File file : files)
		{
			if (!file.isDirectory())
				continue;

			File zipFile = FileUtil.getFile(directory, file.getName() + ".zip");

			ZipOutputStream out = null;

			try
			{
				out = IOUtil.getZipOutputStream(zipFile);
				IOUtil.writeFileToZipOutputStream(out, file, "");
			}
			finally
			{
				IOUtil.close(out);
			}

			count++;
			println("Zip [" + file.getName() + "] to [" + zipFile.getName() + "]");
		}

		println("Zip total : " + count);
	}

	/**
	 * 解压指定目录下的所有图表插件ZIP文件。
	 * 
	 * @param directory
	 * @param deleteAfterUnzip
	 */
	public static void unzip(File directory, boolean deleteAfterUnzip) throws IOException
	{
		File[] files = directory.listFiles();

		int count = 0;
		for (File file : files)
		{
			if (!FileUtil.isExtension(file, "zip"))
				continue;

			File myDirectory = FileUtil.getDirectory(directory, FileUtil.deleteExtension(file.getName()), true);

			ZipInputStream in = null;

			try
			{
				in = IOUtil.getZipInputStream(file);
				IOUtil.unzip(in, myDirectory);
			}
			finally
			{
				IOUtil.close(in);
			}

			if (deleteAfterUnzip)
				FileUtil.deleteFile(file);

			count++;
			println("Unzip [" + file.getName() + "] to [" + myDirectory.getName() + "]");
		}

		println("Unzip total : " + count);
	}

	protected static void println(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.println(str);
	}

	public static void main(String[] args) throws Exception
	{
		File directory = FileUtil.getDirectory("target/chart-plugins", true);

		println("*****************************************");
		println("ChartPluginZipTool, on directory [" + directory.getPath() + "]");
		println("Print:");
		println("1 : for unzip");
		println("2 : for zip");
		println("clean : for clean");
		println("*****************************************");
		println("");

		Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine())
		{
			String input = scanner.nextLine().trim();

			if (input.isEmpty())
				;
			else if ("exit".equalsIgnoreCase(input))
			{
				println("Bye!");
				scanner.close();
				System.exit(0);
			}
			else if ("1".equals(input))
			{
				unzip(directory, true);
			}
			else if ("2".equals(input))
			{
				zip(directory);
			}
			else if ("clean".equals(input))
			{
				FileUtil.clearDirectory(directory);
				println("Clean ok!");
			}
		}
	}
}
