/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

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
}
