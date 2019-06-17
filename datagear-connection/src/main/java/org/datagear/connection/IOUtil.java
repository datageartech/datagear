/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * IO工具类。
 * 
 * @author datagear@163.com
 *
 */
public class IOUtil
{
	public IOUtil()
	{
		super();
	}

	/**
	 * 获取文件对象。
	 * 
	 * @param file
	 * @return
	 */
	public static File getFile(String file)
	{
		return new File(file);
	}

	/**
	 * 获取文件对象。
	 * 
	 * @param directory
	 * @param file
	 * @return
	 */
	public static File getFile(File directory, String file)
	{
		return new File(directory, file);
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
	 * 清空目录，保留目录本身。
	 * 
	 * @param directory
	 */
	public static void clearDirectory(File directory)
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
	 * 读取输入流，并写入输出流。
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void write(InputStream in, OutputStream out) throws IOException
	{
		byte[] cache = new byte[1024];
		int readLen = -1;

		while ((readLen = in.read(cache)) > -1)
			out.write(cache, 0, readLen);
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

			write(in, out);
		}
		finally
		{
			close(in);
		}
	}

	/**
	 * 将输入流写入文件。
	 * 
	 * @param in
	 * @param file
	 * @throws IOException
	 */
	public static void write(InputStream in, File file) throws IOException
	{
		OutputStream out = null;

		try
		{
			out = getOutputStream(file);

			write(in, out);
		}
		finally
		{
			close(out);
		}
	}

	/**
	 * 将文件写入ZIP输出流。
	 * 
	 * @param out
	 * @param file
	 * @param zipEntryName
	 * @throws IOException
	 */
	public static void writeFileToZipOutputStream(ZipOutputStream out, File file, String zipEntryName)
			throws IOException
	{
		if (!file.exists())
			return;

		boolean isDirectory = file.isDirectory();

		if (isDirectory && !zipEntryName.endsWith("/"))
			zipEntryName = zipEntryName + "/";

		ZipEntry zipEntry = new ZipEntry(zipEntryName);

		out.putNextEntry(zipEntry);

		if (!isDirectory)
		{
			InputStream fileIn = null;

			try
			{
				fileIn = new FileInputStream(file);
				write(fileIn, out);
			}
			finally
			{
				close(fileIn);
			}
		}

		out.closeEntry();

		if (isDirectory)
		{
			File[] children = file.listFiles();

			for (File child : children)
			{
				String myName = zipEntryName + child.getName();
				writeFileToZipOutputStream(out, child, myName);
			}
		}
	}

	/**
	 * 获取输入流的{@linkplain ByteArrayInputStream}输入流。
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayInputStream getByteArrayInputStream(InputStream in) throws IOException
	{
		byte[] bytes = getBytes(in);

		return new ByteArrayInputStream(bytes);
	}

	/**
	 * 获取输入流的数据字节数组。
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			write(in, out);
			return out.toByteArray();
		}
		finally
		{
			close(out);
		}
	}

	/**
	 * 获取输入流。
	 * 
	 * @param file
	 * @param encoding
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static Reader getReader(File file, String encoding)
			throws FileNotFoundException, UnsupportedEncodingException
	{
		return getReader(getInputStream(file), encoding);
	}

	/**
	 * 获取输入流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static Reader getReader(File file) throws FileNotFoundException, UnsupportedEncodingException
	{
		return getReader(getInputStream(file), Charset.defaultCharset().name());
	}

	/**
	 * 获取输入流。
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Reader getReader(InputStream in, String encoding) throws UnsupportedEncodingException
	{
		return new BufferedReader(new InputStreamReader(in, encoding));
	}

	/**
	 * 获取输出流。
	 * 
	 * @param file
	 * @param encoding
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static Writer getWriter(File file, String encoding)
			throws FileNotFoundException, UnsupportedEncodingException
	{
		return getWriter(getOutputStream(file), encoding);
	}

	/**
	 * 获取输出流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static Writer getWriter(File file) throws FileNotFoundException, UnsupportedEncodingException
	{
		return getWriter(getOutputStream(file), Charset.defaultCharset().name());
	}

	/**
	 * 获取输出流。
	 * 
	 * @param out
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Writer getWriter(OutputStream out, String encoding) throws UnsupportedEncodingException
	{
		return new BufferedWriter(new OutputStreamWriter(out, encoding));
	}

	/**
	 * 获取文件输入流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static InputStream getInputStream(File file) throws FileNotFoundException
	{
		return new FileInputStream(file);
	}

	/**
	 * 获取文件输入流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static InputStream getInputStream(String file) throws FileNotFoundException
	{
		return new FileInputStream(new File(file));
	}

	/**
	 * 获取文件输出流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static OutputStream getOutputStream(File file) throws FileNotFoundException
	{
		return new FileOutputStream(file);
	}

	/**
	 * 获取文件输出流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static OutputStream getOutputStream(String file) throws FileNotFoundException
	{
		return new FileOutputStream(new File(file));
	}

	/**
	 * 获取ZIP输入流。
	 * 
	 * @param in
	 * @return
	 */
	public static ZipInputStream getZipInputStream(InputStream in)
	{
		ZipInputStream zin = new ZipInputStream(in);

		return zin;
	}

	/**
	 * 获取ZIP输入流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ZipInputStream getZipInputStream(File file) throws FileNotFoundException
	{
		ZipInputStream in = new ZipInputStream(getInputStream(file));

		return in;
	}

	/**
	 * 获取ZIP输入流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ZipInputStream getZipInputStream(String file) throws FileNotFoundException
	{
		ZipInputStream in = new ZipInputStream(getInputStream(file));

		return in;
	}

	/**
	 * 获取ZIP输出流。
	 * 
	 * @param out
	 * @return
	 */
	public static ZipOutputStream getZipOutputStream(OutputStream out)
	{
		ZipOutputStream zout = new ZipOutputStream(out);

		return zout;
	}

	/**
	 * 获取ZIP输出流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ZipOutputStream getZipOutputStream(File file) throws FileNotFoundException
	{
		ZipOutputStream out = new ZipOutputStream(getOutputStream(file));

		return out;
	}

	/**
	 * 获取ZIP输出流。
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ZipOutputStream getZipOutputStream(String file) throws FileNotFoundException
	{
		ZipOutputStream out = new ZipOutputStream(getOutputStream(file));

		return out;
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

				OutputStream out = getOutputStream(my);

				try
				{
					write(zipInputStream, out);
				}
				finally
				{
					close(out);
				}
			}

			zipInputStream.closeEntry();
		}
	}

	/**
	 * 关闭{@linkplain Closeable}。
	 * <p>
	 * 此方法不会抛出任何{@linkplain Throwable}。
	 * </p>
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
		catch (Throwable t)
		{
		}
	}

	/**
	 * 刷新{@linkplain Flushable}。
	 * 
	 * @param flushable
	 */
	public static void flush(Flushable flushable)
	{
		if (flushable == null)
			return;

		try
		{
			flushable.flush();
		}
		catch (IOException e)
		{
		}
	}
}
