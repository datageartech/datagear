/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util;

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
import java.io.StringWriter;
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
	private IOUtil()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 从输入流读字符串。
	 * 
	 * @param in
	 * @param encoding
	 * @param closeIn
	 * @return
	 * @throws IOException
	 */
	public static String readString(InputStream in, String encoding, boolean closeIn) throws IOException
	{
		Reader reader = getReader(in, encoding);

		return readString(reader, closeIn);
	}

	/**
	 * 从输入流读字符串。
	 * 
	 * @param in
	 * @param closeIn
	 * @return
	 * @throws IOException
	 */
	public static String readString(Reader in, boolean closeIn) throws IOException
	{
		StringWriter out = new StringWriter();

		try
		{
			write(in, out);
		}
		finally
		{
			if (closeIn)
				close(in);

			close(out);
		}

		return out.toString();
	}

	/**
	 * 从输入流读字节数组。
	 * 
	 * @param in
	 * @param closeIn
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytes(InputStream in, boolean closeIn) throws IOException
	{
		ByteArrayOutputStream out = null;

		try
		{
			out = new ByteArrayOutputStream();

			write(in, out);
		}
		finally
		{
			if (closeIn)
				close(in);

			close(out);
		}

		return out.toByteArray();
	}

	/**
	 * 读取字符输入流，并写入字符输出流。
	 * 
	 * @param reader
	 * @param writer
	 * @throws IOException
	 */
	public static void write(Reader reader, Writer writer) throws IOException
	{
		char[] cache = new char[512];
		int readLen = -1;

		while ((readLen = reader.read(cache)) > -1)
			writer.write(cache, 0, readLen);
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
	 *            文件的ZIP条目名，可以为{@code null}或者空字符串。
	 * @throws IOException
	 */
	public static void writeFileToZipOutputStream(ZipOutputStream out, File file, String zipEntryName)
			throws IOException
	{
		if (!file.exists())
			return;

		boolean isDirectory = file.isDirectory();
		boolean isZipEntryNameEmpty = (zipEntryName == null || zipEntryName.isEmpty());

		if (isDirectory)
		{
			if (isZipEntryNameEmpty)
				zipEntryName = "";
			else if (!zipEntryName.endsWith("/"))
				zipEntryName = zipEntryName + "/";
		}
		else if (isZipEntryNameEmpty)
		{
			zipEntryName = file.getName();
			isZipEntryNameEmpty = false;
		}

		if (!isZipEntryNameEmpty)
		{
			ZipEntry zipEntry = new ZipEntry(zipEntryName);
			out.putNextEntry(zipEntry);
		}

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

		if (!isZipEntryNameEmpty)
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
	public static BufferedReader getReader(File file, String encoding)
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
	public static BufferedReader getReader(File file) throws FileNotFoundException, UnsupportedEncodingException
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
	public static BufferedReader getReader(InputStream in, String encoding) throws UnsupportedEncodingException
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
	public static BufferedWriter getWriter(File file, String encoding)
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
	public static BufferedWriter getWriter(File file) throws FileNotFoundException, UnsupportedEncodingException
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
	public static BufferedWriter getWriter(OutputStream out, String encoding) throws UnsupportedEncodingException
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

	/**
	 * 连接路径。
	 * 
	 * @param parent
	 * @param child
	 * @param separator
	 * @return
	 */
	public static String concatPath(String parent, String child, String separator)
	{
		parent = trimPath(parent, separator);
		child = trimPath(child, separator);

		boolean parentEndsWith = parent.endsWith(separator);
		boolean childStartsWith = child.startsWith(separator);

		if (parentEndsWith && childStartsWith)
			return parent + child.substring(separator.length());
		else if (parentEndsWith || childStartsWith)
			return parent + child;
		else
			return parent + separator + child;
	}

	/**
	 * 整理路径。
	 * <p>
	 * 此方法将路径中的{@code "/"}、{@code "\"}统一替换为指定的{@code separator}。
	 * </p>
	 * 
	 * @param path
	 * @param separator
	 * @return
	 */
	public static String trimPath(String path, String separator)
	{
		if (path == null)
			return null;

		if (separator.equals("\\"))
			return path.replace("/", separator);
		else
			return path.replace("\\", separator);
	}
}
