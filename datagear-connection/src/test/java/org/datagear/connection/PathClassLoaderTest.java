/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain PathClassLoader}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class PathClassLoaderTest
{
	@Test
	public void setHoldJarFileToFalseTest() throws Exception
	{
		PathClassLoader classLoader = new PathClassLoader("src/test/resources/drivers/mysql");
		classLoader.setHoldJarFile(false);
		classLoader.init();

		try
		{
			try
			{
				classLoader.loadClass("com.mysql.jdbc.Driver");
			}
			catch (Throwable t)
			{
			}

			File holdFile = new File(classLoader.getPath(), "test-for-holder.jar");
			copyFileToFolder(holdFile, new File("target/"));

			boolean deleted = holdFile.delete();
			Assert.assertTrue(deleted);

			if (deleted)
				copyFileToFolder(new File("target/test-for-holder.jar"), classLoader.getPath());
		}
		finally
		{
			classLoader.close();
		}
	}

	@Test
	public void setHoldJarFileToTrueTest() throws Exception
	{
		PathClassLoader classLoader = new PathClassLoader("src/test/resources/drivers/mysql");
		classLoader.setHoldJarFile(true);
		classLoader.init();

		try
		{
			try
			{
				classLoader.loadClass("com.mysql.jdbc.Driver");
			}
			catch (Throwable t)
			{
			}

			File holdFile = new File(classLoader.getPath(), "test-for-holder.jar");
			copyFileToFolder(holdFile, new File("target/"));

			boolean deleted = holdFile.delete();
			Assert.assertFalse(deleted);

			if (deleted)
				copyFileToFolder(new File("target/test-for-holder.jar"), classLoader.getPath());
		}
		finally
		{
			classLoader.close();
		}
	}

	protected void copyFileToFolder(File file, File folder) throws IOException
	{
		if (!folder.exists())
			folder.mkdirs();

		InputStream in = new FileInputStream(file);
		OutputStream out = new FileOutputStream(new File(folder, file.getName()));

		byte[] cache = new byte[1024];
		int readLen = -1;

		try
		{
			while ((readLen = in.read(cache)) > -1)
				out.write(cache, 0, readLen);
		}
		finally
		{
			in.close();
			out.close();
		}
	}
}
