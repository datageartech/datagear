/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.vo;

import java.io.Serializable;

/**
 * 文件信息。
 * 
 * @author datagear@163.com
 *
 */
public class FileInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 文件名 */
	private String name;

	/** 字节数 */
	private long bytes;

	/** 友好显示的大小 */
	private String size;

	public FileInfo()
	{
		super();
	}

	public FileInfo(String name)
	{
		super();
		this.name = name;
	}

	public FileInfo(String name, long bytes)
	{
		super();
		this.name = name;
		this.bytes = bytes;
	}

	public FileInfo(String name, long bytes, String size)
	{
		super();
		this.name = name;
		this.bytes = bytes;
		this.size = size;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getBytes()
	{
		return bytes;
	}

	public void setBytes(long bytes)
	{
		this.bytes = bytes;
	}

	public String getSize()
	{
		return size;
	}

	public void setSize(String size)
	{
		this.size = size;
	}
}
