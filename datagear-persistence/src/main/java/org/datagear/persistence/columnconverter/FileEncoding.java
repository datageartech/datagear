/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.columnconverter;

import java.io.File;

/**
 * 文件编码信息。
 * <p>
 * {@linkplain ClobColumnConverter}能够将此类实例转换为{@linkplain Clob}。
 * </p>
 * <p>
 * {@linkplain ClobColumnConverter}在将{@linkplain File}转换为{@linkplain Clob}时，需要知道
 * {@linkplain File}的编码，而{@linkplain File}本身无法提供编码信息，此类用于封装文件编码信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class FileEncoding
{
	private File file;

	private String encoding;

	public FileEncoding()
	{
		super();
	}

	public FileEncoding(File file, String encoding)
	{
		super();
		this.file = file;
		this.encoding = encoding;
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileEncoding other = (FileEncoding) obj;
		if (encoding == null)
		{
			if (other.encoding != null)
				return false;
		}
		else if (!encoding.equals(other.encoding))
			return false;
		if (file == null)
		{
			if (other.file != null)
				return false;
		}
		else if (!file.equals(other.file))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [file=" + file + ", encoding=" + encoding + "]";
	}
}
