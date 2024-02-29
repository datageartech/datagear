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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 文件合并器，它可以将指定目录下的所有文件合并至单个文件中。
 * 
 * @author datagear@163.com
 *
 */
public class FileMerger
{
	public FileMerger()
	{
	}

	/**
	 * 合并目下的所有文件。
	 * 
	 * @param directory
	 * @param toFileName
	 *            合并至文件的名称
	 * @throws IOException
	 */
	public void merge(File directory, String toFileName) throws IOException
	{
		if (!directory.isDirectory())
			throw new IllegalArgumentException();

		File[] children = directory.listFiles();

		File toFile = FileUtil.getFile(directory, toFileName);

		OutputStream out = null;

		try
		{
			out = IOUtil.getOutputStream(toFile);

			for (File child : children)
			{
				if (child.isDirectory())
					continue;

				InputStream in = null;

				try
				{
					in = IOUtil.getInputStream(child);
					IOUtil.write(in, out);
				}
				finally
				{
					IOUtil.close(in);
				}
			}
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	public static void main(String[] args) throws Exception
	{
		File directory = null;
		String toFileName = null;

		Scanner scanner = new Scanner(System.in);

		println("Print directory to merge:");
		while (scanner.hasNextLine())
		{
			String input = scanner.nextLine().trim();

			if (!StringUtil.isEmpty(input))
			{
				directory = FileUtil.getFile(input);
				if (!directory.exists() || !directory.isDirectory())
					directory = null;
			}

			if (directory != null)
				break;
			else
				println("Print directory to merge:");
		}

		println("Print the file name merge to:");
		while (scanner.hasNextLine())
		{
			toFileName = scanner.nextLine().trim();

			if (!StringUtil.isEmpty(toFileName))
				break;
			else
				println("Print the file name merge to:");
		}

		IOUtil.close(scanner);

		FileMerger fileMerger = new FileMerger();
		fileMerger.merge(directory, toFileName);
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

	protected static void println()
	{
		System.out.println();
	}
}
