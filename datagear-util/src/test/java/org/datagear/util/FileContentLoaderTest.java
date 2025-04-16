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

package org.datagear.util;

import java.io.File;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain FileContentLoader}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class FileContentLoaderTest
{
	@Test
	public void loadTest() throws Exception
	{
		File file = FileUtil.getFile("target/test/FileContentLoaderTest/1.txt");

		{
			writeFile(file, "文件内容", null);
			FileContentLoader loader = new FileContentLoader(file);

			Assert.assertEquals("文件内容", loader.load());

			writeFile(file, "文件内容-1", null);

			Assert.assertEquals("文件内容-1", loader.load());
		}

		{
			writeFile(file, "文件内容UTF-8", IOUtil.CHARSET_UTF_8);

			FileContentLoader loader = new FileContentLoader(file, IOUtil.CHARSET_UTF_8);
			String actual = loader.load();

			Assert.assertEquals("文件内容UTF-8", actual);

			writeFile(file, "文件内容-1", IOUtil.CHARSET_UTF_8);

			Assert.assertEquals("文件内容-1", loader.load());
		}
	}

	protected void writeFile(File file, String content, String encoding) throws Exception
	{
		FileUtil.deleteFile(file);
		FileUtil.mkdirsIfNot(file.getParentFile());

		Writer out = null;

		try
		{
			if (StringUtil.isEmpty(encoding))
				out = IOUtil.getWriter(file);
			else
				out = IOUtil.getWriter(file, encoding);

			out.write(content);
		}
		finally
		{
			IOUtil.close(out);
		}
	}
}
