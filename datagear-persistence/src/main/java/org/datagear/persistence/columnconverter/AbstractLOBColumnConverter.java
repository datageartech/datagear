/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.columnconverter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * 抽象大对象列值转换器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractLOBColumnConverter extends AbstractColumnConverter
{
	/** 系统默认临时文件夹 */
	public static File SYS_TMP_DIR = new File(System.getProperty("java.io.tmpdir"));

	/** 转换为文件时的父路径 */
	private File directory = SYS_TMP_DIR;

	public AbstractLOBColumnConverter()
	{
		super();
	}

	public File getDirectory()
	{
		return directory;
	}

	public void setDirectory(File directory)
	{
		this.directory = directory;
	}

	/**
	 * 读取输入流，并写入输出流。
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	protected void write(InputStream in, OutputStream out) throws IOException
	{
		byte[] cache = new byte[1024];
		int readLen = -1;

		while ((readLen = in.read(cache)) > -1)
			out.write(cache, 0, readLen);
	}

	/**
	 * 读取输入流，并写入输出流。
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	protected void write(Reader in, Writer out) throws IOException
	{
		char[] cache = new char[512];
		int readLen = -1;

		while ((readLen = in.read(cache)) > -1)
			out.write(cache, 0, readLen);
	}

	/**
	 * 读取输入流，并写入输出流。
	 * 
	 * @param in
	 * @param out
	 * @param writeLength
	 * @throws IOException
	 */
	protected void write(Reader in, Writer out, long writeLength) throws IOException
	{
		char[] cache = new char[512];
		int readLen = -1;

		if (writeLength < 0)
		{
			while ((readLen = in.read(cache)) > -1)
				out.write(cache, 0, readLen);
		}
		else
		{
			int toLength = (writeLength >= cache.length ? cache.length : (int) writeLength);

			while ((readLen = in.read(cache, 0, toLength)) > -1)
			{
				out.write(cache, 0, readLen);

				writeLength = writeLength - toLength;
			}
		}
	}

	/**
	 * 关闭{@linkplain Closeable}。
	 * 
	 * @param closeable
	 */
	protected void close(Closeable closeable)
	{
		if (closeable == null)
			return;

		try
		{
			closeable.close();
		}
		catch (IOException e)
		{
			throw new ColumnConverterException(e);
		}
	}
}
