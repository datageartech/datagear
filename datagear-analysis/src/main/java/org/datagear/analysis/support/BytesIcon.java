/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.datagear.analysis.Icon;
import org.datagear.util.IOUtil;

/**
 * 字节数组{@linkplain Icon}。
 * 
 * @author datagear@163.com
 *
 */
public class BytesIcon implements Icon
{
	private byte[] bytes;

	public BytesIcon()
	{
	}

	public BytesIcon(byte[] bytes)
	{
		super();
		this.bytes = bytes;
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
	public InputStream getInputStream() throws IOException
	{
		return new ByteArrayInputStream(this.bytes);
	}

	/**
	 * 构建{@linkplain BytesIcon}。
	 * 
	 * @param bytes
	 * @return
	 */
	public static BytesIcon valueOf(byte[] bytes)
	{
		return new BytesIcon(bytes);
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
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			IOUtil.write(file, out);
		}
		finally
		{
			IOUtil.close(out);
		}

		return new BytesIcon(out.toByteArray());
	}

	/**
	 * 构建{@linkplain BytesIcon}。
	 * <p>
	 * 它不会关闭{@code in}输入流。
	 * </p>
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static BytesIcon valueOf(InputStream in) throws IOException
	{
		return valueOf(in, false);
	}

	/**
	 * 构建{@linkplain BytesIcon}。
	 * 
	 * @param in
	 * @param closeIn
	 * @return
	 * @throws IOException
	 */
	public static BytesIcon valueOf(InputStream in, boolean closeIn) throws IOException
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

		return new BytesIcon(out.toByteArray());
	}
}
