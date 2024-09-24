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

package org.datagear.web.util;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.datagear.analysis.support.html.HtmlChartPluginLoader;
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
	protected static Pattern VERSION_REG_NO_QUOTE = Pattern.compile("version\\s*\\:\\s*\\\"[^\\\"]+\\\"");
	protected static Pattern VERSION_REG_QUOTE = Pattern.compile("\\\"version\\\"\\s*\\:\\s*\\\"[^\\\"]+\\\"");

	/**
	 * 将指定目录下的所有图表插件文件夹压缩为ZIP文件。
	 * 
	 * @param directory
	 */
	public void zip(File directory) throws Exception
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

		println("Zip total : " + count + " / " + files.length);
	}

	/**
	 * 解压指定目录下的所有图表插件ZIP文件。
	 * 
	 * @param directory
	 * @param deleteAfterUnzip
	 */
	public void unzip(File directory, boolean deleteAfterUnzip) throws Exception
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

		println("Unzip total : " + count + " / " + files.length);
	}

	/**
	 * 修改指定目录下所有插件的版本号。
	 * 
	 * @param directory
	 * @param newVersion
	 * @throws Exception
	 */
	public void updateVersion(File directory, String newVersion) throws Exception
	{
		File[] files = directory.listFiles();

		if (files == null)
			return;

		int updateCount = 0;
		for (File file : files)
		{
			if (!file.isDirectory())
				continue;

			File pf = getPluginJsonFile(file);

			if (!pf.exists())
				continue;
			
			String pfPath = file.getName() + "/" + pf.getName();

			String str = readString(pf);
			boolean replaced = false;

			{
				int count = matchesCount(str, VERSION_REG_NO_QUOTE);

				if (count == 1)
				{
					str = replaceAll(str, VERSION_REG_NO_QUOTE, "version: \"" + newVersion + "\"");
					replaced = true;
				}
				else if (count > 1)
				{
					throw new IllegalStateException(
							count + " matches found in : " + pfPath);
				}
			}

			if (!replaced)
			{
				int count = matchesCount(str, VERSION_REG_QUOTE);

				if (count == 1)
				{
					str = replaceAll(str, VERSION_REG_QUOTE, "\"version\": \"" + newVersion + "\"");
					replaced = true;
				}
				else if (count > 1)
				{
					throw new IllegalStateException(
							count + " matches found in : " + pfPath);
				}
			}

			if (replaced)
			{
				writeString(pf, str);
				println("Update version to \"" + newVersion + "\" for [" + pfPath + "]");
				updateCount++;
			}
		}

		println("Update version total : " + updateCount + " / " + files.length);
	}

	protected int matchesCount(String str, Pattern pattern)
	{
		Matcher m = pattern.matcher(str);

		int count = 0;

		while (m.find() == true)
			count++;

		return count;
	}

	protected String replaceAll(String str, Pattern pattern, String text)
	{
		Matcher m = pattern.matcher(str);
		return m.replaceAll(text);
	}

	/**
	 * 创建{@code renderer.js}文件规范。
	 * 
	 * @param directory
	 */
	public void createRendererJs(File directory) throws Exception
	{
		File[] files = directory.listFiles();

		if (files == null)
			return;
		
		String content = "(function(plugin)" + "\n" //
				+ "{" + "\n" //
				+ "\t" + "var r=" + "\n" //
				+ "\t" + "{};" + "\n" //
				+ "\t" + "" + "\n" //
				+ "\t" + "return r;" + "\n" //
				+ "})" + "\n" //
				+ "(plugin);";

		int count = 0;
		for (File file : files)
		{
			if (!file.isDirectory())
				continue;

			File rf = getRendererJsFile(file);

			if (rf.exists())
				continue;

			createRendererJsFile(file, content);
			count++;

			println("Create [" + rf.getName() + "] for : " + file.getName());
		}

		println("Create " + HtmlChartPluginLoader.FILE_NAME_RENDERER + " version total : " + count + " / "
				+ files.length);
	}

	protected void createRendererJsFile(File directory, String content) throws Exception
	{
		File file = getRendererJsFile(directory);
		
		if(file.exists())
			throw new IllegalStateException("[" + file.getName() + "] exists");

		Reader in = null;
		Writer out = null;

		try
		{
			in = IOUtil.getReader(content);
			out = IOUtil.getWriter(file, IOUtil.CHARSET_UTF_8);
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	protected String readString(File file) throws Exception
	{
		Reader in = null;

		try
		{
			in = IOUtil.getReader(file, IOUtil.CHARSET_UTF_8);
			return IOUtil.readString(in, false);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	protected void writeString(File file, String content) throws Exception
	{
		Reader in = null;
		Writer out = null;

		try
		{
			in = IOUtil.getReader(content);
			out = IOUtil.getWriter(file, IOUtil.CHARSET_UTF_8);
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	protected File getRendererJsFile(File directory)
	{
		return FileUtil.getFile(directory, HtmlChartPluginLoader.FILE_NAME_RENDERER);
	}

	protected File getPluginJsonFile(File directory)
	{
		return FileUtil.getFile(directory, HtmlChartPluginLoader.FILE_NAME_PLUGIN);
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
		ChartPluginZipTool tool = new ChartPluginZipTool();
		File directory = FileUtil.getDirectory("target/chart-plugins", true);

		println("*****************************************");
		println("ChartPluginZipTool, on directory [" + directory.getPath() + "]");
		println("Print:");
		println("1 : for unzip");
		println("2 : for zip");
		println("3 : for replace version");
		println("4 : for create " + HtmlChartPluginLoader.FILE_NAME_RENDERER);
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
				tool.unzip(directory, true);
			}
			else if ("2".equals(input))
			{
				tool.zip(directory);
			}
			else if ("3".equals(input))
			{
				println("Input new version :");
				String version = scanner.nextLine().trim();
				tool.updateVersion(directory, version);
			}
			else if ("4".equals(input))
			{
				tool.createRendererJs(directory);
			}
			else if ("clean".equals(input))
			{
				FileUtil.clearDirectory(directory);
				println("Clean ok!");
			}
		}
	}
}
