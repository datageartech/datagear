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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

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
	public void loadTest_encoding_null() throws Exception
	{
		File file = FileUtil.getFile("target/test/FileContentLoaderTest/1.txt");
		TestFileContentLoader loader = new TestFileContentLoader(file);

		{
			writeFile(file, "文件内容", null);

			assertEquals("文件内容", loader.load());
			assertTrue(loader.isReadFromFile());

			assertEquals("文件内容", loader.load());
			assertFalse(loader.isReadFromFile());

			assertEquals("文件内容", loader.load());
			assertFalse(loader.isReadFromFile());
		}

		{
			Thread.sleep(3000);
			writeFile(file, "文件内容-1", null);
		}

		{
			assertEquals("文件内容-1", loader.load());
			assertTrue(loader.isReadFromFile());

			assertEquals("文件内容-1", loader.load());
			assertFalse(loader.isReadFromFile());

			assertEquals("文件内容-1", loader.load());
			assertFalse(loader.isReadFromFile());
		}
	}

	@Test
	public void loadTest_encoding_utf8() throws Exception
	{
		File file = FileUtil.getFile("target/test/FileContentLoaderTest/1.txt");
		TestFileContentLoader loader = new TestFileContentLoader(file, IOUtil.CHARSET_UTF_8);

		{
			writeFile(file, "文件内容UTF-8", IOUtil.CHARSET_UTF_8);

			assertEquals("文件内容UTF-8", loader.load());
			assertTrue(loader.isReadFromFile());

			assertEquals("文件内容UTF-8", loader.load());
			assertFalse(loader.isReadFromFile());

			assertEquals("文件内容UTF-8", loader.load());
			assertFalse(loader.isReadFromFile());
		}

		{
			Thread.sleep(3000);
			writeFile(file, "文件内容-2", IOUtil.CHARSET_UTF_8);
		}

		{
			assertEquals("文件内容-2", loader.load());
			assertTrue(loader.isReadFromFile());

			assertEquals("文件内容-2", loader.load());
			assertFalse(loader.isReadFromFile());

			assertEquals("文件内容-2", loader.load());
			assertFalse(loader.isReadFromFile());
		}
	}

	@Test
	public void loadTest_cached_false() throws Exception
	{
		File file = FileUtil.getFile("target/test/FileContentLoaderTest/1.txt");
		TestFileContentLoader loader = new TestFileContentLoader(file, null, false);

		{
			writeFile(file, "文件内容", null);

			assertEquals("文件内容", loader.load());
			assertTrue(loader.isReadFromFile());

			assertEquals("文件内容", loader.load());
			assertTrue(loader.isReadFromFile());

			assertEquals("文件内容", loader.load());
			assertTrue(loader.isReadFromFile());
		}

		{
			Thread.sleep(3000);
			writeFile(file, "文件内容-1", null);
		}

		{
			assertEquals("文件内容-1", loader.load());
			assertTrue(loader.isReadFromFile());

			assertEquals("文件内容-1", loader.load());
			assertTrue(loader.isReadFromFile());

			assertEquals("文件内容-1", loader.load());
			assertTrue(loader.isReadFromFile());
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
			out.flush();
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	protected static class TestFileContentLoader extends FileContentLoader
	{
		private boolean readFromFile = false;

		public TestFileContentLoader()
		{
			super();
		}

		public TestFileContentLoader(File file)
		{
			super(file);
		}

		public TestFileContentLoader(File file, String encoding)
		{
			super(file, encoding);
		}

		public TestFileContentLoader(File file, String encoding, boolean cached)
		{
			super(file, encoding, cached);
		}

		public boolean isReadFromFile()
		{
			return readFromFile;
		}

		public void setReadFromFile(boolean readFromFile)
		{
			this.readFromFile = readFromFile;
		}

		@Override
		public String load() throws IOException
		{
			setReadFromFile(false);
			return super.load();
		}

		@Override
		protected String readFile(File file) throws IOException
		{
			setReadFromFile(true);
			return super.readFile(file);
		}
	}
}
