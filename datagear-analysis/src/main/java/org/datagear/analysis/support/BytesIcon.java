/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.datagear.analysis.Icon;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;

/**
 * 字节数组{@linkplain Icon}。
 * 
 * @author datagear@163.com
 *
 */
public class BytesIcon implements Icon
{
	private static final long serialVersionUID = 1L;

	private String type;

	private byte[] bytes;

	private long lastModified;

	public BytesIcon()
	{
	}

	public BytesIcon(String type, byte[] bytes, long lastModified)
	{
		super();
		this.type = type;
		this.bytes = bytes;
		this.lastModified = lastModified;
	}

	@Override
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public byte[] getBytes()
	{
		return bytes;
	}

	public void setBytes(byte[] bytes)
	{
		this.bytes = bytes;
	}

	@Override
	public long getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return new ByteArrayInputStream(this.bytes);
	}

	/**
	 * 构建{@linkplain BytesIcon}。
	 * 
	 * @param bytes
	 * @param lastModified
	 * @return
	 */
	public static BytesIcon valueOf(String type, byte[] bytes, long lastModified)
	{
		return new BytesIcon(type, bytes, lastModified);
	}

	/**
	 * 构建{@linkplain BytesIcon}。
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static BytesIcon valueOf(File file) throws IOException
	{
		return valueOf(FileUtil.getExtension(file), file);
	}

	/**
	 * 构建{@linkplain BytesIcon}。
	 * 
	 * @param type
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static BytesIcon valueOf(String type, File file) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			IOUtil.write(file, out);
		}
		finally
		{
			IOUtil.close(out);
		}

		return new BytesIcon(type, out.toByteArray(), file.lastModified());
	}

	/**
	 * 构建{@linkplain BytesIcon}。
	 * <p>
	 * 它不会关闭{@code in}输入流。
	 * </p>
	 * 
	 * @param type
	 * @param in
	 * @param lastModified
	 * @return
	 * @throws IOException
	 */
	public static BytesIcon valueOf(String type, InputStream in, long lastModified) throws IOException
	{
		return valueOf(type, in, lastModified, false);
	}

	/**
	 * 构建{@linkplain BytesIcon}。
	 * 
	 * @param type
	 * @param in
	 * @param lastModified
	 * @param closeIn
	 * @return
	 * @throws IOException
	 */
	public static BytesIcon valueOf(String type, InputStream in, long lastModified, boolean closeIn) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			IOUtil.write(in, out);
		}
		finally
		{
			if (closeIn)
				IOUtil.close(in);

			IOUtil.close(out);
		}

		return new BytesIcon(type, out.toByteArray(), lastModified);
	}
}
