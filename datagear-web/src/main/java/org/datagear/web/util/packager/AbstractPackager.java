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

package org.datagear.web.util.packager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;

/**
 * 抽象打包工具类。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractPackager
{
	public AbstractPackager()
	{
		super();
	}

	/**
	 * 合并多个文件至指定目录内。
	 * 
	 * @param sources
	 * @param targetDir
	 *            合并存储目录，没有则会创建
	 * @param threshold
	 *            单个合并文件的阈值KB数，{@code -1}表示不设，只会合并至一个文件中
	 * @param nameGenerator
	 *            类型参数：&lt;合并文件总数, 当前合并文件索引, 生成文件名&gt;
	 * @return
	 * @throws IOException
	 */
	protected List<File> mergeFile(List<File> sources, File targetDir, int threshold,
			BiFunction<Integer, Integer, String> nameGenerator) throws IOException
	{
		FileUtil.mkdirsIfNot(targetDir);

		List<File> re = new ArrayList<>();

		List<List<File>> filess = splitByThreshold(sources, threshold);

		for (int i = 0; i < filess.size(); i++)
		{
			List<File> files = filess.get(i);
			File mergeFile = FileUtil.getFile(targetDir, nameGenerator.apply(filess.size(), i));

			OutputStream out = null;

			try
			{
				out = IOUtil.getOutputStream(mergeFile);

				for (File file : files)
				{
					InputStream in = null;

					try
					{
						in = IOUtil.getInputStream(file);
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

			re.add(mergeFile);
		}

		return re;
	}

	protected List<List<File>> splitByThreshold(List<File> sources, int threshold)
	{
		List<List<File>> re = new ArrayList<>();

		if (threshold < 0)
		{
			re.add(sources);
		}
		else
		{
			long thresholdBytes = (long)threshold * 1024;
			
			List<File> split = new ArrayList<>();
			long sizeTotal = 0;
			
			for(File file : sources)
			{
				// 至少添加一个文件
				split.add(file);
				sizeTotal += file.length();

				if (sizeTotal >= thresholdBytes)
				{
					re.add(split);
					split = new ArrayList<>();
					sizeTotal = 0;
				}
			}

			if (split.size() > 0)
				re.add(split);
		}

		return re;
	}

	protected List<File> toFiles(String baseDir, List<String> paths)
	{
		List<File> files = new ArrayList<>(paths.size());

		File parent = FileUtil.getDirectory(baseDir, false);
		
		for (String path : paths)
		{
			File file = FileUtil.getFile(parent, path);
			files.add(file);
		}

		return files;
	}

	protected static void print(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.print(str);
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
