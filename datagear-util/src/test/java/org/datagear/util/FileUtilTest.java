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

package org.datagear.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import org.junit.Test;

/**
 * {@linkplain FileUtil}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class FileUtilTest
{
	@Test
	public void getFileTest_String()
	{
		FileUtil.getDirectory("target");

		File file = FileUtil.getFile("target");

		assertTrue(file.exists());
	}

	@Test
	public void getFileTest_String_boolean()
	{
		File file = FileUtil.getFile("target/getFileTest_String_boolean");
		assertFalse(file.exists());

		file = FileUtil.getFile("target/getFileTest_String_boolean/", true);
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
		FileUtil.deleteFile(file);

		file = FileUtil.getFile("target/getFileTest_String_boolean/test", true);
		assertFalse(file.exists());
		assertTrue(file.getParentFile().exists());
		FileUtil.deleteFile(file.getParentFile());
	}

	@Test
	public void getFileTest_File_String()
	{
		File parent = FileUtil.getDirectory(FileUtil.getFile("target"), "getFileTest_File_String");
		assertTrue(parent.exists());

		File file = FileUtil.getFile(parent, "test");
		assertFalse(file.exists());

		FileUtil.deleteFile(parent);
	}

	@Test
	public void getFileTest_File_String_boolean()
	{
		File parent = FileUtil.getDirectory(FileUtil.getFile("target"), "getFileTest_File_String");
		assertTrue(parent.exists());

		File file = FileUtil.getFile(parent, "test", false);
		assertFalse(file.exists());

		file = FileUtil.getFile(parent, "test0/test1/", true);
		assertTrue(file.exists());

		file = FileUtil.getFile(parent, "test2/test3", true);
		assertTrue(file.getParentFile().exists());
		assertFalse(file.exists());

		FileUtil.deleteFile(parent);
	}

	@Test
	public void getDirectoryTest_String()
	{
		File file = FileUtil.getDirectory("target/getDirectoryTest_String");
		assertTrue(file.exists());

		FileUtil.deleteFile(file);
	}

	@Test
	public void getDirectoryTest_String_boolean()
	{
		File file = FileUtil.getDirectory("target/getDirectoryTest_String_boolean", false);
		assertFalse(file.exists());

		file = FileUtil.getDirectory("target/getDirectoryTest_String_boolean", true);
		assertTrue(file.exists());

		FileUtil.deleteFile(file);
	}

	@Test
	public void getDirectoryTest_File_String()
	{
		File parent = FileUtil.getDirectory("target/getDirectoryTest_File_String", true);

		File file = FileUtil.getDirectory(parent, "test0/test1");
		assertTrue(file.exists());
		assertTrue(file.isDirectory());

		FileUtil.deleteFile(parent);
	}

	@Test
	public void getDirectoryTest_File_String_boolean()
	{
		File parent = FileUtil.getDirectory("target/getDirectoryTest_File_String_boolean", true);

		File file = FileUtil.getDirectory(parent, "test0/test1", false);
		assertFalse(file.exists());

		file = FileUtil.getDirectory(parent, "test2/test3", true);
		assertTrue(file.exists());
		assertTrue(file.isDirectory());

		FileUtil.deleteFile(parent);
	}
	
	@Test
	public void createTempDirectoryTest() throws IOException
	{
		File directory = FileUtil.createTempDirectory();
		
		assertTrue(directory.isDirectory());
		
		File file = new File(directory, "test.txt");
		
		try(Writer w = IOUtil.getWriter(file))
		{
			w.write("test");
		}
		
		assertTrue(file.length() > 0);
		
		FileUtil.deleteFile(directory);
	}
	
	@Test
	public void createTempFileTest() throws IOException
	{
		File file = FileUtil.createTempFile();
		
		assertFalse(file.isDirectory());
		assertTrue(file.length() == 0);
		
		try(Writer w = IOUtil.getWriter(file))
		{
			w.write("test");
		}
		
		assertTrue(file.length() > 0);
		
		FileUtil.deleteFile(file);
	}
	
	@Test
	public void createTempFileTest_extension() throws IOException
	{
		File file = FileUtil.createTempFile("mytestsuffix");
		
		assertFalse(file.isDirectory());
		assertTrue(file.getName().indexOf("mytestsuffix") > 0);
		assertTrue(file.length() == 0);
		
		try(Writer w = IOUtil.getWriter(file))
		{
			w.write("test");
		}
		
		assertTrue(file.length() > 0);
		
		FileUtil.deleteFile(file);
	}
	
	@Test
	public void renameTrackedTest() throws IOException
	{
		File target = FileUtil.getFile("target");
		boolean targetExists = target.exists();
		if(!targetExists)
			target.mkdir();

		//文件 -> 文件
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File src = FileUtil.getFile(testRoot, "a/b/c/txt.txt", true);
			writeTestFile(src);
			File dest = FileUtil.getFile(testRoot, "aa/bb/cc/txt.txt");
			
			Map<File,File> tracks = FileUtil.renameTracked(src, dest);
			
			assertEquals(src, tracks.get(dest));
			assertTrue(dest.exists());
			assertFalse(src.exists());
			
			FileUtil.deleteFile(testRoot);
		}
		
		//文件 -> 目录
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File src = FileUtil.getFile(testRoot, "a/b/c/txt.txt", true);
			writeTestFile(src);
			File destDirectory = FileUtil.getDirectory(testRoot, "aa/bb/cc/", true);
			
			Map<File,File> tracks = FileUtil.renameTracked(src, destDirectory);
			
			File dest = FileUtil.getFile(destDirectory, "txt.txt");
			
			assertEquals(src, tracks.get(dest));
			assertTrue(dest.exists());
			assertFalse(src.exists());
			
			FileUtil.deleteFile(testRoot);
		}
		
		//文件 -> 自己
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File src = FileUtil.getFile(testRoot, "a/b/c/txt.txt", true);
			writeTestFile(src);
			File dest = FileUtil.getFile(testRoot, "a/b/c/txt.txt");
			
			Map<File,File> tracks = FileUtil.renameTracked(src, dest);
			
			assertTrue(tracks.isEmpty());
			assertTrue(dest.exists());
			assertTrue(src.exists());
			
			FileUtil.deleteFile(testRoot);
		}

		//文件 -> 自己父目录
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File src = FileUtil.getFile(testRoot, "a/b/c/txt.txt", true);
			writeTestFile(src);
			File dest = FileUtil.getFile(testRoot, "a/b/c/");
			
			Map<File,File> tracks = FileUtil.renameTracked(src, dest);
			
			assertTrue(tracks.isEmpty());
			assertTrue(dest.exists());
			assertTrue(src.exists());
			
			FileUtil.deleteFile(testRoot);
		}
		
		//目录 -> 目录
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File txta = FileUtil.getFile(testRoot, "a/b/c/txt-a.txt", true);
			writeTestFile(txta);
			File txtb = FileUtil.getFile(testRoot, "a/b/c/txt-b.txt", true);
			writeTestFile(txtb);
			
			File src = FileUtil.getFile(testRoot, "a/b");
			File dest = FileUtil.getDirectory(testRoot, "aa/bb/cc");

			Map<File,File> tracks = FileUtil.renameTracked(src, dest);
			
			assertEquals(txta, tracks.get(FileUtil.getFile(dest, "c/txt-a.txt")));
			assertEquals(txtb, tracks.get(FileUtil.getFile(dest, "c/txt-b.txt")));
			assertTrue(dest.exists());
			assertFalse(src.exists());
			
			FileUtil.deleteFile(testRoot);
		}

		//目录 -> 上级目录
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File txta = FileUtil.getFile(testRoot, "a/b/c/d/txt-a.txt", true);
			writeTestFile(txta);
			File txtb = FileUtil.getFile(testRoot, "a/b/c/d/txt-b.txt", true);
			writeTestFile(txtb);
			File txtc = FileUtil.getFile(testRoot, "a/b/c/e/txt-c.txt", true);
			writeTestFile(txtc);
			
			File src = FileUtil.getFile(testRoot, "a/b/c");
			File dest = FileUtil.getDirectory(testRoot, "a/b");
			
			Map<File,File> tracks = FileUtil.renameTracked(src, dest);
			
			assertEquals(txta, tracks.get(FileUtil.getFile(dest, "d/txt-a.txt")));
			assertEquals(txtb, tracks.get(FileUtil.getFile(dest, "d/txt-b.txt")));
			assertEquals(txtc, tracks.get(FileUtil.getFile(dest, "e/txt-c.txt")));
			assertTrue(dest.exists());
			assertFalse(src.exists());
			
			FileUtil.deleteFile(testRoot);
		}

		//目录 -> 下级新目录
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File txta = FileUtil.getFile(testRoot, "a/b/c/d/txt-a.txt", true);
			writeTestFile(txta);
			File txtb = FileUtil.getFile(testRoot, "a/b/c/d/txt-b.txt", true);
			writeTestFile(txtb);
			File txtc = FileUtil.getFile(testRoot, "a/b/c/e/txt-c.txt", true);
			writeTestFile(txtc);
			
			File src = FileUtil.getFile(testRoot, "a/b/c");
			File dest = FileUtil.getDirectory(testRoot, "a/b/f");
			
			Map<File,File> tracks = FileUtil.renameTracked(src, dest);
			
			assertEquals(txta, tracks.get(FileUtil.getFile(dest, "d/txt-a.txt")));
			assertEquals(txtb, tracks.get(FileUtil.getFile(dest, "d/txt-b.txt")));
			assertEquals(txtc, tracks.get(FileUtil.getFile(dest, "e/txt-c.txt")));
			assertTrue(dest.exists());
			assertFalse(src.exists());
			
			FileUtil.deleteFile(testRoot);
		}

		//目录 -> 下级旧目录
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File txta = FileUtil.getFile(testRoot, "a/b/c/d/txt-a.txt", true);
			writeTestFile(txta);
			File txtb = FileUtil.getFile(testRoot, "a/b/c/d/txt-b.txt", true);
			writeTestFile(txtb);
			File txtc = FileUtil.getFile(testRoot, "a/b/c/e/txt-c.txt", true);
			writeTestFile(txtc);
			
			File src = FileUtil.getFile(testRoot, "a/b/c");
			File dest = FileUtil.getDirectory(testRoot, "a/b/c/e");
			
			Map<File,File> tracks = FileUtil.renameTracked(src, dest);
			
			assertEquals(txta, tracks.get(FileUtil.getFile(dest, "d/txt-a.txt")));
			assertEquals(txtb, tracks.get(FileUtil.getFile(dest, "d/txt-b.txt")));
			assertTrue(dest.exists());
			assertFalse(FileUtil.getFile(src, "d").exists());
			
			FileUtil.deleteFile(testRoot);
		}

		//目录 -> 自己
		{
			FileUtil.deleteFile(FileUtil.getFile(target, "renameWithTrackTest"));
			File testRoot = FileUtil.getDirectory(target, "renameWithTrackTest");
			
			File txta = FileUtil.getFile(testRoot, "a/b/c/txt-a.txt", true);
			writeTestFile(txta);
			File txtb = FileUtil.getFile(testRoot, "a/b/c/txt-b.txt", true);
			writeTestFile(txtb);
			
			File src = FileUtil.getFile(testRoot, "a/b");
			File dest = FileUtil.getDirectory(testRoot, "a/b");

			Map<File,File> tracks = FileUtil.renameTracked(src, dest);
			
			assertTrue(tracks.isEmpty());
			assertTrue(dest.exists());
			assertTrue(src.exists());
			
			FileUtil.deleteFile(testRoot);
		}

		if(!targetExists)
			FileUtil.deleteFile(target);
	}
	
	protected void writeTestFile(File file) throws IOException
	{
		Reader reader = new StringReader("test");
		Writer out = null;
		
		try
		{
			out = IOUtil.getWriter(file);
			IOUtil.write(reader, out);
		}
		finally
		{
			IOUtil.close(reader);
			IOUtil.close(out);
		}
	}
}
