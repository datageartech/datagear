/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.io.Serializable;

/**
 * 驱动库信息。
 * 
 * @author datagear@163.com
 *
 */
public class DriverLibraryInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 库名称 */
	private String name;

	/** 库字节数大小 */
	private long size;

	public DriverLibraryInfo()
	{
		super();
	}

	public DriverLibraryInfo(String name, long size)
	{
		super();
		this.name = name;
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

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", size=" + size + "]";
	}
}
